<template>
  <div class="login-overlay">
    <div class="login-wrapper border border-light">
      <form class="form-signin">
        <!-- <a class="btn btn-lg btn-primary btn-block" v-on:click="login">Sign in with Google</a> -->
        <a class="btn btn-lg btn-primary btn-block" href="http://localhost:8080/protected/somepage">Sign in with Google</a>
      </form>
    </div>
  </div>
</template>

<script>

export default {
  name: 'Login',
  data () {
    return {
      email: '',
      password: '',
      error: false
    }
  },
  created () {
    this.checkCurrentLogin()
  },
  updated () {
    this.checkCurrentLogin()
  },
  methods: {
    checkCurrentLogin () {
      if (this.currentUser) {
        this.$router.replace(this.$route.query.redirect || '/authors')
      }
    },
    login () {
      // http://localhost:8080/protected/login"
      // getUserInfo
      // this.$http.get('/protected/getUserInfo')
      //   .then(request => this.loginSuccessful(request))
      //   .catch(() => {
      //     console.log("now have to redirect to login page !")
      //     window.location.href = 'http://localhost:8080/protected/login?state=bla';
      //   })
    },
    loginSuccessful (req) {
      console.log(req)
      if (!req.data.access_token) {
        this.loginFailed()
        return
      }
      this.error = false
      localStorage.token = req.data.access_token
      this.$store.dispatch('login')
      this.$router.replace(this.$route.query.redirect || '/authors')
    },
    loginFailed () {
      this.error = 'Login failed!'
      this.$store.dispatch('logout')
      delete localStorage.token
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