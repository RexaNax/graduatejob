<template>
  <div class="container">
    <el-table v-loading="loading" :data="list" empty-text="暂无数据" style="width: 100%">
      <el-table-column prop="fileName" label="文件名"/>
      <el-table-column prop="format" label="输出格式" width="120" />
      <el-table-column label="转码耗时" width="150" >
        <template #default="scope">
          <span v-text="calcTransTime(scope.row.startTime, scope.row.endTime)"></span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="180" >
        <template #default="scope">
          <el-tag type="success" v-if="scope.row.transStatus == 1">转码成功</el-tag>
          <el-tag type="info" v-if="scope.row.transStatus == 3">转码失败</el-tag>
          <el-tag type="info" v-if="scope.row.transStatus == 6">取消转码</el-tag>
          <el-progress v-if="scope.row.transStatus == 0" :text-inside="true" :stroke-width="16" :percentage="scope.row.progress" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="转码时间" width="180" />
      <el-table-column fixed="right" label="操作" width="100">
        <template #default="scope">
          <el-popconfirm title="确定要重新转码吗？" @confirm="manualTranscode(scope.row.fileId)" cancel-button-text="取消" confirm-button-text="确定">
            <template #reference>
              <el-button link type="primary" size="small">重新转码</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <div class="page">
      <el-pagination background layout="prev, pager, next"
                     :hide-on-single-page="true"
                     :page-size="pageSize" :current-page="pageNo" :total="rowTotal"
        @prev-click="prevPage" @next-click="nextPage" @current-change="pageChange"/>
    </div>

  </div>
</template>

<script setup>
import {ref, onMounted, onUnmounted, onBeforeUnmount} from 'vue'
import api from "~/api/config";
import { ElMessage } from 'element-plus'

const list = ref([])
const pageNo = ref(1);
const pageSize = ref(10);
const rowTotal = ref(0);
const interval = ref({});
const loading = ref(false);

// 生命周期钩子
onMounted(() => {
  getList();
  interval.value = setInterval(getList, 3000, true);
})

onUnmounted(() => {
  console.log("onUnmounted")
})

onBeforeUnmount(() => {
  console.log("onBeforeUnmount")
  clearInterval(interval.value);
})
const getList = async (autoLoading = false) => {
  if (!autoLoading){
    if (loading.value){
      return;
    }
    loading.value = true;
  }

  const result = await api.get(`/transProgress/list`, {params: {pageNo: pageNo.value, pageSize: pageSize.value}});
  list.value = result.data.records;
  rowTotal.value = Number(result.data.total);
  loading.value = false;
}
const manualTranscode = async (fileId) => {
  if (loading.value){
    return;
  }
  loading.value = true;
  const result = await api.post(`/file/manualTranscode`, {fileId: fileId});
  getList();
  loading.value = false;
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
  getList();
}

const calcTransTime = (startTime, endTime) => {
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

</script>

<style lang="scss" scoped>
  .container{
    //padding: 15px 20px;
  }
  .page{
    margin-top: 20px;
    float: right;
  }
</style>

