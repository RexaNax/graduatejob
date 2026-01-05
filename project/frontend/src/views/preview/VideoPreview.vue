<template>
  <div class="container">
    <div id="mse"></div>
  </div>
</template>

<script setup>
import Player from 'xgplayer';
import Mp4Player from "xgplayer-mp4";
import HlsPlayer from "xgplayer-hls";
import 'xgplayer/dist/index.min.css';


import {ref, onMounted, onUnmounted, onBeforeUnmount} from 'vue'
import api from "~/api/config";

const props = defineProps(["fileId", "fileName", "previewUrl", "suffix"])
const player = ref()

// 生命周期钩子
onMounted(() => {
  console.log("video onMounted", props)
  previewXg();
})

onUnmounted(() => {
  console.log("onUnmounted")
})

onBeforeUnmount(() => {
  console.log("onBeforeUnmount")
  player.value.destroy()
})
const previewXg = () => {
  let config = {
    id: 'mse',
    autoplay: true,
    volume: 0.3,
    url: props.previewUrl,
    playsinline: true,
    height: 500,
    width: 950,
    // controls: false,
    plugins: [],
    errorTips: `请<span>刷新</span>试试`,
  }
  console.log("suffix", props.suffix)
  if (props.suffix.indexOf(".mp4") > -1){
    config.plugins.push(Mp4Player)
  }else if (props.suffix.indexOf(".m3u8") > -1){
    config.plugins.push(HlsPlayer)
  }
  console.log(config)
  player.value = new Player(config);
}
</script>

<style lang=scss scoped>

</style>