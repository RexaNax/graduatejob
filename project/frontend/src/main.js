import { createApp } from "vue";
import App from "./App.vue";
import router from './router'
import "~/styles/index.scss";
import "uno.css";
import api from "../src/api/config"

// If you want to use ElMessage, import it.
import "element-plus/theme-chalk/src/message.scss";
import "element-plus/theme-chalk/src/message-box.scss";
import '/src/assets/iconfont/iconfont.js'


const app = createApp(App);
app.config.globalProperties.api = api;

// app.use(ElementPlus);
app.use(router).mount("#app");
