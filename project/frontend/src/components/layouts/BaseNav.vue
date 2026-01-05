<template>
  <div>
    <div class="header">
      <div class="logoDiv">
        <img :src="logo" />
        <span class="title">云文件管理系统</span>
      </div>
      <div class="header-right-div">
        <!-- 用户信息 -->
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="28" :icon="UserFilled" />
            <span class="username">{{ userInfo.nickname || userInfo.username || '用户' }}</span>
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
/**
 * 顶部导航栏
 * 
 * 【原理讲解】
 * 1. 从 localStorage 读取用户信息
 * 2. 退出登录：清除 localStorage 中的 token 和 user
 * 3. 跳转到登录页
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, ArrowDown, SwitchButton } from '@element-plus/icons-vue'
import logo from '../../assets/logo.png'

const router = useRouter()

// 用户信息
const userInfo = ref({})

// 获取用户信息
onMounted(() => {
  const user = localStorage.getItem('user')
  if (user) {
    userInfo.value = JSON.parse(user)
  }
})

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  }
}

// 退出登录
const handleLogout = () => {
  // 清除本地存储
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  
  ElMessage.success('已退出登录')
  // 跳转到登录页
  router.push('/login')
}
</script>

<style lang="scss" scoped >
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
  cursor: pointer;
  color: #fff;
  
  .username {
    margin: 0 8px;
    font-size: 14px;
  }
}

.header ul {
  margin-top: 0px;
  list-style-type: none;
  li {
    cursor: pointer;
    height: 44px;
    line-height: 44px;
    float: left;
    color: #ffffff;
    font-family: PingFang SC;
    font-style: normal;
    font-weight: normal;
    font-size: 14px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    margin: 0px 10px 0px 10px;
    .liTxt {
      max-width: 100px;
      margin-right: 8px;
      height: 44px;
      line-height: 44px;
      float: left;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }
  }
}

.header li .liIcon {
  float: left;
  margin: 0px 5px 0px 0px;
}
.header .dropdownMenuDiv {
  height: 44px;
}
.header .dropdownMenuDiv.user {
  padding-right: 56px;
}
.header .dropdownMenuDiv:hover {
  color: #20ccc6;
  -webkit-transition: all 0.15s;
  -moz-transition: all 0.15s;
  -ms-transition: all 0.15s;
  -o-transition: all 0.15s;
  transition: all 0.15s;
}

.right-border {
  display: inline-block;
  border-left: 1px solid #edf5fc;
  text-align: center;
  height: 165px;
  margin-top: 20px;
}
</style>
