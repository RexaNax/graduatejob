<template>
  <div class="container">
    <el-tabs v-model="activeName" class="tabs">
      <el-tab-pane label="信息" name="first">
        <el-descriptions title="" :column="1">
          <el-descriptions-item label="文件名称：">{{file.name}}</el-descriptions-item>
          <el-descriptions-item label="文件大小：">{{calcFileSize(file.fileSize)}}</el-descriptions-item>
          <el-descriptions-item label="视频时长：" v-if="file.fileType === 1">{{calcThumTime(file.duration)}}</el-descriptions-item>
          <el-descriptions-item label="文档页数：" v-if="file.fileType === 3">{{file.duration}}页</el-descriptions-item>
          <el-descriptions-item label="文件格式：">{{file.suffix ? file.suffix : "-"}}</el-descriptions-item>
          <el-descriptions-item label="转码状态：">
            <el-progress v-if="file.transStatus === 0" class="transProgress" :text-inside="true" :stroke-width="16" :percentage="calcPercentage(file.progressList)" />
            <el-tag type="success" v-if="file.transStatus === 1">转码成功</el-tag>
            <el-tag type="warning" v-if="file.transStatus === 2">部分转码成功</el-tag>
            <el-tag type="info" v-if="file.transStatus === 3">转码失败</el-tag>
            <el-tag type="success" v-if="file.transStatus === 4">不需要转码</el-tag>
            <el-tag type="danger" v-if="file.transStatus === 5">不支持转码</el-tag>
            <el-tag type="info" v-if="file.transStatus === 6">取消转码</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="文件预览：" v-if="file.fileType === 1 || file.fileType === 2 || file.fileType === 4">
            <el-button type="primary" size="small" @click="previewFile(file, file.previewUrl, file.suffix)">原文件预览</el-button>
          </el-descriptions-item>
          <el-descriptions-item label="文件预览：" v-if="file.fileType === 3 && (file.transStatus === 1 || file.transStatus === 4)">
            <el-button type="primary" size="small" @click="previewFile(file, file.previewUrl)">转码预览</el-button>
          </el-descriptions-item>
        </el-descriptions>
        <div class="transProgressList" v-if="file.fileType === 1 && file.progressList && file.progressList.length > 0">
          <div class="title">转码详情</div>
          <el-table :data="file.progressList" style="width: 100%">
            <el-table-column prop="format" label="输出格式" width="120" />
            <el-table-column label="文件大小" width="120" >
              <template #default="scope">
                {{calcFileSize(scope.row.fileSize)}}
              </template>
            </el-table-column>
            <el-table-column label="转码耗时" width="150" >
              <template #default="scope">
                <span v-text="calcTransTime(scope.row.startTime, scope.row.endTime)"></span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="170" >
              <template #default="scope">
                <el-tag type="success" v-if="scope.row.transStatus == 1">转码成功</el-tag>
                <el-tag type="info" v-if="scope.row.transStatus == 3">转码失败</el-tag>
                <el-tag type="info" v-if="scope.row.transStatus == 6">取消转码</el-tag>
                <el-progress v-if="scope.row.transStatus == 0" :text-inside="true" :stroke-width="16" :percentage="scope.row.progress" />
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="80">
              <template #default="scope">
                <el-button v-if="scope.row.transStatus === 1" link type="primary" size="small" @click="previewFile(file, scope.row.previewUrl, '.' + scope.row.format)">预览</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

      </el-tab-pane>
      <el-tab-pane label="封面" name="second">
        <div class="selected">
          <div class="title">已设置封面</div>
          <el-image style="width: 192px; height: 108px" :src="file.thumUrl" />
        </div>
        <div class="selectList">
          <div class="header">
            <span class="title">智能提取</span>
            <span class="change" @click="changeThumList">换一换</span>
          </div>
          <div class="item" v-for="item in showThumList" title="点击设置封面" @click="updateFileThum(file.id, item)">
            <div class="img">
              <el-image style="width: 192px; height: 108px" :src="item.fileUrl" />
            </div>
            <div class="duration" v-if="file.fileType === 1">{{calcThumTime(item.duration)}}</div>
            <div class="duration" v-if="file.fileType === 3">第{{item.duration}}页</div>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="操作" name="third">
        <el-button type="primary" @click="delFile(file.id)">删除文件</el-button>
        <el-button type="success" @click="showShareDialog">分享文件</el-button>
      </el-tab-pane>
      
      <!-- 分享Tab -->
      <el-tab-pane label="分享" name="share">
        <div class="share-section">
          <div class="share-create">
            <h4>创建分享链接</h4>
            <el-form :inline="true">
              <el-form-item label="有效期">
                <el-select v-model="shareExpireDays" style="width: 150px">
                  <el-option label="永久有效" :value="0" />
                  <el-option label="1天" :value="1" />
                  <el-option label="7天" :value="7" />
                  <el-option label="30天" :value="30" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="createShare">生成链接</el-button>
              </el-form-item>
            </el-form>
          </div>
          
          <!-- 分享链接展示 -->
          <div v-if="shareResult.shareCode" class="share-result">
            <el-alert type="success" :closable="false">
              <template #title>
                <div>分享链接已生成</div>
              </template>
              <div class="share-link">
                <el-input v-model="shareLink" readonly>
                  <template #append>
                    <el-button @click="copyShareLink">复制</el-button>
                  </template>
                </el-input>
              </div>
            </el-alert>
          </div>
          
          <!-- 已有分享列表 -->
          <div class="share-list" v-if="shareList.length > 0">
            <h4>已创建的分享</h4>
            <el-table :data="shareList" size="small">
              <el-table-column prop="shareCode" label="分享码" width="120" />
              <el-table-column label="有效期" width="150">
                <template #default="scope">
                  {{ scope.row.expireTime === 0 ? '永久' : formatExpireTime(scope.row.expireTime) }}
                </template>
              </el-table-column>
              <el-table-column prop="viewCount" label="访问次数" width="100" />
              <el-table-column label="操作" width="100">
                <template #default="scope">
                  <el-button link type="danger" size="small" @click="cancelShare(scope.row.id)">取消</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

  </div>

  <el-dialog
      v-model="previewDialog.visible"
      title="预览"
      class="previewDialog"
      width="1000px"
      @close="closePreview()"
      destroy-on-close
  >
    <VideoPreview v-if="previewDialog.fileType === 1" :id="previewDialog.fileId" :name="previewDialog.fileName" :preview-url="previewDialog.previewUrl" :suffix="previewDialog.suffix"/>
    <AudioPreview v-if="previewDialog.fileType === 2" :id="previewDialog.fileId" :name="previewDialog.fileName" :preview-url="previewDialog.previewUrl" :suffix="previewDialog.suffix"/>
    <PdfPreview v-if="previewDialog.fileType === 3" :id="previewDialog.fileId" :name="previewDialog.fileName" :preview-url="previewDialog.previewUrl" :suffix="previewDialog.suffix" :pdf-watermark="previewDialog.watermark"/>
    <ImagePreview v-if="previewDialog.fileType === 4" :id="previewDialog.fileId" :name="previewDialog.fileName" :preview-url="previewDialog.previewUrl" :suffix="previewDialog.suffix" :pdf-watermark="previewDialog.watermark"/>
  </el-dialog>
</template>

<script setup>
import VideoPreview from "~/views/preview/VideoPreview.vue";
import ImagePreview from "~/views/preview/ImagePreview.vue";
import AudioPreview from "~/views/preview/AudioPreview.vue";
import PdfPreview from "~/views/preview/PdfPreview.vue";
import {onBeforeUnmount, onMounted, ref} from "vue";
import api from "~/api/config";
import {ElMessage, ElMessageBox} from "element-plus";

const emits = defineEmits();

const props = defineProps(["fileId", "fileMd5", "dialogName"])
const file = ref({})
const detailIsShow = ref(false)
const showThumIndex = ref(1)
const showThumList = ref([])
const thumList = ref([])
const activeName = ref("first")
const previewDialog = ref({
  visible: false,
  fileId: 0,
  fileName: "",
  previewUrl: "",
  watermark: "",
  fileType: 0
})

// 分享相关
const shareExpireDays = ref(7)
const shareResult = ref({})
const shareList = ref([])
const shareLink = ref('')

// 生命周期钩子
onMounted(() => {
  detailIsShow.value = true;
  getThumList(props.fileMd5);
  getFileDetail(props.fileId)
  getShareList()
})

onBeforeUnmount(() => {
  console.log("onBeforeUnmount")
  detailIsShow.value = false;
})
const getFileDetail = async (fileId) => {
  const result = await api.get(`/file/detail/${fileId}`);
  file.value = result.data;
  console.log("detail file", file.value)
  if (detailIsShow.value && (file.value.transStatus === 0 || file.value.transStatus === 2)){
    setTimeout(getFileDetail, 3000, fileId);
  }
}

const updateFileThum = async (fileId, thum) => {
  const result = await api.post(`/file/updateFileThum`,{fileId: fileId, thumId: thum.id});
  file.value.thumUrl = thum.fileUrl;
  if (result){
    ElMessage({
      message: '封面设置成功',
      type: 'success',
    })
    getFileList();
  }
}

const getThumList = async (md5) => {
  const result = await api.get(`/file/thumList`,{params: {md5: md5}});
  console.log("res", result)
  thumList.value = result.data;
  changeThumList();
}

const delFile = async (fileId) => {
  emits("delFile", fileId);
}
const changeThumList = async () => {
  showThumIndex.value = showThumIndex.value === 1 ? 0 : 1;
  showThumList.value = thumList.value.slice(showThumIndex.value * 5, showThumIndex.value * 5 + 5)
}
const previewFile = async (file, previewUrl, suffix) => {
  console.log(file, previewUrl)
  previewDialog.value.visible = true;
  previewDialog.value.fileId = file.id;
  previewDialog.value.fileName = file.name;
  previewDialog.value.previewUrl = previewUrl;
  previewDialog.value.watermark = file.pdfWatermark;
  previewDialog.value.fileType = file.fileType;
  previewDialog.value.suffix = suffix;
}

const closePreview = () => {
  previewDialog.value.visible = false;
}
const calcThumTime = (seconds) => {
  var hours = Math.floor(seconds / 3600);
  var minutes = Math.floor((seconds % 3600) / 60);
  var secs = Math.floor(seconds % 60);
  hours = hours < 10 ? "0" + hours : hours;
  minutes = minutes < 10 ? "0" + minutes : minutes;
  secs = secs < 10 ? "0" + secs : secs;
  return `${hours}:${minutes}:${secs}`;
}

const calcTransTime = (startTime, endTime) => {
  console.log(startTime, endTime)
  if (endTime === "0" && startTime !== "0"){
    endTime = new Date().getTime();
  }
  const seconds = (endTime - startTime) / 1000;
  var hours = Math.floor(seconds / 3600);
  var minutes = Math.floor((seconds % 3600) / 60);
  var secs = Math.floor(seconds % 60);
  hours = hours < 10 ? "0" + hours : hours;
  minutes = minutes < 10 ? "0" + minutes : minutes;
  secs = secs < 10 ? "0" + secs : secs;
  if (hours == 0){
    return `${minutes}:${secs}`;
  }else {
    return `${hours}:${minutes}:${secs}`;
  }
}


const calcPercentage = (progressList) => {
  if (progressList === undefined || progressList.length === 0){
    return 0;
  }
  let percentage = 0;
  for (let i in progressList){
    percentage += progressList[i].progress;
  }
  return Number((percentage / progressList.length).toFixed(2));
}

const calcFileSize = (size) => {
  console.log("calcFileSize", size)
  if (size === undefined){
    return "-";
  }
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  for (var i = 0; size >= 1024 && i < units.length - 1; i ++) {
    size /= 1024;
  }
  console.log("calcFileSize", size)
  return Number(size).toFixed(1) + " " + units[i];
}

// ========== 分享功能 ==========

/**
 * 获取分享列表
 */
const getShareList = async () => {
  try {
    const result = await api.get('/share/list', { params: { fileId: props.fileId } })
    if (result && result.data) {
      shareList.value = result.data
    }
  } catch (e) {
    console.error('获取分享列表失败', e)
  }
}

/**
 * 创建分享链接
 * 
 * 【原理】
 * 1. 调用后端 /share/create 接口
 * 2. 后端生成唯一分享码
 * 3. 前端拼接分享链接
 */
const createShare = async () => {
  try {
    const result = await api.post('/share/create', null, {
      params: {
        fileId: props.fileId,
        expireDays: shareExpireDays.value
      }
    })
    if (result && result.data) {
      shareResult.value = result.data
      // 拼接分享链接
      shareLink.value = `${window.location.origin}/#/share/${result.data.shareCode}`
      ElMessage.success('分享链接已生成')
      getShareList()
    }
  } catch (e) {
    console.error('创建分享失败', e)
  }
}

/**
 * 复制分享链接
 */
const copyShareLink = () => {
  navigator.clipboard.writeText(shareLink.value).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

/**
 * 取消分享
 */
const cancelShare = async (shareId) => {
  try {
    await api.post('/share/cancel', null, { params: { shareId } })
    ElMessage.success('已取消分享')
    getShareList()
  } catch (e) {
    console.error('取消分享失败', e)
  }
}

/**
 * 格式化过期时间
 */
const formatExpireTime = (timestamp) => {
  const date = new Date(timestamp)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

/**
 * 显示分享对话框
 */
const showShareDialog = () => {
  activeName.value = 'share'
}
</script>

<style scoped lang="scss">
.selected{
  .title{
    font-weight: bold;
    margin: 5px 0 10px 0;
    border-bottom: solid 1px #DFE6ED;
    padding-bottom: 5px;
  }
}
.selectList{
  .header{
    padding: 5px 0;
    border-bottom: solid 1px #DFE6ED;
    margin: 5px 0 10px 0;
    .title{
      font-weight: bold;
    }
    .change{
      float: right;
      cursor: pointer;
    }
  }
  .item{
    display: inline-block;
    margin: 10px;
    cursor: pointer;
    .duration{
      text-align: center;
      font-size: 12px;
    }
  }
}
.transProgress{
  width: 200px;
  display: inline-block;
  vertical-align: sub;
}
.transProgressList{
  .title{
    font-weight: bold;
    border-bottom: solid 1px #DFE6ED;
    padding: 10px 0;
  }
}

// 分享样式
.share-section {
  h4 {
    margin: 0 0 15px 0;
    color: #333;
  }
  
  .share-create {
    margin-bottom: 20px;
  }
  
  .share-result {
    margin: 20px 0;
    
    .share-link {
      margin-top: 10px;
    }
  }
  
  .share-list {
    margin-top: 20px;
  }
}
</style>