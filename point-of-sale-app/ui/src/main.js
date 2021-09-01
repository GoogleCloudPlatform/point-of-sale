import Vue from 'vue'
import Vuex from 'vuex'
import App from './App.vue'
import BootstrapVue from "bootstrap-vue"
import AudioRecorder from 'vue-audio-recorder'
import titleMixin from './mixins/titleMixin'
import "bootstrap/dist/css/bootstrap.min.css"
import "bootstrap-vue/dist/bootstrap-vue.css"
import '@fortawesome/fontawesome-free/css/all.css'
import '@fortawesome/fontawesome-free/js/all.js'
import './css/app.css'

Vue.use(Vuex)
Vue.use(BootstrapVue)
Vue.use(AudioRecorder)
Vue.mixin(titleMixin)

Vue.config.productionTip = false

new Vue({
  render: h => h(App),
}).$mount('#app')
