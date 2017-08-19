import router from '../router'

// const API_URL = 'http://localhost:9090/'
const API_URL = '/api'
const LOGIN_URL = API_URL + '/token'
const SIGNUP_URL = API_URL + '/users/'

export default {

  user: {
    authenticated: false
  },

  login(context, creds, redirect) {
    //context.$http.post(LOGIN_URL, creds, (data) => {
    const headers = { ...creds };
    context.$http.get(LOGIN_URL, { headers }).then((response) => {
      const data = response.body;
      localStorage.setItem('id_token', data)

      this.user.authenticated = true

      if(redirect) {
        router.go(redirect)
      }

    }, (err) => {
      debugger;
      context.error = err
    })
  },

  signup(context, creds, redirect) {
    context.$http.post(SIGNUP_URL, creds, (data) => {
      localStorage.setItem('id_token', data)

      this.user.authenticated = true

      if(redirect) {
        //router.go(redirect)
        debugger;
        router.push({name:redirect })
      }

    }).error((err) => {
      context.error = err
    })
  },

  logout() {
    localStorage.removeItem('id_token')
    this.user.authenticated = false
  },

  checkAuth() {
    var jwt = localStorage.getItem('id_token')
    if(jwt) {
      this.user.authenticated = true
    }
    else {
      this.user.authenticated = false
    }
  },


  getAuthHeader() {
    return {
      'Authorization': 'Bearer ' + localStorage.getItem('id_token')
    }
  }
}
