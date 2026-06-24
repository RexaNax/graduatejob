<template>
  <el-watermark :content="props.pdfWatermark">
    <el-image :src="props.previewUrl" />
  </el-watermark>
</template>

<script setup>

import {ref, onMounted, onUnmounted, onBeforeUnmount, reactive} from 'vue'
import api from "~/api/config";
const props = defineProps(["fileId", "fileName", "previewUrl", "pdfWatermark"])

// 生命周期钩子
onMounted(() => {

})

onUnmounted(() => {
  console.log("onUnmounted")
})

onBeforeUnmount(() => {
  console.log("onBeforeUnmount")
})

</script>

<style lang=scss scoped>

</style>