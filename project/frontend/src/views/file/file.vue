<template>
  <div class="container">
    <div class="fileType">
      <el-menu
          ref="fileTypeMenuRef"
          :default-active="fileType"
          class="el-menu-vertical"
          style="height: calc(100vh - 45px)"
          @select="selectFileType"
      >
        <el-menu-item index="0">
          <el-icon><Tickets /></el-icon>
          <template #title>所有文件</template>
        </el-menu-item>
        <el-menu-item index="1">
          <el-icon></el-icon>
          <template #title>视频</template>
        </el-menu-item>
        <el-menu-item index="2">
          <el-icon></el-icon>
          <template #title>音频</template>
        </el-menu-item>
        <el-menu-item index="3">
          <el-icon></el-icon>
          <template #title>文档</template>
        </el-menu-item>
        <el-menu-item index="4">
          <el-icon></el-icon>
          <template #title>图片</template>
        </el-menu-item>
        <el-menu-item index="9">
          <el-icon></el-icon>
          <template #title>其他</template>
        </el-menu-item>
      </el-menu>
    </div>
    <div class="file">
      <div class="header">
        <el-button type="primary" @click="upload()">上传文件</el-button>
        <el-button type="warning" plain @click="dirAdd()">新建文件夹</el-button>
        <el-button @click="toBatchDownload()" :disabled="multipleSelectionIds.length === 0 || multipleSelectionExistDir">下载</el-button>
        <el-button @click="multipletToMove()" :disabled="multipleSelectionIds.length === 0">移动</el-button>
        <el-button @click="multipleDelFile()" :disabled="multipleSelectionIds.length === 0">删除</el-button>
        <el-input class="searchInp" placeholder="请输文件名搜索" v-model="inputKey" auto-complete="off" @keyup.enter.native="searchFiles" clearable>
          <template #append>
            <el-button :icon="Search" @click="searchFiles"/>
          </template>
        </el-input>
      </div>
      <el-divider />
      <div class="bread">
        <div v-if="searchKey.length > 0">
          <el-breadcrumb class="breadcrumb" separator=" / ">
            <el-breadcrumb-item @click="selectDirPath(0, 0)">所有文件</el-breadcrumb-item>
            <el-breadcrumb-item>“{{searchKey}}”的搜索结果</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div v-else>
          <el-breadcrumb class="breadcrumb" separator=" / ">
            <el-breadcrumb-item v-for="(item, index) in pathList" @click="selectDirPath(item.id, index)">{{item.name}}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <el-icon class="icon showTypeIcon" @click="changeShowType">
          <icon-grid v-if="showType === 'list'" />
          <icon-operation v-if="showType === 'grid'" />
        </el-icon>
      </div>
      <el-divider />
      <grid v-if="showType === 'grid'" ref="gridRef" :fileList="fileList" :editId="editId"
            @selectFile="selectFile"
            @delFile="delFile"
            @toEditFile="toEditFile"
            @toDownload="toDownload"
            @toMove="toMove"
            @rename="rename"
            @setMultipleSelectionIds="setMultipleSelectionIds"
            @setMultipleSelectionExistDir="setMultipleSelectionExistDir"
      />
      <list v-if="showType === 'list'" ref="listRef" :fileList="fileList" :editId="editId" :search-key="searchKey" :file-type="fileType"
            @selectFile="selectFile"
            @delFile="delFile"
            @toEditFile="toEditFile"
            @toDownload="toDownload"
            @toMove="toMove"
            @rename="rename"
            @setMultipleSelectionIds="setMultipleSelectionIds"
            @setMultipleSelectionExistDir="setMultipleSelectionExistDir"
            @selectDirPath="selectDirPath"
      />

      <div class="page">
        <el-pagination background layout="prev, pager, next"
                       :hide-on-single-page="true"
                       :page-size="pageSize" :current-page="pageNo" :total="rowTotal"
                       @prev-click="prevPage" @next-click="nextPage" @current-change="pageChange"/>
      </div>
    </div>
  </div>


  <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      class="dialog"
      width="680px"
      destroy-on-close
      @close="close()"
  >
    <detail @delFile="delFile" :fileId="dialog.file.id" :fileMd5="dialog.file.md5"></detail>
  </el-dialog>

  <el-dialog
      v-model="uploadDialog.visible"
      title="上传文件"
      class="uploadDialog"
      width="680px"
      destroy-on-close
      @close="closeUpload()"
  >
    <Upload :dirId="dirId" @selectFile="selectFile"/>
  </el-dialog>

  <el-dialog
      v-model="moveDialog.visible"
      title="移动文件"
      class="moveDialog"
      width="680px"
      destroy-on-close
  >
    <move @setMoveTargetDirId="setMoveTargetDirId"/>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="closeMove">取消</el-button>
        <el-button type="primary" @click="moveFile()" :disabled="moveTargetDirId === -1">确定</el-button>
      </div>
    </template>
  </el-dialog>

</template>
<script setup>
import {ref, shallowRef, nextTick, onMounted, watch} from 'vue'
import api from "~/api/config";
import eventBus from "~/utils/eventBus";
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  Grid as IconGrid,
  Operation as IconOperation,
  Search,
  Tickets
} from "@element-plus/icons-vue";

import detail from "~/views/file/detail.vue";
import grid from "~/views/file/grid.vue";
import list from "~/views/file/list.vue";
import move from "~/views/file/move.vue";


//显示类型，0list，1grid
const showType = ref("list");
const inputKey = ref("");
const fileType = ref("0");
const searchKey = ref("");
const gridRef = ref();
const listRef = ref();
const dialog = ref({
  visible: false,
  title: "",
  activeName: "first",
  fileId: 0,
  file: {},
  thumList: [],
  showThumIndex: 1,
  showThumList: []
})
const uploadDialog = ref({
  visible: false,
})
const moveDialog = ref({
  visible: false,
})
const dirId = ref(0);
const editId = ref(0);
const pageNo = ref(1);
const pageSize = ref(20);
const rowTotal = ref(0);
const fileList = ref([]);
const loading = ref(false);
const movefileIds = ref([]);
const moveTargetDirId = ref(-1);
const multipleSelectionIds = ref([]);
const multipleSelectionExistDir = ref(false);
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
  multipleSelectionExistDir.value = false;
  movefileIds.value = [];
  moveTargetDirId.value = -1;
  if (showType.value === "grid"){
    gridRef.value.resetCheckedData(multipleSelectionIds.value);
  }else {
    listRef.value.resetCheckedData(multipleSelectionIds.value);
  }
}


const selectFileType = (index) => {
  fileType.value = index;
  dirId.value = 0;
  pageNo.value = 1;
  pathList.value = [].concat([defaultPath.value]);
  resetMultipleData();
  getFileList();
}

const selectFile = async (file, reSearch = false) => {
  if (file.isDir === 1) {
    dirId.value = file.id;
    pageNo.value = 1;
    pathList.value.push({name: file.name, id: file.id})
    getFileList();
    return;
  }
  dialog.value.fileId = file.id;
  dialog.value.file = file;
  dialog.value.title = file.name;
  dialog.value.visible = true;
  dialog.value.showThumIndex = 1;
  dialog.value.activeName = "first";
  getThumList(file.md5);
  if (reSearch) {
    getFileDetail(file.id);
  }
}
const selectDirPath = async (id, index, resetFileType = false) => {
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
  if (resetFileType) {
    fileType.value = 0;
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
    dirId: dirId.value,
    pageNo: pageNo.value,
    pageSize: pageSize.value,
    key: key,
    fileType: fileType.value,
  };
  const result = await api.get(`/file/list`, {params: params});
  if (result){
    fileList.value = result.data.records;
    fileList.value.forEach((r, i) => {
      r.checked = false;
    });
    rowTotal.value = Number(result.data.total);
  }
  loading.value = false;
}
const getFileDirPath = async (dirId) => {
  const result = await api.get(`/file/filePathList`, {params: {dirId: dirId}});
  if (result){
    pathList.value = [defaultPath.value].concat(result.data);
  }
}
const getFileDetail = async (fileId) => {
  const result = await api.get(`/file/detail/${fileId}`);
  if (dialog.value.file.id !== fileId) {
    return;
  }
  dialog.value.file = result.data;
  if (dialog.value.visible && (dialog.value.file.transStatus === 0 || dialog.value.file.transStatus === 2)) {
    setTimeout(getFileDetail, 3000, fileId);
  }
}
const getThumList = async (md5) => {
  const result = await api.get(`/file/thumList`, {params: {md5: md5}});
  dialog.value.thumList = result.data;
  changeThumList();
}

const dirAdd = async () => {
  const result = await api.post(`/file/dirAdd`, {dirId: dirId.value, name: "新建文件夹"});
  if (result){
    fileList.value.unshift(result.data);
    editId.value = result.data.id;
    if (showType.value === "grid"){
      gridRef.value.toEditName(result.data.name);
    }else {
      listRef.value.toEditName(result.data.name);
    }
  }

}
const toEditFile = async (fileId) => {
  editId.value = fileId;
}
const rename = async (row, fileName) => {
  if (editId.value === 0) {
    return;
  }
  if (fileName === "") {
    ElMessage({
      message: '名称不能为空',
      type: 'error',
    })
    return;
  }
  const result = await api.post(`/file/rename`, {id: editId.value, name: fileName});
  if (result) {
    row.name = fileName;
    editId.value = 0;
  }
}
const moveFile = async () => {
  if(movefileIds.value.length === 0 || moveTargetDirId.value === -1){
    return;
  }
  const result = await api.post(`/file/move`, {fileIds: movefileIds.value.join(","), dirId: moveTargetDirId.value});
  if (result){
    ElMessage({
      message: '文件移动成功',
      type: 'success',
    })
    getFileList();
    multipleSelectionIds.value = [];
    if (showType.value === "grid"){
      gridRef.value.resetCheckedData(multipleSelectionIds.value);
    }
    closeMove();
  }
}
const delFile = (fileId) => {
  batchDelFile([fileId]);
}
const multipleDelFile = () => {
  if (multipleSelectionIds.value.length === 0){
    ElMessage({
      message: '请先选择文件',
      type: 'error',
    })
    return
  }
  batchDelFile(multipleSelectionIds.value);
}
const batchDelFile = async (fileIds) => {
  ElMessageBox.confirm(
      '确定要删除吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    const result = await api.post(`/file/delete`, {fileIds: fileIds.join(",")});
    if (result) {
      ElMessage({
        message: '删除成功',
        type: 'success',
      })
      getFileList();
      // 通知侧边栏刷新存储统计
      eventBus.emit('file-changed');
      multipleSelectionIds.value = [];
      if (showType.value === "grid"){
        gridRef.value.resetCheckedData(multipleSelectionIds.value);
      }else {
        listRef.value.resetCheckedData(multipleSelectionIds.value);
      }
      close();
    }
  }).catch(() => {
  })
}
const toMove = (fileId) => {
  console.log("toMove fileId", fileId);
  movefileIds.value = [fileId];
  moveTargetDirId.value = -1;
  moveDialog.value.visible = true;
}
const multipletToMove = () => {
  movefileIds.value = multipleSelectionIds.value;
  moveTargetDirId.value = -1;
  moveDialog.value.visible = true;
}
const setMoveTargetDirId = (dirId) => {
  moveTargetDirId.value = dirId;
}
const toDownload = (fileId) => {
  multipleSelectionIds.value = [fileId];
  toBatchDownload();
}
const toBatchDownload = async () => {
  const result = await api.post(`/file/getDownloadUrl`, {fileIds: multipleSelectionIds.value.join(",")});
  if (result) {
    for (let i in result.data){
      window.open(result.data[i]);
    }
  }
}
const changeThumList = async () => {
  dialog.value.showThumIndex = dialog.value.showThumIndex === 1 ? 0 : 1;
  dialog.value.showThumList = dialog.value.thumList.slice(dialog.value.showThumIndex * 5, dialog.value.showThumIndex * 5 + 5)
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

const close = () => {
  dialog.value.visible = false;
}
const closeMove = () => {
  moveDialog.value.visible = false;
}
const closeUpload = () => {
  uploadDialog.value.visible = false;
  getFileList();
  // 通知侧边栏刷新存储统计
  eventBus.emit('file-changed');
}
const upload = () => {
  uploadDialog.value.visible = true;
}

const changeShowType = () => {
  if (showType.value === "list"){
    showType.value = "grid";
    nextTick(() => {
      gridRef.value.resetCheckedData(multipleSelectionIds.value);
    })
  }else {
    showType.value = "list";
    nextTick(() => {
      listRef.value.resetCheckedData(multipleSelectionIds.value);
    })
  }
}
const setMultipleSelectionIds = (ids) => {
  multipleSelectionIds.value = ids;
  console.log("setMultipleSelectionIds", multipleSelectionIds.value)
}
const setMultipleSelectionExistDir = (exist) => {
  multipleSelectionExistDir.value = exist;
  console.log("multipleSelectionExistDir", multipleSelectionExistDir.value)

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
    width: calc(100% - 160px);
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

