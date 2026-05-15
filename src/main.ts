import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { restoreAuth } from './store/auth'
import './assets/style.css'

restoreAuth()

const app = createApp(App)
app.use(router)
app.mount('#app')