import Vue from 'vue'

import auth from '../auth';

export default {
  getBookmarks() {
    const headers = auth.getAuthHeader();
    return Vue.http.get('api/bookmarks', { headers })
      .then((response) => {
        return response.data.bookmarks;
      });
      // .catch((error) => Promise.reject(error));
  }
}
