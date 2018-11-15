// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue'
import App from './App'
import router from './router'
import axios from './backend/vue-axios'
import store from './store'
import 'font-awesome-webpack'
import GoogleAuth from 'vue-google-oauth'

Vue.use(GoogleAuth, { client_id: '1004190463315-nba24dnbjdppk9p2okf1gtbqm924e7mp.apps.googleusercontent.com' })
Vue.googleAuth().load()

Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  axios,
  store,
  template: '<App/>',
  components: { App }
})
