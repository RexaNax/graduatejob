<template>
  <div class="container">
    <div class="file">
      <div class="header">
        <el-button type="primary" @click="multipleRecycleFile()" :disabled="multipleSelectionIds.length === 0">还原</el-button>
        <el-button @click="multipleDelFile()" :disabled="multipleSelectionIds.length === 0">彻底删除</el-button>
        <el-button type="danger" @click="clearTrash()" :disabled="rowTotal === 0">清空回收站</el-button>
      </div>
      <el-divider />
      <list ref="listRef" :fileList="fileList" :editId="editId" :search-key="searchKey"
            @delFile="delFile"
            @recycle="recycleFile"
            @setMultipleSelectionIds="setMultipleSelectionIds"
      />

      <div class="page">
        <el-pagination background layout="prev, pager, next"
                       :hide-on-single-page="true"
                       :page-size="pageSize" :current-page="pageNo" :total="rowTotal"
                       @prev-click="prevPage" @next-click="nextPage" @current-change="pageChange"/>
      </div>
    </div>
  </div>

</template>
<script setup>
import {ref, shallowRef, nextTick, onMounted, watch} from 'vue'
import api from "~/api/config";
import {ElMessage, ElMessageBox} from 'element-plus'
import list from "~/views/trash/list.vue";
import eventBus from "~/utils/eventBus";


const inputKey = ref("");
const searchKey = ref("");
const listRef = ref();

const dirId = ref(0);
const editId = ref(0);
const pageNo = ref(1);
const pageSize = ref(20);
const rowTotal = ref(0);
const fileList = ref([]);
const loading = ref(false);
const multipleSelectionIds = ref([]);
const defaultPath = ref({name: "所有文件", id: 0})
const pathList = ref([defaultPath.value])

watch(dirId, (val) => {
  resetMultipleData();
})
onMounted(() => {
  getFileList();
})

const resetMultipleData = () => {
  multipleSelectionIds.value = [];
  listRef.value.resetCheckedData(multipleSelectionIds.value);
}

const selectDirPath = async (id, index) => {
  if (id === dirId.value && searchKey.value === "") {
    return;
  }
  dirId.value = id;
  pageNo.value = 1;
  if (index === -1){
    getFileDirPath(id);
  }else {
    pathList.value.splice(index + 1);
  }
  getFileList();
}

const searchFiles = () => {
  getFileList(inputKey.value);
}

const getFileList = async (key = "") => {
  loading.value = true;
  searchKey.value = key;
  let params = {
    pageNo: pageNo.value,
    pageSize: pageSize.value,
    key: key,
  };
  const result = await api.get(`/trash/list`, {params: params});
  if (result){
    fileList.value = result.data.records;
    fileList.value.forEach((r, i) => {
      r.checked = false;
    });
    rowTotal.value = Number(result.data.total);
  }
  loading.value = false;
}
const delFile = (fileId) => {
  batchDelFile([fileId]);
}
const multipleDelFile = () => {
  console.log("multipleDelFile", multipleSelectionIds.value)
  if (multipleSelectionIds.value.length === 0){
    ElMessage({
      message: '请先选择文件',
      type: 'error',
    })
    return
  }
  batchDelFile(multipleSelectionIds.value);
}
const batchDelFile = async (ids) => {
  console.log("batchDelFile", ids)
  ElMessageBox.confirm(
      '确定要删除吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    const result = await api.post(`/trash/delete`, {ids: ids.join(",")});
    if (result) {
      ElMessage({
        message: '删除成功',
        type: 'success',
      })
      getFileList();
      // 通知侧边栏刷新存储统计
      eventBus.emit('file-changed');
      multipleSelectionIds.value = [];
      listRef.value.resetCheckedData(multipleSelectionIds.value);
    }
  }).catch(() => {
    console.log("batchDelFile catch", ids)
  })
}
const recycleFile = (fileId) => {
  batchRecycleFile([fileId]);
}
const multipleRecycleFile = () => {
  console.log("multipleRecycleFile", multipleSelectionIds.value)
  if (multipleSelectionIds.value.length === 0){
    ElMessage({
      message: '请先选择文件',
      type: 'error',
    })
    return
  }
  batchRecycleFile(multipleSelectionIds.value);
}
const batchRecycleFile = async (ids) => {
  console.log("batchDelFile", ids)
  ElMessageBox.confirm(
      '确定还原文件吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    const result = await api.post(`/trash/recycle`, {ids: ids.join(",")});
    if (result) {
      ElMessage({
        message: '还原成功',
        type: 'success',
      })
      getFileList();
      // 通知侧边栏刷新存储统计
      eventBus.emit('file-changed');
      multipleSelectionIds.value = [];
      listRef.value.resetCheckedData(multipleSelectionIds.value);
    }
  }).catch(() => {
    console.log("batchDelFile catch", ids)
  })
}
const prevPage = async () => {
  console.log("prevPage", pageNo.value)
}
const nextPage = async () => {
  console.log("nextPage", pageNo.value)
}
const pageChange = async (p) => {
  console.log("pageChange", p)
  pageNo.value = p;
  resetMultipleData();
  getFileList();
}

const setMultipleSelectionIds = (ids) => {
  multipleSelectionIds.value = ids;
  console.log("setMultipleSelectionIds", multipleSelectionIds.value)
}

/**
 * 清空回收站
 */
const clearTrash = () => {
  ElMessageBox.confirm(
      '确定要清空回收站吗？此操作将彻底删除所有文件，无法恢复！',
      '警告',
      {
        confirmButtonText: '确定清空',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
  ).then(async () => {
    const result = await api.post(`/trash/clear`);
    if (result) {
      ElMessage({
        message: '回收站已清空',
        type: 'success',
      })
      getFileList();
      eventBus.emit('file-changed');
      multipleSelectionIds.value = [];
    }
  }).catch(() => {
    console.log("clearTrash cancelled")
  })
}
</script>

<style lang="scss" scoped>
.container{
  .fileType{
    width: 160px;
    float: left;
    margin-left: -20px;
    margin-top: -20px;
    margin-bottom: -20px;
  }
  .file{
    width: calc(100% - 20px);
    float: left;
    padding-left: 20px;
  }
  .ep-menu-item{
    height: 45px;
  }

  .header{
    position: relative;
    .searchInp{
      position: absolute;
      width: 300px;
      right: 0;
      top: 0;
    }
  }
  .bread{
    position: relative;
    .breadcrumb{
      width: calc(100% - 50px);
    }
    .showTypeIcon{
      position: absolute;
      right: 20px;
      top: 0;
      cursor: pointer;
    }
  }
  .addDir{
    display: inline-block;
    margin-left: 20px;
    font-size: 14px;
    cursor: pointer;
    height: 32px;
    line-height: 32px;
    padding: 0px 20px;
    border-radius: 5px;
    color: #606366;
  }
  .addDir:hover{
    background: #f5f3f3;
  }
  .ep-breadcrumb{
    margin-top: 10px;
  }
  .ep-breadcrumb__item{
    cursor: pointer;
    line-height: 20px;
  }

  .page{
    margin-top: 10px;
    margin-right: 20px;
    float: right;
  }
}
.ep-divider--horizontal{
  margin-bottom: 0;
  margin-top: 10px;
}

</style>

