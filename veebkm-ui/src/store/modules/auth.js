import * as types from '../mutation-types'
import { getLocalToken, setLocalToken, removeLocalToken} from '../../utils/auth'
import { veebkmService } from '@/services'

import router from '@/router';

// initial state
const state = {
  authenticated: false,
  id_token: getLocalToken() || null,
  err: null,
}


// actions
const actions = {
  getToken({ commit }, creds) {
    veebkmService.getToken(creds).then(id_token => {
      setLocalToken(id_token)
      commit(types.SET_ID_TOKEN, { id_token })

      router.push({path: '/'})
    }).catch(err => {
      commit(types.GET_TOEKN_FAILED, err)
    })
  }
}

// mutations
const mutations = {
  [types.SET_ID_TOKEN] (state, { id_token }) {
    state.id_token = id_token
    state.authenticated = true
  },

  [types.GET_TOEKN_FAILED] (state, {err}) {
    state.id_token = null
    state.authenticated = false
    state.err = err
  }
}

export default {
  state,
  actions,
  mutations,
}
