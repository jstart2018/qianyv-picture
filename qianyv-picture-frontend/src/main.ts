// 导入全局样式
import './assets/main.css'
// 导入 Ant Design Vue 样式
import 'ant-design-vue/dist/reset.css'
// 导入全局 CSS 变量和动画
import './styles/base/variables.css'
import './styles/base/animations.css'
// 导入通用工具类
import './styles/mixins/common.css'
// 导入组件样式
import './styles/components/loading.css'
import './styles/components/button.css'
import './styles/components/card.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)

app.mount('#app')
