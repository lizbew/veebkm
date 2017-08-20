import Vue from 'vue';

import VueResource from 'vue-resource';

Vue.use(VueResource);


Vue.http.options.root = '/api';
// Vue.http.headers.common['Authorization'] = 'Bearer ' + localStorage.getItem('id_token');

// return promise
// export const veebkmService = import('./veebkm')


import veebkm from './veebkm'

export const veebkmService = veebkm;



