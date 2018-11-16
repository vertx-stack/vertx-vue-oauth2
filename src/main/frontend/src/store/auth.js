/* global localStorage */

import * as MutationTypes from './mutation-types'

const state = {
  user: localStorage.currentUser
}

const mutations = {
  [MutationTypes.LOGIN] (state) {
    let user = JSON.parse(localStorage.currentUser)
    // two different flavours of the user profile we can get (depending on whether frontend- or backen-auth was used)
    // aligning them here into a common structure
    if (user !== null) {
      // frontentd-auth flavour
      if (user.U3 !== undefined) {
        state.user = {
          email: user.U3,
          firstname: user.ofa,
          lastname: user.wea,
          fullname: user.ig,
          image: user.Paa
        }
      }
      // backend-auth flavour
      else if (user.objectType !== undefined) {
        state.user = {
          email: user.emails[0].value,
          firstname: user.name.givenName,
          lastname: user.name.familyName,
          fullname: user.displayName,
          image: user.image.url
        }
      }
    }
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
