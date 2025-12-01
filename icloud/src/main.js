import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import axios from 'axios'
import VueAxios from 'vue-axios'
import reset from './assets/css/reset.css'
import './assets/css/iconfont/iconfont.css'
import './assets/css/iconfont/iconfont.js'
import global_msg from './global/global.js'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';

Vue.prototype.$global_msg = global_msg;
Vue.config.productionTip = false
Vue.use(ElementUI);
Vue.use(VueAxios, axios)
Vue.prototype.$axios = axios;

// ========== 请求拦截器：自动携带 satoken ==========
axios.interceptors.request.use(
    config => {
        const token = sessionStorage.getItem('satoken');
        if (token) {
            config.headers['satoken'] = token; // Sa-Token 默认读取的 header 字段
        }

        // 如果传入的是普通对象（如 {account: 'xxx', password: 'yyy'}），自动转为 URLSearchParams
        if (config.data && typeof config.data === 'object' && !(config.data instanceof FormData)) {
            config.data = new URLSearchParams(config.data).toString();
            config.headers['Content-Type'] = 'application/x-www-form-urlencoded';
        }

        return config;
    },
    error => {
        return Promise.reject(error);
    }
);
// ==========================================

// 超时设置
axios.defaults.timeout = 8000;

// 注意：不要设置全局 Content-Type 为 application/json！
// 因为我们现在要发 form 数据，由拦截器动态决定 Content-Type

new Vue({
    router,
    store,
    render: h => h(App)
}).$mount('#app')