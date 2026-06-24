<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <img :src="logo" class="logo" alt="logo" />
        <h2>云文件管理系统</h2>
      </div>

      <div class="login-tip">
        <div>`admin / 123456`：管理员，可查看全部数据</div>
        <div>`demo / 123456`：演示用户，只能查看自己的空间</div>
      </div>

      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules">
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
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>基于容器技术的云文件管理系统</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '~/api/config'
import logo from '~/assets/logo.png'

const router = useRouter()
const loading = ref(false)
const loginFormRef = ref()

const loginForm = reactive({
  username: '',
  password: '',
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  loading.value = true
  try {
    const result = await api.post('/user/login', null, {
      params: {
        username: loginForm.username,
        password: loginForm.password,
      },
    })

    if (result?.data) {
      localStorage.setItem('token', result.data.token)
      localStorage.setItem('user', JSON.stringify(result.data.user))
      ElMessage.success('登录成功')
      router.push('/index')
    }
  } catch (error) {
    console.error('登录失败', error)
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
  margin-bottom: 24px;

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

.login-tip {
  margin-bottom: 18px;
  padding: 12px 14px;
  border-radius: 8px;
  background: #f4f7fb;
  color: #5d6778;
  font-size: 13px;
  line-height: 1.8;
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
