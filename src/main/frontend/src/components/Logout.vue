<template></template>

<script>
export default {
  name: 'Logout',
  created () {
    delete localStorage.currentUser
    this.$store.dispatch('logout')
    this.$router.push('/')
  }
}
</script>
