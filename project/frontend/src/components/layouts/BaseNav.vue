<template>
  <div>
    <div class="header">
      <div class="logoDiv">
        <img :src="logo" />
        <span class="title">云文件管理系统</span>
      </div>
      <div class="header-right-div">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="28" :icon="UserFilled" />
            <span class="username">{{ userInfo.nickname || userInfo.username || '用户' }}</span>
            <span class="role-tag">{{ userInfo.isAdmin ? '管理员' : '普通用户' }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">
                <el-icon><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import logo from '../../assets/logo.png'

const router = useRouter()
const userInfo = ref({})

onMounted(() => {
  const user = localStorage.getItem('user')
  if (user) {
    userInfo.value = JSON.parse(user)
  }
})

const handleCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  }
}

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.header {
  height: 44px;
  line-height: 44px;
  width: 100%;
  background: linear-gradient(
    145deg,
    rgba(34, 39, 48, 1) 0%,
    rgba(42, 51, 68, 1) 100%
  );
}

.header .logoDiv {
  float: left;
  height: 44px;
  display: flex;
  align-items: center;
  padding-left: 20px;

  img {
    width: 32px;
    height: 32px;
  }

  .title {
    color: #fff;
    font-size: 16px;
    margin-left: 10px;
    font-weight: 500;
  }
}

.header .header-right-div {
  float: right;
  height: 44px;
  line-height: 44px;
  margin-right: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #fff;

  .username {
    font-size: 14px;
  }

  .role-tag {
    padding: 2px 8px;
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.14);
    font-size: 12px;
    line-height: 1.4;
  }
}
</style>
