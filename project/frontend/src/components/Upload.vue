<template>
  <el-upload class="upload-file" action="#" drag multiple :auto-upload="false" :show-file-list="true"
             v-model:file-list="fileList" :on-change="handleChange" :on-remove="handleRemove">
    <el-icon class="el-icon--upload"><upload-filled /></el-icon>
    <div class="el-upload__text">
      拖拽文件到这里或者点击选择文件上传
    </div>
  </el-upload>
  <el-row>
    <el-table :data="tableData" stripe empty-text="暂无数据" style="width: 100%">
      <el-table-column prop="name" label="文件名" />
      <el-table-column prop="percentage" label="进度">
        <template #default="scope">
          <el-progress :text-inside="true" :stroke-width="26" :percentage="scope.row.percentage" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80">
        <template #default="scope">
          <el-button v-if="scope.row.fileId > 0" link type="primary" size="small" @click="getFileDetail(scope.row.fileId)">查看</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-row>
</template>

<script setup lang="ts">
import axios from 'axios'
import api from '../api/config'
import { UploadFilled } from '@element-plus/icons-vue'
import { type UploadProps, UploadFile, UploadFiles, ElMessage } from 'element-plus'
import { ref } from 'vue';
import SparkMD5 from 'spark-md5'

const props = defineProps(["dirId"])

const tableData = ref<UploadFiles>([])
// 文件列表
const fileList = ref<UploadFiles>([])

// 分块大小8MB
const chunkSize = 1024 * 1024 * 8

const dialogTableVisible = ref(false)

/**
 * 将分块文件上传至服务器
 * @param file 上传的分块文件
 * @param chunkNumber 当前是第几块
 * @param chunkTotal 文件分块的总数
 * @param fileName 文件名称
 */
const uploadFileInit = async (index: any, fileName: any, md5: any) => {
  const result = await api.post(`/file/uploadInit`, {
    fileName: fileName,
    md5: md5,
    dirId: props.dirId
  })
  tableData.value[index].fileId = result.data.upload.fileId
  return result
}

/**
 * 将分块文件上传至服务器
 * @param file 上传的分块文件
 * @param chunkNumber 当前是第几块
 * @param chunkTotal 文件分块的总数
 * @param fileName 文件名称
 */
const uploadFileToServer = async (index: any, file: any, chunkNumber: any, chunkTotal: any, fileName: any, uploadId: any, md5: any) => {
  const form = new FormData();
  // 这里的data是文件
  form.append("file", file);
  form.append("chunkNumber", chunkNumber);
  form.append("chunkTotal", chunkTotal);
  form.append("fileName", fileName);
  form.append("uploadId", uploadId);
  form.append("md5", md5);
  form.append("dirId", props.dirId);
  const result = await api.postForm(`/file/upload`, form)
  console.log("success", result)
  tableData.value[index].fileId = result.data;
  return result
}

const emits = defineEmits();
const getFileDetail = async (fileId: any) => {
  if (fileId == 0){
    return;
  }
  const result = await api.get(`/file/detail/${fileId}`);
  emits("selectFile", result.data, true);
}

const uploadFileToMinioServer = async (file: any, fileName: any, url: any) => {
  const form = new FormData();
  // 这里的data是文件
  // form.append("key", fileName);
  // form.append("x-amz-algorithm", xamzalgorithm);
  // form.append("x-amz-credential", xamzcredential);
  // form.append("policy", policy);
  // form.append("x-amz-signature", xamzsignature);
  // form.append("x-amz-date", xamzdate);
  form.append("file", file);
  const result = await axios.put(url, form)
  return result
}



/**
 * 合并文件
 * @param chunkTotal 文件分块的总数量
 * @param fileName 文件名称
 */
const mergeFiles = async (chunkTotal: any, fileName: any) => {
  // const form = new FormData();
  // form.append("chunkTotal", chunkTotal);
  // form.append("fileName", fileName)
  // await axios.post("http://localhost:8080/uploadVideo/merge", form)

  const result = await axios.get(`http://localhost:8080/uploadVideo/merge?chunkTotal=${chunkTotal}&fileName=${fileName}`)
  // 这个result.data是axios相关的了，这里就不说了
  return result.data
}



/**
 * 根据文件名删除文件
 * @param fileName 文件名
 */
const deleteFileByFileName = async (fileName: any) => {
  const result = await axios.get(`http://localhost:8080/uploadVideo/deleteByFileName?fileName=${fileName}`)
  // 这个result.data这个不多说了，axios相关的，不懂的console.log(result)就知道了
  console.log(result.data)
}



/**
 * el-upload内置的change函数，文件上传或者上传成功时的回调，不过这里因为
 * :auto-upload="false"的缘故，上床成功的回调不会执行
 * @param uploadFile el-upload当前上传的文件对象
 * @param uploadFiles el-upload上传的文件列表
 */
const handleChange: UploadProps['onChange'] = async (uploadFile, uploadFiles) => {
  tableData.value.push({ ...uploadFile })
  console.log("handleChange table size：", tableData.value.length)
  calculateMd5(uploadFile);
}

const uploadFile: UploadProps['onChange'] = async (uploadFile, md5) => {
  const index = tableData.value.findIndex(item => item.uid === uploadFile.uid)
  let fileName = uploadFile.name
  const fileSize = uploadFile.size || 0
  const initResult = await uploadFileInit(index, fileName, md5);
  if (initResult.data.upload.skip){
    console.log("跳过文件上传")
    const percents = 100;
    uploadFile.percentage = percents
    tableData.value[index].percentage = percents
  }else{
    let chunkTotals = Math.ceil(fileSize / chunkSize);
    if (chunkTotals > 0) {
      for (let chunkNumber = 0, start = 0; chunkNumber < chunkTotals; chunkNumber++, start += chunkSize) {
        let end = Math.min(fileSize, start + chunkSize);
        // uploadFile.raw这个是element plus 中 el-upload的自己上传的文件就放在这个raw里面，可以console.log(uploadFile)看一下
        // 加 ？是因为ts语法提示“uploadFile.raw”可能为“未定义”，加了这个就不过有报错了
        const files = uploadFile.raw?.slice(start, end)

        console.log("开始上传文件 :" + initResult.data.fileServerType);
        let result;
        if (initResult.data.fileServerType === "local"){
          result = await uploadFileToServer(index, files, chunkNumber + 1, chunkTotals, fileName, initResult.data.upload.uploadId, md5)
        }else {
          result = await uploadFileToMinioServer(files, initResult.data.fileName, initResult.data.policyUrl)
        }
        const percents = parseFloat(((chunkNumber + 1) / chunkTotals * 100).toFixed(2) + "")
        uploadFile.percentage = percents
        tableData.value[index].percentage = percents
        // percentage.value = percents
        console.log(result)
      }
    }
  }
}



/**
 * 从el-upload封装的文件列表中删除文件
 * // el-upload的（内置方法），点击el-upload默认的文件展示以列表的 X 那个图标时，会移除文件，并从文件列表中去掉
 * @param uploadFile el-upload当前删除点击的文件
 * @param uploadFiles el-upload的文件列表
 */
const handleRemove: UploadProps['onRemove'] = async (uploadFile, uploadFiles) => {
  // 这个删除表格tableData中的列表数据
  const index2 = tableData.value.findIndex((item2: UploadFile) => item2.uid === uploadFile.uid)
  if (index2 !== -1) {
    tableData.value.splice(index2, 1)
  }
  await deleteFileByFileName(uploadFile.name)
  // handleRemove内置的删除文件方法，
  // 这里的uploadFiles跟绑定的v-model:file-list="fileList"可以看成是同一个数组，这两个数组的数据是一样的，从这两个数组中删除或者移除任何一个数据，
  // 另外一个数据也会跟着变化的，这个element plus已经实现了
  console.log(uploadFiles)
}



/**
 * 自定义的表格删除的方法
 * @param file 表格某一行的数据
 */
const handleRemoveUploadFileList = async (file: UploadFile) => {
  const index = fileList.value.findIndex((item: UploadFile) => item.uid === file.uid)
  if (index !== -1) {
    fileList.value.splice(index, 1)
  }
  const index2 = tableData.value.findIndex((item2: UploadFile) => item2.uid === file.uid)
  if (index2 !== -1) {
    tableData.value.splice(index2, 1)
  }

  // 删除磁盘中的文件
  await deleteFileByFileName(file.name)
}

/**
 * 预览视频
 * @param row 表格传入的行数据
 */
const handlePreviewUploadFileList = () => {
  dialogTableVisible.value = true
}

/**
 * el-dialog的回调
 */
const handleClose = () => {

  // 下面这两中都可以，any方便。HTMLVideoElement这个也行，有提示。两种方法写都可以不会报错

  // let myVideo:any = document.getElementById('video-play') //对应video标签的ID

  let myVideo:HTMLVideoElement = document.getElementById('video-play')  as HTMLVideoElement//对应video标签的ID

  myVideo.pause()

  dialogTableVisible.value = false
}

const calculateMd5 = (file: any) => {
  console.log("fileSize: " + file.size)
  const blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice
  const fileReader = new FileReader()
  const chunkSize = 2097152
  const chunks = Math.ceil(file.size / chunkSize)
  let currentChunk = 0
  //注意点，网上都是 这一步都是有问题的， SparkMD5.ArrayBuffer()
  const spark = new SparkMD5.ArrayBuffer()
  fileReader.onload = function (e) {
    spark.append(e.target.result)
    currentChunk++
    if (currentChunk < chunks) {
      loadNext()
    } else {
      const md5 = spark.end()
      //计算后的结果
      console.log('computed hash', md5)
      uploadFile(file, md5);
    }
  }
  fileReader.onerror = function () {
    console.warn('FileReader error.')
  }
  function loadNext () {
    const start = currentChunk * chunkSize
    const end = ((start + chunkSize) >= file.size) ? file.size : start + chunkSize
    // 注意这里的 fileRaw
    fileReader.readAsArrayBuffer(blobSlice.call(file.raw, start, end))
  }
  loadNext()
}

</script>

<style scoped>
:deep(.ep-upload-list){
  display: none !important;
}
</style>
