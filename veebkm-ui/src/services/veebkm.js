import Vue from 'vue'

import auth from '../auth';

const API_URL = '/api'
const JWT_URL = 'token'

export default {
  getToken({login, password}) {
    return Vue.http.get(JWT_URL, { headers: {login, password} })
        .then((response) => {
          const token = response.bod;
          Vue.http.headers.common['Authorization'] = 'Bearer ' + token;
          return response.body
        })
  },

  getBookmarks() {
    const headers = auth.getAuthHeader();
    return Vue.http.get('bookmarks', { headers })
      .then((response) => {
        return response.data.bookmarks;
      });
      // .catch((error) => Promise.reject(error));
  },

  addBookmark(title, url) {
    const data = {
      title,
      url,
    };
    return Vue.http.post('bookmarks', data)
      .then(response => response.data);
  }
}
