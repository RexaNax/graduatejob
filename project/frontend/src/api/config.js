import axios from 'axios'
import qs from 'qs'
import { ElMessage, ElMessageBox } from 'element-plus'


// 创建一个axios实例
const service = axios.create({
    baseURL: "/api",
    // 请求超时时间
    timeout: 10000,
})
service.defaults.headers['Content-Type'] = 'application/x-www-form-urlencoded';
service.defaults.headers['X-Requested-With'] = 'XMLHttpRequest';
// 添加请求拦截器
service.interceptors.request.use(config => {
      if (config.headers['Content-Type'] === 'application/x-www-form-urlencoded'){
          config.data = qs.stringify(config.data)
      }
      //请求缓存处理,
      if(config.url.indexOf('?')>-1){
        config.url = config.url + '&t='+new Date().getTime();
      }else{
        config.url = config.url + '?t='+new Date().getTime();
      }
      const token = window.localStorage.getItem("token");
      config.headers['token'] = token;
      return config
  },
  err => {
    console.log(err)
  }
)

//响应拦截
service.interceptors.response.use(
  response => {
      if (response.data.code !== 0){
          if (response.data.code === 1013){
              // 检查是否在登录页，登录页不弹窗
              const isLoginPage = window.location.hash.includes('/login')
              if (!isLoginPage) {
                  ElMessageBox.alert('登陆信息已失效，请重新登陆', '提示', {
                      confirmButtonText: '去登录',
                      callback: (action) => {
                          // 跳转到登录页
                          window.location.hash = '/login'
                      },
                  })
              }
          } else {
              ElMessage.error(response.data.msg);
          }
          return;
      }
    return response.data
  },
  err => {
      console.log("err", err);
      // 检查是否在登录页，登录页不弹网络错误
      const isLoginPage = window.location.hash.includes('/login')
      if (!isLoginPage) {
          ElMessage.error("网络异常，请检查网络后重试");
      }
  }
)

const login = async () => {
    const result = await service.post("/getToken", {appId: "YbcKSAlou6UREvkwJmTx", appSecret: "95NmdotNX939f4gk7vTd3cHkMd8LhBcSzPn50G8c"})
    console.log("login result", result);
    const token = result.data;
    window.localStorage.setItem("token", token);
    window.location.reload();
}

export default service
