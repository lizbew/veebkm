
import * as types from '../mutation-types'
import veebkmService from '@/services/veebkm'


// initial state
const state = {
  all: []
}

// getters
const getters = {
  allBookmarks: state => state.all
}

// actions
const actions = {
  getAllBookmarks ({ commit }) {
    veebkmService.getBookmarks().then(bookmarks => {
      commit(types.RECEIVE_BOOKMARKS, { bookmarks })
    }).catch((error) => { console.log(error) });
  }
}

// mutations
const mutations = {
  [types.RECEIVE_BOOKMARKS] (state, { bookmarks }) {
    state.all = bookmarks
  },

}

export default {
  state,
  getters,
  actions,
  mutations
}
