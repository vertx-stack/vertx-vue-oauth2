/* global localStorage */

import * as MutationTypes from './mutation-types'

const state = {
  user: localStorage.currentUser
}

const mutations = {
  [MutationTypes.LOGIN] (state) {
    state.user = JSON.parse(localStorage.currentUser)
  },
  [MutationTypes.LOGOUT] (state) {
    state.user = null
  }
}

const getters = {
  currentUser (state) {
    return state.user
  }
}

const actions = {
  login ({ commit }) {
    commit(MutationTypes.LOGIN)
  },

  logout ({ commit }) {
    commit(MutationTypes.LOGOUT)
  }
}

export default {
  state,
  mutations,
  getters,
  actions
}
