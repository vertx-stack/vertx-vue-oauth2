<template>
  <nav class="navbar navbar-dark bg-header">
    <a class="navbar-brand" href="/">Your App</a>
    <span class="navbar-text">
      {{ currentUser.fullname }}
      <img v-bind:src="currentUser.image" /> 
      <router-link to="/logout" class="btn btn-logout btn-sm">Logout</router-link>
    </span>
  </nav>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Navbar',
  computed: {
    ...mapGetters({ currentUser: 'currentUser' })
  }
}
</script>

<style lang="scss" scoped>
.bg-header {
  background: rgb(31, 31, 31) !important;
}

.btn-logout {
  background-color: #007bff;
  color: #fff;
}

.btn-logout:hover {
  background-color: darken(#837A75, 20%);
  border-color: darken(#837A75, 20%);
  color: #fff;
}

img {
    border-radius: 50%;
    height: 50px
}
</style>
