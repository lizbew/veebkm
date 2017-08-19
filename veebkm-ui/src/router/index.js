import Vue from 'vue';
import Router from 'vue-router';
// import Hello from '@/components/Hello';
import BookmarkList from '@/components/BookmarkList';

import Signup from '@/components/Signup.vue'
import Login from '@/components/Login.vue'

Vue.use(Router);

export default new Router({
  routes: [
    {
      path: '/',
      name: 'home',
      component: BookmarkList,
    },
    {
      path: '/login',
      component: Login,
    },
    {
      path: '/signup',
      component: Signup,
    },
  ],
});

/*
router.redirect({
  '*': '/home'
})
*/
