<template>
  <el-upload
    v-model:file-list="fileList"
    class="upload-file"
    action="#"
    drag
    multiple
    :auto-upload="false"
    :show-file-list="true"
    :on-change="handleChange"
    :on-remove="handleRemove"
  >
    <el-icon class="el-icon--upload">
      <UploadFilled />
    </el-icon>
    <div class="el-upload__text">拖拽文件到这里，或点击选择文件上传</div>
  </el-upload>

  <el-row>
    <el-table :data="tableData" stripe empty-text="暂无数据" style="width: 100%">
      <el-table-column prop="name" label="文件名" />
      <el-table-column prop="percentage" label="进度">
        <template #default="scope">
          <el-progress
            :text-inside="true"
            :stroke-width="26"
            :percentage="scope.row.percentage || 0"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="80">
        <template #default="scope">
          <el-button
            v-if="scope.row.fileId > 0"
            link
            type="primary"
            size="small"
            @click="getFileDetail(scope.row.fileId)"
          >
            查看
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-row>
</template>

<script setup lang="ts">
/**
 * 上传组件
 *
 * 讲解抓手：
 * 1. 先看 calculateMd5()，这是上传前置处理。
 * 2. 再看 uploadFileInit() 和 startUpload()，这是上传主链路。
 * 3. 这里体现的是“秒传判断 + 8MB 分片上传 + 进度更新”。
 */

import axios from 'axios'
import { UploadFilled } from '@element-plus/icons-vue'
import type { UploadFile, UploadFiles, UploadProps } from 'element-plus'
import { ref } from 'vue'
import SparkMD5 from 'spark-md5'
import api from '../api/config'

type UploadRow = UploadFile & {
  fileId?: number
  percentage?: number
}

const props = defineProps<{
  dirId: number | string
}>()

const emit = defineEmits<{
  (e: 'selectFile', fileDetail: any, visible: boolean): void
}>()

const tableData = ref<UploadRow[]>([])
const fileList = ref<UploadFiles>([])

// 当前实现按 8MB 分片：既能降低大文件单次上传压力，也方便前端显示进度。
const chunkSize = 8 * 1024 * 1024

const uploadFileInit = async (index: number, fileName: string, md5: string) => {
  // 初始化接口负责登记文件、判断是否可以秒传，并返回后续上传需要的 uploadId。
  const result = await api.post('/file/uploadInit', {
    fileName,
    md5,
    dirId: props.dirId,
  })
  tableData.value[index].fileId = result.data.upload.fileId
  return result
}

const uploadFileToServer = async (
  index: number,
  file: Blob,
  chunkNumber: number,
  chunkTotal: number,
  fileName: string,
  uploadId: string,
  md5: string
) => {
  // 本地文件服务器模式下，每个分片都带上元信息提交给后端合并。
  const form = new FormData()
  form.append('file', file)
  form.append('chunkNumber', String(chunkNumber))
  form.append('chunkTotal', String(chunkTotal))
  form.append('fileName', fileName)
  form.append('uploadId', uploadId)
  form.append('md5', md5)
  form.append('dirId', String(props.dirId))

  const result = await api.postForm('/file/upload', form)
  tableData.value[index].fileId = result.data
  return result
}

const uploadFileToMinioServer = async (file: Blob, url: string) => {
  // 如果后端返回的是对象存储模式，前端直接把分片传到预签名地址。
  return axios.put(url, file)
}

const getFileDetail = async (fileId: number) => {
  if (!fileId) {
    return
  }

  const result = await api.get(`/file/detail/${fileId}`)
  emit('selectFile', result.data, true)
}

const startUpload = async (uploadFile: UploadRow, md5: string) => {
  const index = tableData.value.findIndex((item) => item.uid === uploadFile.uid)
  if (index === -1) {
    return
  }

  const fileName = uploadFile.name
  const fileSize = uploadFile.size || 0
  const initResult = await uploadFileInit(index, fileName, md5)

  if (initResult.data.upload.skip) {
    // 如果后端发现同 MD5 文件已经存在，就直接完成，这就是“秒传”。
    uploadFile.percentage = 100
    tableData.value[index].percentage = 100
    return
  }

  const chunkTotal = Math.ceil(fileSize / chunkSize)
  if (!chunkTotal || !uploadFile.raw) {
    return
  }

  for (let chunkNumber = 0, start = 0; chunkNumber < chunkTotal; chunkNumber += 1, start += chunkSize) {
    const end = Math.min(fileSize, start + chunkSize)
    const chunkFile = uploadFile.raw.slice(start, end)

    if (initResult.data.fileServerType === 'local') {
      await uploadFileToServer(
        index,
        chunkFile,
        chunkNumber + 1,
        chunkTotal,
        fileName,
        initResult.data.upload.uploadId,
        md5
      )
    } else {
      await uploadFileToMinioServer(chunkFile, initResult.data.policyUrl)
    }

    const percent = Number((((chunkNumber + 1) / chunkTotal) * 100).toFixed(2))
    uploadFile.percentage = percent
    tableData.value[index].percentage = percent
  }
}

const handleChange: UploadProps['onChange'] = async (uploadFile) => {
  // 文件先进入表格，再异步计算 MD5，这样用户一选中文件就能看到上传项。
  tableData.value.push({ ...uploadFile })
  calculateMd5(uploadFile as UploadRow)
}

const handleRemove: UploadProps['onRemove'] = async (uploadFile) => {
  // 删除动作只移除当前前端队列，不再误调旧系统接口。
  const index = tableData.value.findIndex((item) => item.uid === uploadFile.uid)
  if (index !== -1) {
    tableData.value.splice(index, 1)
  }
}

const calculateMd5 = (file: UploadRow) => {
  if (!file.raw) {
    return
  }

  // MD5 也按较小块读取，避免一次性读取大文件占用过多浏览器内存。
  const blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice
  const fileReader = new FileReader()
  const md5ChunkSize = 2 * 1024 * 1024
  const chunks = Math.ceil((file.size || 0) / md5ChunkSize)
  let currentChunk = 0
  const spark = new SparkMD5.ArrayBuffer()

  fileReader.onload = (event) => {
    if (event.target?.result) {
      spark.append(event.target.result as ArrayBuffer)
    }

    currentChunk += 1
    if (currentChunk < chunks) {
      loadNext()
    } else {
      const md5 = spark.end()
      startUpload(file, md5)
    }
  }

  fileReader.onerror = () => {
    console.warn('FileReader error.')
  }

  const loadNext = () => {
    const start = currentChunk * md5ChunkSize
    const end = Math.min(file.size || 0, start + md5ChunkSize)
    fileReader.readAsArrayBuffer(blobSlice.call(file.raw, start, end))
  }

  loadNext()
}
</script>

<style scoped>
:deep(.ep-upload-list) {
  display: none !important;
}
</style>
