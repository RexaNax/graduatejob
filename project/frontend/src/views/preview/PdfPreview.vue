<template>
  <el-watermark :content="props.pdfWatermark">
    <div class="container">
      <iframe :src="pdfPreviewUrl" frameborder="0" style="width: 100%; height: 600px"></iframe>
    </div>
  </el-watermark>
</template>

<script setup>

import {ref, onMounted, onUnmounted, onBeforeUnmount, reactive} from 'vue'
import api from "~/api/config";
const props = defineProps(["fileId", "fileName", "previewUrl", "pdfWatermark"])
const pdfPreviewUrl = ref("");

const previewPdf = () => {
  pdfPreviewUrl.value = '/pdf/web/viewer.html?file=' + encodeURIComponent(props.previewUrl);
}

// 生命周期钩子
onMounted(() => {
  previewPdf();
})

onUnmounted(() => {
  console.log("onUnmounted")
})

onBeforeUnmount(() => {
  console.log("onBeforeUnmount")
})

</script>

<style lang=scss scoped>
.container{
  border-bottom: solid 5px #d4d4d7;
  background: #d4d4d7;
}
</style>
