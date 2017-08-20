const KEY_TOKEN = 'id_token'


export function  getLocalToken() {
    return localStorage.getItem(KEY_TOKEN);
  }

export function setLocalToken(token) {
    localStorage.setItem(KEY_TOKEN, token)
  }

export function removeLocalToken() {
    localStorage.removeItem(KEY_TOKEN)
  }

