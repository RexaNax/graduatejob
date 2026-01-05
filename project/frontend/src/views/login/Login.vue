<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <img :src="logo" class="logo" />
        <h2>云文件管理系统</h2>
      </div>
      
      <el-tabs v-model="activeTab" class="login-tabs">
        <!-- 登录表单 -->
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef">
            <el-form-item prop="username">
              <el-input 
                v-model="loginForm.username" 
                placeholder="请输入用户名"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="请输入密码"
                prefix-icon="Lock"
                size="large"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                class="login-btn" 
                size="large"
                :loading="loading"
                @click="handleLogin"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <!-- 注册表单 -->
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef">
            <el-form-item prop="username">
              <el-input 
                v-model="registerForm.username" 
                placeholder="请输入用户名（3-20位）"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input 
                v-model="registerForm.password" 
                type="password" 
                placeholder="请输入密码（至少6位）"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-form-item prop="nickname">
              <el-input 
                v-model="registerForm.nickname" 
                placeholder="请输入昵称（可选）"
                prefix-icon="UserFilled"
                size="large"
              />
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                class="login-btn" 
                size="large"
                :loading="loading"
                @click="handleRegister"
              >
                注 册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      
      <div class="login-footer">
        <p>基于容器的云文件管理系统</p>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 登录页面
 * 
 * 【原理讲解】
 * 1. 用户输入用户名和密码
 * 2. 调用 /user/login 接口
 * 3. 后端验证密码（BCrypt 比对）
 * 4. 验证成功返回 JWT Token
 * 5. 前端存储 Token 到 localStorage
 * 6. 后续请求携带 Token（在 axios 拦截器中自动添加）
 */
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '~/api/config'
import logo from '~/assets/logo.png'

const router = useRouter()
const activeTab = ref('login')
const loading = ref(false)
const loginFormRef = ref()
const registerFormRef = ref()

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

// 注册表单
const registerForm = reactive({
  username: '',
  password: '',
  nickname: ''
})

// 登录校验规则
const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 注册校验规则
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    // 调用登录接口
    const result = await api.post('/user/login', null, {
      params: {
        username: loginForm.username,
        password: loginForm.password
      }
    })
    
    if (result && result.data) {
      // 存储 Token 和用户信息
      localStorage.setItem('token', result.data.token)
      localStorage.setItem('user', JSON.stringify(result.data.user))
      
      ElMessage.success('登录成功')
      // 跳转到首页
      router.push('/index')
    }
  } catch (e) {
    console.error('登录失败', e)
  } finally {
    loading.value = false
  }
}

// 处理注册
const handleRegister = async () => {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    const result = await api.post('/user/register', null, {
      params: {
        username: registerForm.username,
        password: registerForm.password,
        nickname: registerForm.nickname || undefined
      }
    })
    
    if (result && result.data) {
      // 注册成功自动登录
      localStorage.setItem('token', result.data.token)
      localStorage.setItem('user', JSON.stringify(result.data.user))
      
      ElMessage.success('注册成功')
      router.push('/index')
    }
  } catch (e) {
    console.error('注册失败', e)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
  
  .logo {
    width: 60px;
    height: 60px;
    margin-bottom: 10px;
  }
  
  h2 {
    margin: 0;
    color: #333;
    font-size: 24px;
  }
}

.login-tabs {
  :deep(.el-tabs__nav-wrap::after) {
    display: none;
  }
  
  :deep(.el-tabs__item) {
    font-size: 16px;
  }
}

.login-btn {
  width: 100%;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  
  p {
    color: #999;
    font-size: 12px;
    margin: 0;
  }
}
</style>
