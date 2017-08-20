import Vue from 'vue'

import auth from '../auth';

const API_URL = '/api'
const JWT_URL = 'token'

export default {
  getBookmarks() {
    const headers = auth.getAuthHeader();
    return Vue.http.get('bookmarks', { headers })
      .then((response) => {
        return response.data.bookmarks;
      });
      // .catch((error) => Promise.reject(error));
  },

  getToken({login, password}) {
    return Vue.http.get(JWT_URL, { headers: {login, password} })
        .then((response) => {
          const token = response.bod;
          Vue.http.headers.common['Authorization'] = 'Bearer ' + token;
          return response.body
        })
  }
}
