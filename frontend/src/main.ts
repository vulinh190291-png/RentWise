import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/global.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)

// 注册 Pinia
app.use(createPinia())

// 注册 Vue Router
app.use(router)

// 注册 Element Plus
app.use(ElementPlus)

// 挂载应用
app.mount('#app')