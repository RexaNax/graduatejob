<template>
  <el-checkbox size="large"
               v-if="props.fileList.length > 0"
               :indeterminate="multipleSelectionIds.length > 0 && multipleSelectionIds.length < props.fileList.length"
               v-model="selectAllChecked"
               @click.stop
               @change="toggleSelectAll"
               :label="multipleSelectionIds.length > 0 ? '已选中' + multipleSelectionIds.length + '个文件/文件夹' : '全选'"/>
  <el-divider v-if="props.fileList.length > 0"/>
  <div class="grid" v-loading="props.loading">
    <template v-for="(item, index) in props.fileList" :key="item.id">
      <ContextMenu :menu="item.isDir === 0 ?
          [
            { label: '下载' },
            { label: '移动到' },
            { label: '重命名' },
            { label: '删除' },
          ] : [
            { label: '移动到' },
            { label: '重命名' },
            { label: '删除' },
          ]" @select="contextMenuSelect($event.label, item.id, item.name)" @handleEnter="contextMenuHandleEnter(item)">
        <div :class="props.editId === item.id || item.checked ? 'item actived' : 'item'" @click="selectFile(item)" @mouseenter="hoverFile(item, true)" @mouseleave="hoverFile(item, false)">
          <el-checkbox size="large" :class="item.checked || item.hover ? 'show' : 'hide'" v-model="item.checked" @click.stop @change="checkFileChange(item)"/>
          <div :class="'img ' + (item.isDir === 1 ? ' folder' : '')" :style="getFileBg(item)">
            <span :class="'icon ' + (item.thumUrl !== '' && item.thumUrl !== undefined && item.thumUrl !== null ? 'tag' : 'normal')" v-html="genFileIcon(item)"></span>
          </div>
          <span v-if="item.id === props.editId">
            <el-input ref="editNameRef" class="editInp" @click.stop v-model="editName" maxlength="200" @blur="rename(item)" @keydown.enter="rename(item)"/>
          </span>
          <div class="label" :title="item.name" v-else>{{item.name}}</div>
        </div>
      </ContextMenu>
    </template>
    <el-empty description="暂无数据" v-if="fileList.length === 0" />
  </div>
</template>
<script setup>
import {nextTick, ref, shallowRef, watch, onMounted} from "vue";
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

const props = defineProps(["fileList","loading","editId"])
const editNameRef = shallowRef(null);
const editName = ref("")
const multipleSelectionIds = ref([])
const selectAllChecked = ref(false)

watch(props.fileList, (val) => {
  selectAllChecked.value = props.fileList.length > 0 && props.fileList.length === multipleSelectionIds.value.length;
})
watch(multipleSelectionIds, (val) => {
  selectAllChecked.value = props.fileList.length > 0 && props.fileList.length === multipleSelectionIds.value.length;
})

onMounted(() => {
  selectAllChecked.value = props.fileList.length > 0 && props.fileList.length === multipleSelectionIds.value.length;
})

const selectFile = (item) => {
  emits("selectFile", item);
  singleCheckedFile(item, false)
}
const contextMenuHandleEnter = (item) => {
  let existDir = item.isDir === 1;
  singleCheckedFile(item, existDir)
}

const singleCheckedFile = (item, existDir) => {
  let ids = [item.id];
  props.fileList.forEach((r, i) => {
    if (r && r.id > 0 && r.checked) {
      r.checked = false;
    }
  });
  item.checked = true;
  setMultipleIds(ids, existDir)
}

const setMultipleIds = (ids, existDir) => {
  emits('setMultipleSelectionIds', ids)
  emits('setMultipleSelectionExistDir', existDir)
  multipleSelectionIds.value = ids;
}

const checkFileChange = (item) => {
  let ids = [];
  let existDir = false;
  props.fileList.forEach((r, i) => {
    if (r && r.id > 0 && r.checked) {
      if (r.isDir === 1){
        existDir = true;
      }
      ids.push(r.id);
    }
  });
  setMultipleIds(ids, existDir)
}
const toggleSelectAll = () => {
  let ids = [];
  let existDir = false;
  props.fileList.forEach((r, i) => {
    r.checked = selectAllChecked.value;
    if (r && r.id > 0 && r.checked) {
      if (r.isDir === 1){
        existDir = true;
      }
      ids.push(r.id);
    }
  });
  setMultipleIds(ids, existDir)
}
const hoverFile = (item, hover) => {
  item.hover = hover;
}
const contextMenuSelect = (item, fileId, fileName) => {
  if (item === "下载"){
    download(fileId);
  }else if (item === "移动到"){
    move(fileId);
  }else if (item === "重命名"){
    emits("toEditFile", fileId)
    toEditName(fileName);
  }else if (item === "删除"){
    delFile(fileId);
  }
}
const toEditName = (fileName) => {
  editName.value = fileName;
  nextTick(() => {
    editNameRef.value[0].focus();
    try {
      let el = editNameRef.value[0].$el.children[0].children[0];
      el.select()
      el.selectionEnd = fileName.lastIndexOf(".");
    }catch (e){
      editNameRef.value[0].select();
    }
  })
}
const download = (fileId) => {
  emits("toDownload", fileId);
}

const move = (fileId) => {
  emits("toMove", fileId);
}
const rename = (item) => {
  emits("rename", item, editName.value)
}
const delFile = (fileId) => {
  emits("delFile", fileId);
}

const getFileBg = (item) => {
  if(item.isDir === 1) {
    return '';
  }
  let css = 'background-color: transparent;border:1px solid #D8DFE6;background-image:url("';
  if(item.thumUrl === '') {
    // css += "/static/default.png";
  }else {
    css += item.thumUrl;
  }
  css += '")';
  return css;
}
const resetCheckedData = (selectionIds) => {
  selectAllChecked.value = selectionIds.length === props.fileList.length;
  multipleSelectionIds.value = selectionIds;
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
  toEditName, resetCheckedData
})
</script>

<style scoped lang="scss">
.grid {
  overflow-y: auto;
  display: block;
  height: calc(100vh - 262px);
  .item {
    width: 176px;
    height: 176px;
    border-radius: 6px;
    border: 2px solid transparent;
    background-color: transparent;
    text-align: left;
    display: inline-block;
    margin: 18px 0 0 15px;
    position: relative;
    .folder {
      background-color: transparent;
      background-size: contain;
      width: 86px;
      height: 100px;
    }
    .img {
      width: 118px;
      height: 104px;
      background-color: transparent;
      margin: 27px auto 0 auto;
      background-repeat: no-repeat;
      background-position: center;
      background-size: cover;
      border-radius: 6px;
      position: relative;
      text-align: center;
    }
    .icon {
      line-height:104px;
      display: block;

    }
    .normal  {
      background-color: transparent;
      border-radius: 6px;
      :deep(svg) {
        width: 80px;
        height: 80px;
        vertical-align: middle;
        margin-left: 0 !important;
        margin-right: 0 !important;
      }
    }
    .tag {
      background-color: transparent;
      text-align: right;
      :deep(svg) {
        width: 26px;
        height: 30px;
        vertical-align: bottom;
        margin-bottom: -3px;
        margin-right: -6px;
      }
    }
    .label {
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
      max-width: 90%;
      color: #303233;
      font-size: 14px;
      margin: 8px auto 0 auto;
      line-height: 20px;
      text-align: center;
    }
    .editInp{
      padding: 0 5px;
      margin: 4px 0 16px 0;
      height: 25px;
      :deep(.ep-input__inner){
        height: 25px;
        text-align: center;
      }
    }
    :deep(.ep-checkbox--large){
      position: absolute;
      left: 10px;
      top: 10px;
      height: 10px;
    }
    .show{
      display: inline-block;
    }
    .hide{
      display: none;
    }

  }
  .item:hover {
    cursor: pointer;
    background-color: #F5F7FA;
  }
  .actived {
    border: 2px solid #1890ff;
  }
}
.ep-divider--horizontal{
  margin: 0 !important;
}
</style>
