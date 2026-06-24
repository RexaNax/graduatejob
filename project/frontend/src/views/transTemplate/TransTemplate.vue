<template>
  <div class="container">
    <el-button type="primary" @click="add()">新增</el-button>
    <el-table v-loading="loading" :data="list" empty-text="暂无数据" style="width: 100%">
      <el-table-column fixed="left" prop="name" label="名称"/>
      <el-table-column label="分辨率" width="160" >
        <template #default="scope">
        <span v-if="scope.row.width === 0 && scope.row.height > 0">
          0(自动) x {{scope.row.height}}
        </span>
          <span v-else-if="scope.row.width > 0 && scope.row.height === 0">
          {{scope.row.width}} x 0(自动)
        </span>
          <span v-else>
          {{scope.row.width}} x {{scope.row.height}}
        </span>
        </template>
      </el-table-column>
      <el-table-column prop="format" label="输出格式" width="80" />
      <el-table-column prop="codec" label="视频编解码器" width="110" />
      <el-table-column prop="frameRate" label="视频帧率" width="80" />
      <el-table-column prop="bitRate" label="视频码率" width="80" />
      <el-table-column prop="audioBitRate" label="音频码率" width="80" />
      <el-table-column prop="audioSampleRate" label="音频采样率" width="100" />
      <el-table-column prop="audioChannel" label="音频声道" width="80" />
      <el-table-column prop="audioCodec" label="音频编解码器" width="110" />
      <el-table-column fixed="right" label="状态" width="70" >
        <template #default="scope">
          <el-switch v-model="scope.row.statusBoo" @click="updateStatus(scope.row.id, scope.row.status)"/>
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="100">
        <template #default="scope">
          <el-button link type="primary" size="small" @click="edit(scope.row)">编辑</el-button>
          <el-popconfirm title="确定删除吗？" @confirm="del(scope.row.id)" cancel-button-text="取消" confirm-button-text="确定">
            <template #reference>
              <el-button link type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <el-dialog
      v-model="dialogVisible"
      :title="title"
      width="750px"
  >
    <el-form :model="form" :inline="true" label-width="120px">
      <el-form-item label="模板名称">
        <el-input v-model="form.name" maxlength="32" placeholder="请输入模板名称" style="width: 189px"/>
      </el-form-item>
      <el-form-item label="输出格式">
        <el-select v-model="form.format" placeholder="请选择输出格式">
          <el-option v-for="item in formatList" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="分辨率">
        <el-select v-model="form.resolRatio" placeholder="请选择分辨率">
          <el-option v-for="item in resolRatioList" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="视频编解码器">
        <el-select v-model="form.codec" placeholder="请选择编解码器">
          <el-option v-for="item in codecList" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="视频帧率">
        <el-select v-model="form.frameRate" placeholder="请选择视频帧率">
          <el-option v-for="item in frameRateList" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="视频码率">
        <el-select v-model="form.bitRate" placeholder="请选择视频码率">
          <el-option v-for="item in bitRateList" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="音频码率">
        <el-select v-model="form.audioBitRate" placeholder="请选择音频码率">
          <el-option v-for="item in audioBitRateList" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="音频采样率">
        <el-select v-model="form.audioSampleRate" placeholder="请选择音频采样率">
          <el-option v-for="item in audioSampleRateList" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="音频声道">
        <el-select v-model="form.audioChannel" placeholder="请选择音频声道">
          <el-option v-for="item in audioChannelList" :label="item" :value="item" />
        </el-select>
      </el-form-item>
      <el-form-item label="音频编解码器">
        <el-select v-model="form.audioCodec" placeholder="请选择音频编解码器">
          <el-option v-for="item in audioCodecList" :label="item" :value="item" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="close()" :disabled="saveLoading">取消</el-button>
        <el-button type="primary" @click="save()" :loading="saveLoading">
          保存
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import api from "~/api/config";
import { ElMessage,ElLoading } from 'element-plus'

const list = ref([])
const loading = ref(false);
const saveLoading = ref(false);
const dialogVisible = ref(false)
const title = ref("新增")
const formatList = ref(["mp4","m3u8"])
const codecList = ref(["h264"])
const resolRatioList = ref(["640x480","850x480","1280x720","1920x1080","480x640","480x850","720x1280","1080x1920"])
const frameRateList = ref([15,20,25,30,40,50,60])
const bitRateList = ref([500,800,1200,2000,3000,5000,8000])
const audioBitRateList = ref([16,32,48,64,80,96,112,128,160,192,224,256,320,384,448,512])
const audioSampleRateList = ref([8000,11025,12000,16000,22050,24000,32000,44100,48000,64000,88200,96000])
const audioChannelList = ref([1,2])
const audioCodecList = ref(["aac"])
const defaultForm = ref({
  "name": "",
  "status": 0,
  "resolRatio": "1920x1080",
  "format": "mp4",
  "frameRate": 30,
  "bitRate": 1200,
  "codec": "h264",
  "audioCodec": "aac",
  "audioChannel": 2,
  "audioBitRate": 128,
  "audioSampleRate": 48000
})
const form = ref({})
// 生命周期钩子
onMounted(() => {
  getList();
})


const getList = async () => {
  if (loading.value){
    return;
  }
  loading.value = true;
  const result = await api.get(`/transTemplate/list`);
  list.value = result.data;
  for (let i in list.value){
    list.value[i].statusBoo = list.value[i].status === 1;
  }
  loading.value = false;
}
const save = async () => {
  saveLoading.value = true;
  const resolRatioArr = form.value.resolRatio.split("x");
  form.value.width = resolRatioArr[0];
  form.value.height = resolRatioArr[1];
  const result = await api.post(`/transTemplate/save`, form.value);
  saveLoading.value = false;
  if (result){
    dialogVisible.value = false;
    ElMessage({
      message: title.value + '成功',
      type: 'success',
    })
    getList();
  }
}
const del = async (id) => {
  const loading = ElLoading.service({
    lock: true,
    text: '删除中',
    background: 'rgba(0, 0, 0, 0.7)',
  })
  const result = await api.post(`/transTemplate/delete`, {id: id});
  loading.close();
  if (result){
    ElMessage({
      message: '删除成功',
      type: 'success',
    })
    getList();
  }
}
const updateStatus = async (id, status) => {
  console.log(status)
  status = status === 0 ? 1 : 0;
  console.log(status)
  const result = await api.post(`/transTemplate/updateStatus`, {id: id, status: status});
  if (result){
    ElMessage({
      message: status === 1 ? '开启成功' : '关闭成功',
      type: 'success',
    })
    getList();
  }
}
const add = () => {
  title.value = "新增";
  form.value = JSON.parse(JSON.stringify(defaultForm.value));
  dialogVisible.value = true;
}
const edit = (row) => {
  console.log(row)
  title.value = "修改";
  form.value = row;
  form.value.resolRatio = row.width + "x" + row.height;
  dialogVisible.value = true;
}
const close = () => {
  form.value = {};
  dialogVisible.value = false;
}
</script>

<style lang="scss" scoped>
  :deep(.ep-form-item__content){
    width: 180px;
  }
</style>

