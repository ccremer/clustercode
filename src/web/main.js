// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.

global.jQuery = global.$ = require("jquery");
require("bootstrap/dist/css/bootstrap.min.css");
require("bootstrap/dist/js/bootstrap.min");
require("font-awesome/css/font-awesome.min.css");
require("metismenu/dist/metisMenu.min");
require("metismenu/dist/metisMenu.min.css");
require("startbootstrap-sb-admin-2/dist/js/sb-admin-2.min");
require("startbootstrap-sb-admin-2/dist/css/sb-admin-2.min.css");
require("../css/clustercode.css");
require("../js/clustercode");

import Vue from 'vue'
import App from './App'
import router from '../router/index'
import { store } from "../store/state"

Vue.config.productionTip = false;

/* eslint-disable no-new */
new Vue({
    el: 'app',
    router,
    store,
    template: '<app/>',
    components: {
        App,
    }
});
