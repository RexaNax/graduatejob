<template>
  <div class="share-container">
    <div class="share-box">
      <!-- 加载中 -->
      <div v-if="loading" class="loading">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>正在获取分享信息...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error">
        <el-icon :size="60" color="#f56c6c"><CircleCloseFilled /></el-icon>
        <h2>{{ error }}</h2>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </div>
      
      <!-- 分享信息 -->
      <div v-else class="share-info">
        <div class="file-icon">
          <el-icon :size="60" color="#409eff"><Document /></el-icon>
        </div>
        <h2 class="file-name">{{ shareInfo.fileName }}</h2>
        <div class="file-meta">
          <span>文件大小：{{ formatFileSize(shareInfo.fileSize) }}</span>
          <span>访问次数：{{ shareInfo.viewCount }}</span>
        </div>
        <el-button type="primary" size="large" @click="downloadFile">
          <el-icon><Download /></el-icon>
          下载文件
        </el-button>
      </div>
      
      <div class="share-footer">
        <p>云文件管理系统 - 文件分享</p>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 分享访问页面
 * 
 * 【原理讲解】
 * 1. 从路由参数获取分享码
 * 2. 调用 /share/access/{code} 接口
 * 3. 后端验证分享有效性，返回文件信息
 * 4. 用户点击下载，打开下载链接
 * 
 * 注意：此页面不需要登录
 */
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Loading, CircleCloseFilled, Document, Download } from '@element-plus/icons-vue'
import api from '~/api/config'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const error = ref('')
const shareInfo = ref({})

// 获取分享信息
const getShareInfo = async () => {
  const code = route.params.code
  if (!code) {
    error.value = '分享码无效'
    loading.value = false
    return
  }
  
  try {
    const result = await api.get(`/share/access/${code}`)
    if (result && result.data) {
      shareInfo.value = result.data
    }
  } catch (e) {
    if (e.response && e.response.data) {
      error.value = e.response.data.msg || '分享链接无效或已过期'
    } else {
      error.value = '获取分享信息失败'
    }
  } finally {
    loading.value = false
  }
}

// 下载文件
const downloadFile = () => {
  if (shareInfo.value.downloadUrl) {
    window.open(shareInfo.value.downloadUrl)
  }
}

// 格式化文件大小
const formatFileSize = (size) => {
  if (!size) return '-'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let i = 0
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(1) + ' ' + units[i]
}

// 返回首页
const goHome = () => {
  router.push('/')
}

onMounted(() => {
  getShareInfo()
})
</script>

<style lang="scss" scoped>
.share-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.share-box {
  width: 450px;
  padding: 50px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  text-align: center;
}

.loading {
  p {
    margin-top: 20px;
    color: #666;
  }
}

.error {
  h2 {
    margin: 20px 0;
    color: #666;
    font-size: 18px;
  }
}

.share-info {
  .file-icon {
    margin-bottom: 20px;
  }
  
  .file-name {
    margin: 0 0 15px 0;
    font-size: 20px;
    color: #333;
    word-break: break-all;
  }
  
  .file-meta {
    margin-bottom: 30px;
    color: #999;
    font-size: 14px;
    
    span {
      margin: 0 10px;
    }
  }
}

.share-footer {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  
  p {
    margin: 0;
    color: #999;
    font-size: 12px;
  }
}
</style>
