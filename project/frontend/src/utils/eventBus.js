/**
 * 简单的事件总线
 * 
 * 【原理讲解】
 * 事件总线是一种组件间通信方式：
 * 1. 发布者调用 emit('事件名') 发送事件
 * 2. 订阅者调用 on('事件名', callback) 监听事件
 * 3. 当事件触发时，所有订阅者的 callback 都会执行
 * 
 * 用途：上传文件后通知侧边栏刷新存储统计
 */
import { ref } from 'vue'

const listeners = ref({})

export const eventBus = {
  /**
   * 监听事件
   */
  on(event, callback) {
    if (!listeners.value[event]) {
      listeners.value[event] = []
    }
    listeners.value[event].push(callback)
  },
  
  /**
   * 触发事件
   */
  emit(event, data) {
    if (listeners.value[event]) {
      listeners.value[event].forEach(callback => callback(data))
    }
  },
  
  /**
   * 移除监听
   */
  off(event, callback) {
    if (listeners.value[event]) {
      if (callback) {
        listeners.value[event] = listeners.value[event].filter(cb => cb !== callback)
      } else {
        delete listeners.value[event]
      }
    }
  }
}

export default eventBus
