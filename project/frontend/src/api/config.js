/*
 * 前端请求总管
 *
 * 讲解抓手：
 * 1. request 拦截器负责“发请求前统一处理”。
 * 2. response 拦截器负责“收响应后统一处理”。
 * 3. 这样页面组件只关心业务，不需要每次都重复写 token 和错误处理。
 */

import axios from 'axios'
import qs from 'qs'
import { ElMessage, ElMessageBox } from 'element-plus'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

service.defaults.headers['Content-Type'] = 'application/x-www-form-urlencoded'
service.defaults.headers['X-Requested-With'] = 'XMLHttpRequest'

service.interceptors.request.use(
  (config) => {
    // 普通表单接口沿用后端已有接收方式，统一转成 x-www-form-urlencoded。
    if (config.headers['Content-Type'] === 'application/x-www-form-urlencoded') {
      config.data = qs.stringify(config.data)
    }

    // 给请求追加时间戳，尽量避免列表和预览类请求命中浏览器缓存。
    if (config.url.indexOf('?') > -1) {
      config.url = `${config.url}&t=${Date.now()}`
    } else {
      config.url = `${config.url}?t=${Date.now()}`
    }

    // 登录后保存的 token，会在这里被统一放到请求头里。
    const token = window.localStorage.getItem('token')
    config.headers.token = token
    return config
  },
  (error) => {
    console.log(error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response) => {
    if (response.data.code !== 0) {
      if (response.data.code === 1013) {
        const isLoginPage = window.location.hash.includes('/login')
        if (!isLoginPage) {
          ElMessageBox.alert('登录信息已失效，请重新登录', '提示', {
            confirmButtonText: '去登录',
            callback: () => {
              window.location.hash = '/login'
            },
          })
        }
      } else {
        ElMessage.error(response.data.msg)
      }

      // 业务错误继续向外 reject，页面层的 try/catch 才能拿到具体错误状态。
      const error = new Error(response.data.msg || '请求失败')
      error.response = {
        data: response.data,
        status: response.status,
      }
      return Promise.reject(error)
    }

    return response.data
  },
  (error) => {
    console.log('err', error)
    const isLoginPage = window.location.hash.includes('/login')
    if (!isLoginPage) {
      // 网络错误和业务错误分开处理，页面不会因为 undefined 响应变成空白状态。
      ElMessage.error('网络异常，请检查网络后重试')
    }
    return Promise.reject(error)
  }
)

const login = async () => {
  const result = await service.post('/getToken', {
    appId: 'YbcKSAlou6UREvkwJmTx',
    appSecret: '95NmdotNX939f4gk7vTd3cHkMd8LhBcSzPn50G8c',
  })
  console.log('login result', result)
  const token = result.data
  window.localStorage.setItem('token', token)
  window.location.reload()
}

export default service
