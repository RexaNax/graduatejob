<template>
  <div class="list" v-loading="props.loading">
    <el-table class="table" :data="props.fileList"
              v-if="props.fileList.length > 0"
              ref="tableRef"
              table-layout="auto"
              @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="30" />
      <el-table-column fixed prop="name" label="文件名称">
        <template #default="scope">
          <span class="fileName">
            <span class="icon" v-html="genFileIcon(scope.row)"></span>
            {{scope.row.fileName}}
          </span>

        </template>
      </el-table-column>
      <el-table-column label="大小" width="120" >
        <template #default="scope">
          {{scope.row.isDir === 1 ? "-" : calcFileSize(scope.row.fileSize)}}
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="所在目录" width="180" v-if="props.searchKey.length > 0 || props.fileType > 0">
        <template #default="scope">
          <el-link @click="selectDirPath(scope.row.dirId)">{{scope.row.dirName}}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="删除时间" width="180" />
      <el-table-column label="操作" width="80">
        <template #default="scope">
          <el-dropdown @command="(code) => fileOperate(code, scope.row)" v-if="editId === 0">
              <span class="el-dropdown-link">
                 <el-icon class="icon">
                   <icon-more-filled />
                 </el-icon>
              </span>
            <template #dropdown>
              <span>
                <el-dropdown-menu>
                  <el-dropdown-item command="recycle">还原</el-dropdown-item>
                  <el-dropdown-item command="delete">彻底删除</el-dropdown-item>
                </el-dropdown-menu>
              </span>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <el-empty description="暂无数据" v-if="fileList.length === 0" />
  </div>
</template>
<script setup>
import {nextTick, ref, shallowRef} from "vue";
import {ElMessage, ElMessageBox} from "element-plus";
import api from "~/api/config";

import {
  VideoCamera as IconVideoCamera,
  Document as IconDocument,
  FolderOpened as IconFolderOpened,
  Headset as IconHeadset,
  Picture as IconPicture,
  MoreFilled as IconMoreFilled,
  Warning as IconWarning,
} from "@element-plus/icons-vue";

const emits = defineEmits();

const props = defineProps(["fileList", "loading", "editId", "searchKey", "fileType"])
const tableRef = ref();
const multipleSelectionIds = ref([])

const handleSelectionChange = (rows) => {
  let multipleSelectionIds = [];
  rows.forEach((r, i) => {
    if (r && r.id > 0) {
      multipleSelectionIds.push(r.id);
    }
  });
  props.fileList.forEach((r, i) => {
    r.checked = multipleSelectionIds.includes(r.id);
  })
  emits('setMultipleSelectionIds', multipleSelectionIds)
}
const selectDirPath = (dirId) => {
  emits("selectDirPath", dirId, -1, true)
}
const delFile = (fileId) => {
  emits("delFile", fileId);
}
const recycle = (fileId) => {
  emits("recycle", fileId);
}

const fileOperate = (code, item) => {
  if (code === "recycle"){
    recycle(item.id);
  }else if (code === "delete"){
    delFile(item.id);
  }
}
const calcFileSize = (size) => {
  if (size === undefined) {
    return "-";
  }
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  for (var i = 0; size >= 1024 && i < units.length - 1; i++) {
    size /= 1024;
  }
  return Number(size).toFixed(1) + " " + units[i];
}
const resetCheckedData = (selectionIds) => {
  // selectAllChecked.value = selectionIds.length === props.fileList.length;
  multipleSelectionIds.value = selectionIds;
  props.fileList.forEach((r, i) => {
    multipleSelectionIds.value.forEach((id) => {
      if (r.id === id){
        tableRef.value.toggleRowSelection(r, true)
      }
    })
  })
}
const genFileIcon = (row) => {
  if (row.isDir === 1){
    return '<svg aria-hidden="true"><use xlink:href="#icon-content-courses"></use></svg>';
  }
  let icon = "icon-big-";
  if (row.suffix === ".pdf"){
    icon += "pdf";
  }else if (row.suffix === ".ppt" || row.suffix === ".pptx"){
    icon += "ppt";
  }else if (row.suffix === ".doc" || row.suffix === ".docx"){
    icon += "word";
  }else if (row.suffix === ".xls" || row.suffix === ".xlsx"){
    icon += "excel";
  }else if (row.suffix === ".txt"){
    icon += "text";
  }else if (row.fileType === 4){
    icon += "image";
  }else if (row.fileType === 1){
    icon += "audio";
  }else if (row.fileType === 2){
    icon += "video";
  }else if (row.fileType === 5){
    icon += "zip";
  }else {
    icon = "icon-others-other";
  }
  return '<svg aria-hidden="true"><use xlink:href="#'+icon+'"></use></svg>';
}
defineExpose({
  resetCheckedData
})
</script>

<style scoped lang="scss">
.list {
  overflow-y: auto;
  display: block;
  height: calc(100vh - 220px);
  .table{
    max-height: 100vh;
  }
  .fileName{
    cursor: pointer;
    .icon{
      margin-right: 10px;
      :deep(svg) {
        width: 26px;
        height: 30px;
        vertical-align: bottom;
        margin-bottom: -3px;
        margin-right: -6px;
      }
    }
  }

  .el-dropdown-link {
    cursor: pointer;
    color: var(--el-color-primary);
    display: flex;
    align-items: center;
    width: 20px;
    height: 20px;
  }
  :deep(.ep-table__row){
    height: 50px;
  }
}
</style>