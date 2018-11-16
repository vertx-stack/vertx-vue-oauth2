<template>
  <div class="login-overlay">
    <div class="login-wrapper border border-light">
      <form class="form-signin">
        <div v-if=response class="text-red"><p>{{response}}</p></div>
        <!-- <a class="btn btn-lg btn-primary btn-block" v-on:click="login">Sign in with Google</a> -->
        <a class="btn btn-lg btn-primary btn-block" v-on:click="signIn">Sign in with Google</a>
      </form>
    </div>
  </div>
</template>

<script>
import Vue from 'vue'
export default {
  name: 'Login',
  data: function (router) {
    return {
      section: 'Login',
      loading: '',
      response: ''
    }
  },
  methods: {
    signIn: function () {
      let ctx = this;
      let useBackendScenario = true;

      // Scenario-1 (backend auth): use this code if you'd wanna handle Google OAuth on your vertx backend
      if(useBackendScenario == true) {
        Vue.googleAuth().signIn(this.onSignInSuccess, this.onSignInError)
      }
      // Scenario-2 (frontend auth): use this code if you'd wanna handle Google OAuth o VueJS
      else {
        Vue.googleAuth().directAccess()
        Vue.googleAuth().signIn(function (googleUser) {
          let user = JSON.parse(JSON.stringify(googleUser));
          localStorage.currentUser = JSON.stringify(user.w3);
          ctx.$store.dispatch('login')
          ctx.$router.replace(ctx.$route.query.redirect || '/authors')
        }, function (error) {
          console.log(error)
        })
      }
    },
    onSignInSuccess: function (authorizationCode) {
      let ctx = this;

      // backend-auth: use this code if you'd wanna handle the goole auth on your backend
      this.toggleLoading()
      this.resetResponse()
      this.$http.post('http://localhost:8080/auth/google', { code: authorizationCode, redirect_uri: 'postmessage' }).then(function (response) {
        if (response.data) {
          var user = JSON.parse(JSON.stringify(response.data))
          localStorage.currentUser = JSON.stringify(user);
          ctx.$store.dispatch('login')
          ctx.$router.replace(ctx.$route.query.redirect || '/authors')
        }
      }, function (response) {
        var data = response.body
        this.response = data.error
        console.log('BACKEND SERVER - SIGN-IN ERROR', data)
      })
    },
    onSignInError: function (error) {
      this.response = 'Failed to sign-in'
      console.log('GOOGLE SERVER - SIGN-IN ERROR', error)
    },
    toggleLoading: function () {
      this.loading = (this.loading === '') ? 'loading' : ''
    },
    resetResponse: function () {
      this.response = ''
    }
  }
}
</script>

<style lang="css" scoped>
.login-overlay {
  background: #605B56 !important;
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
}

.login-wrapper {
  background: #fff;
  width: 70%;
  margin: 12% auto;
  animation: fadein 0.6s;
}

@keyframes fadein {
    from { opacity: 0; }
    to   { opacity: 1; }
}

.form-signin {
  max-width: 330px;
  padding: 10% 15px;
  margin: 0 auto;
}
.form-signin .form-signin-heading,
.form-signin .checkbox {
  margin-bottom: 10px;
}
.form-signin .checkbox {
  font-weight: normal;
}
.form-signin .form-control {
  position: relative;
  height: auto;
  -webkit-box-sizing: border-box;
          box-sizing: border-box;
  padding: 10px;
  font-size: 16px;
}
.form-signin .form-control:focus {
  z-index: 2;
}
.form-signin input[type="email"] {
  margin-bottom: -1px;
  border-bottom-right-radius: 0;
  border-bottom-left-radius: 0;
}
.form-signin input[type="password"] {
  margin-bottom: 10px;
  border-top-left-radius: 0;
  border-top-right-radius: 0;
}
</style>