<template>
  <div class="side-container">
    <el-menu
        default-active="2"
        class="el-menu-vertical"
        @open="handleOpen"
        @close="handleClose"
        :router="true"
    >
      <el-menu-item index="/index">
        <el-icon><icon-menu /></el-icon>
        <span>文件管理</span>
      </el-menu-item>
      <el-menu-item index="/transTemplate">
        <el-icon><icon-switch /></el-icon>
        <span>转码配置</span>
      </el-menu-item>
      <el-menu-item index="/transProgress">
        <el-icon><icon-finished /></el-icon>
        <span>转码进度</span>
      </el-menu-item>
      <el-menu-item index="/trash">
        <el-icon><icon-delete /></el-icon>
        <span>回收站</span>
      </el-menu-item>
    </el-menu>
    
    <!-- 存储空间统计 -->
    <div class="storage-stats">
      <div class="storage-title">
        <el-icon><icon-folder /></el-icon>
        <span>存储空间</span>
      </div>
      <el-progress 
        :percentage="storageStats.usedPercent" 
        :stroke-width="8"
        :color="progressColor"
      />
      <div class="storage-info">
        {{ storageStats.usedSizeFormat }} / {{ storageStats.maxSizeFormat }}
      </div>
      <div class="storage-count">
        共 {{ storageStats.fileCount }} 个文件
      </div>
    </div>
  </div>
</template>

<script lang="js" setup>
import { ref, onMounted, onUnmounted, computed } from "vue";
import api from "~/api/config";
import eventBus from "~/utils/eventBus";
import {
  Menu as IconMenu,
  Switch as IconSwitch,
  Finished as IconFinished,
  Delete as IconDelete,
  Folder as IconFolder,
} from "@element-plus/icons-vue";

const isCollapse = ref(true);

// 存储空间统计数据
const storageStats = ref({
  usedSize: 0,
  maxSize: 0,
  usedPercent: 0,
  fileCount: 0,
  usedSizeFormat: '0 B',
  maxSizeFormat: '10 GB'
});

// 进度条颜色：根据使用率变化
// 原理：< 60% 绿色，60-80% 橙色，> 80% 红色
const progressColor = computed(() => {
  const percent = storageStats.value.usedPercent;
  if (percent < 60) return '#67c23a';
  if (percent < 80) return '#e6a23c';
  return '#f56c6c';
});

// 获取存储统计
const getStorageStats = async () => {
  const token = localStorage.getItem('token');
  if (!token) return;
  
  try {
    const result = await api.get('/file/storageStats');
    if (result && result.data) {
      storageStats.value = result.data;
    }
  } catch (e) {
    console.error('获取存储统计失败', e);
  }
};

const handleOpen = (key, keyPath) => {
  console.log(key, keyPath);
};
const handleClose = (key, keyPath) => {
  console.log(key, keyPath);
};

// 页面加载时获取统计
onMounted(() => {
  getStorageStats();
  // 监听文件变化事件，实时刷新存储统计
  eventBus.on('file-changed', getStorageStats);
});

// 组件卸载时移除监听
onUnmounted(() => {
  eventBus.off('file-changed', getStorageStats);
});
</script>

<style lang="scss" scoped>
.side-container {
  height: calc(100vh - 44px);
  display: flex;
  flex-direction: column;
}

.el-menu-vertical {
  flex: 1;
  border-right: none;
}

.storage-stats {
  padding: 16px;
  border-top: 1px solid #e4e7ed;
  background: #fafafa;
  
  .storage-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #606266;
    margin-bottom: 10px;
  }
  
  .storage-info {
    font-size: 12px;
    color: #909399;
    margin-top: 8px;
  }
  
  .storage-count {
    font-size: 11px;
    color: #c0c4cc;
    margin-top: 4px;
  }
}
</style>
