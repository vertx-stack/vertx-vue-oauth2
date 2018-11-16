package com.ibm.example;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2ClientOptions;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.providers.OpenIDConnectAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.OAuth2AuthHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class MainVerticle extends AbstractVerticle {

	// configuration for HTTP server
	private JsonObject httpServer = new JsonObject()
			.put("hostname", "0.0.0.0")
			.put("port", 8080);

	// configuration for HTTPS server
	private JsonObject httpsServer = new JsonObject()
			.put("hostname", "0.0.0.0")
			.put("port", 4443)
			.put("keyStore", "test.jks")
			.put("enforceRedirect", false);

	private JsonArray messages = new JsonArray()
			.add(new JsonObject().put("content", "blabla"))
			.add(new JsonObject().put("content", "here's a second message"))
			.add(new JsonObject().put("content", "one more"));

	private OAuth2Auth googleOAuth2Provider = null;
	private OAuth2AuthHandler googleOAuth2Handler = null;

	@Override
	public void start(Future<Void> startFuture) {
		createHttpServerAndRoutes();
		createApiEndpoints();
	}


	@Override
	public void stop() throws Exception {
		super.stop();
	}


	private void createHttpServerAndRoutes()	{
		Router router = Router.router(vertx);

		// create HTTP server
		HttpServerOptions httpOptions = new HttpServerOptions();
		httpOptions.setHost(httpServer.getString("hostname"));
		httpOptions.setPort(httpServer.getInteger("port"));
		httpOptions.setSsl(false);
		vertx.createHttpServer(httpOptions).requestHandler(router::accept).listen();
		System.out.println("created HTTP server at " + httpServer.getString("hostname") + ":" + httpServer.getInteger("port"));

		// create HTTPS server 
		HttpServerOptions httpsOptions = new HttpServerOptions();
		httpsOptions.setHost(httpsServer.getString("hostname"));
		httpsOptions.setPort(httpsServer.getInteger("port"));
		httpsOptions.setSsl(true);
		httpsOptions.setKeyStoreOptions(new JksOptions().setPath( httpsServer.getString("keyStore") ).setPassword("testpassword"));
		vertx.createHttpServer(httpsOptions).requestHandler(router::accept).listen();
		System.out.println("created HTTPS server at " + httpsServer.getString("hostname") + ":" + httpsServer.getInteger("port") + " (keyFile: " + httpsServer.getString("keyStore") + ")");

		// configure BodyHandler
		final BodyHandler bodyHandler = BodyHandler.create();
		router.route("/app/*").handler(bodyHandler);
		router.route("/login").handler(bodyHandler);
		router.route().handler(BodyHandler.create());

		// enable CORS
		router.route().handler(CorsHandler.create("http://localhost:8081")
				.allowedMethod(io.vertx.core.http.HttpMethod.GET)
				.allowedMethod(io.vertx.core.http.HttpMethod.POST)
				.allowedMethod(io.vertx.core.http.HttpMethod.OPTIONS)
				.allowCredentials(true)
				.allowedHeader("Access-Control-Allow-Headers")
				.allowedHeader("Authorization")
				.allowedHeader("Access-Control-Allow-Method")
				.allowedHeader("Access-Control-Allow-Origin")
				.allowedHeader("Access-Control-Allow-Credentials")
				.allowedHeader("Content-Type"));

		// HTTP to HTTPS redirect
		router.route().handler( context -> {
			boolean sslUsed = context.request().isSSL();
			boolean enforceSslRedirect = httpsServer.getBoolean("enforceRedirect");

			if(!sslUsed && enforceSslRedirect) {
				try {
					int httpsPort = httpsServer.getInteger("port");

					URI myHttpUri = new URI( context.request().absoluteURI() );
					URI myHttpsUri = new URI("https", 
							myHttpUri.getUserInfo(), 
							myHttpUri.getHost(), 
							httpsPort,
							myHttpUri.getRawPath(), 
							myHttpUri.getRawQuery(), 
							myHttpUri.getRawFragment());
					context.response().putHeader("location", myHttpsUri.toString() ).setStatusCode(302).end();
				} catch(URISyntaxException ex) {
					ex.printStackTrace();
					context.next();
				}
			}
			else context.next();
		});

//		// handler to deliver the user info object, currently disabled
//		router.route("/app/userinfo").handler(context -> {
//			if (context.user() != null) {
//				JsonObject userDetails = context.user().principal();
//				userDetails.remove("password");
//				userDetails.put("jsessionid", context.session().id());
//				context.request().response().putHeader("Content-Type", "application/json");
//				context.request().response().end(userDetails.encodePrettily());
//			}
//			else context.request().response().end(
//					new JsonObject().put("error", "401").put("message", "user is not authenticated").encodePrettily()
//					);
//		});

	
		OpenIDConnectAuth.discover(
				vertx,
				new OAuth2ClientOptions()
				.setClientID("<your google auth client id>")
				.setClientSecret("<your google auth client secret>")
				.setSite("https://accounts.google.com")
				.setTokenPath("https://www.googleapis.com/oauth2/v3/token")
				.setAuthorizationPath("/o/oauth2/auth"),
				res -> {
					if (res.succeeded()) {
						// the setup call succeeded. At this moment your auth is ready to use and google signature keys
						// are loaded so tokens can be decoded and verified.
						
						System.out.println("Google OAUth2 setup successful !");
						googleOAuth2Provider = res.result();

						OAuth2FlowType flowType = googleOAuth2Provider.getFlowType();
						System.out.println("Flow Type: "+flowType);

						googleOAuth2Handler = OAuth2AuthHandler.create(googleOAuth2Provider, "http://localhost:8081/callback");
						Set<String> authorities = new HashSet<String>();
						authorities.add("profile");
						googleOAuth2Handler.addAuthorities(authorities);
						googleOAuth2Handler.setupCallback(router.get("/callback"));

						
//						// setup the callback handler for receiving the Google callback
//						oauth2.setupCallback(router.get("/callback").produces("application/json").handler(rc -> {
//							
//							System.err.println("received body ::: '"+rc.getBodyAsString()+"'");
////							System.err.println("received header ::: '"+rc.get()+"'");
//							String state = rc.request().getParam("state");
//							String code = rc.request().getParam("code");
//							String scope = rc.request().getParam("scope");
//							
////							Session session = rc.session();
////							session.get(key)
//							
//							rc.response()
//								.putHeader("state", state)
//								.putHeader("code", code)
//								.putHeader("scope", scope);
//							rc.put("code", code).next();
//							
//						}));
						
						// welcome page
//						router.get("/").handler(ctx -> ctx.response().putHeader("content-type", "text/html").end("Hello<br><a href=\"/protected/somepage\">Protected by Google</a>"));
						
					} else {
						// the setup failed.
						System.err.println("Google OAUth2 setup failed !");
					}
				});

		

		// Google auth handler, we can drive the the google auth from the backend here and request the access_token 
		// and id_token based on the authorization code however in this example we handle this in the frontend
		router.route("/auth/google").produces("application/json").handler(context -> {
			System.err.println("received body ::: '"+context.getBodyAsString()+"'");
			JsonObject authRequestJson = context.getBodyAsJson();

			// use the google oauth provider to execute authentication based on the "authorization code"
			googleOAuth2Provider.authenticate(authRequestJson, ctx -> {
				if(ctx.succeeded()) {
					// we've now successfull retrieved the access_token, refresh_token, id_token and the scope
					JsonObject token = ctx.result().principal();
					
					// now use the access_token to retrieve the user profile, we utilize vertx HttpClient to do this REST call
					HttpClient httpClient = vertx.createHttpClient();
					final String url = "https://www.googleapis.com/plus/v1/people/me?access_token="+token.getString("access_token");
					httpClient.getAbs(url, response -> {
						if (response.statusCode() != 200) {
							// anything else than HTTP 200 comes back, reply UNAUTHORIZED to the frontend
							context.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();
						} else {
							response.bodyHandler(b -> {
								// we've received the google user in a JSON object and we're going to reply this to the frontend
								JsonObject googleUser = b.toJsonObject();
								context.request().response().end( googleUser.encodePrettily() );
							});
						}
					}).end();
				}
				else {
					// authentication failed, reply UNAUTHORIZED to the frontend
					context.response().setStatusCode(HttpResponseStatus.UNAUTHORIZED.code()).end();
				}
			});
		});

		// configure EventBus based on the SockJSBridge
		router.route("/eventbus/*").handler(new SockJSBridge(vertx));

		// staticHandler to handle static resources
		router.route().handler(StaticHandler.create().setCachingEnabled(true));
	}


	/**
	 * creates Vertx EventBus based API endpoints (for query and mutation)
	 */
	private void createApiEndpoints() {
		vertx.eventBus().consumer("/api/messages", this::apiMessages);
		vertx.eventBus().consumer("/api/messages/delete", this::apiMessagesDelete);
		vertx.eventBus().consumer("/api/messages/add", this::apiMessagesAdd);
	}

	/**
	 * get messages API handler, this simply returns our as-is messages array
	 * @param msg
	 */
	private void apiMessages(Message<JsonObject> msg) {
		System.err.println("apiMessages called");
		msg.reply(messages);
	}

	/**
	 * delete message API handler, this deletes a given message from our messages array and 
	 * publishes the entire message array to all potential subscribers
	 * @param msg
	 */
	private void apiMessagesDelete(Message<JsonObject> msg) {
		JsonObject inputObject = msg.body();
		System.err.println("apiMessagesDelete called : "+inputObject.encode());

		for(int a=0; a < messages.size(); a++) {
			if(messages.getJsonObject(a).equals(inputObject)) {
				System.out.println("=> removing document: "+inputObject.encode());
				messages.remove(a);
				break;
			}
		}
		// publish all known messages to any subscriber
		this.vertx.eventBus().publish(":pubsub/messages", messages);
		msg.reply(new JsonObject());
	}

	/**
	 * add message API handler, this adds a new message into our messages array and 
	 * publishes the entire message array to all potential subscribers
	 * @param msg
	 */
	private void apiMessagesAdd(Message<JsonObject> msg) {
		JsonObject inputObject = msg.body();
		System.err.println("apiMessagesAdd called : "+inputObject.encode());
		messages.add(inputObject);

		// publish all known messages to any subscriber
		this.vertx.eventBus().publish(":pubsub/messages", messages);
		msg.reply(new JsonObject());
	}
}

