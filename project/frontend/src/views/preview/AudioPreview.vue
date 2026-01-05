<template>
  <div class="container">
    <div id="vs"></div>
    <div id="canvas">
      <canvas width="450" height="100"></canvas>
    </div>
  </div>
</template>

<script setup>
import Player from 'xgplayer';
import MusicPreset, {Analyze} from 'xgplayer-music';

import {ref, onMounted, onUnmounted, onBeforeUnmount, nextTick} from 'vue'
import api from "~/api/config";

const props = defineProps(["id", "name", "previewUrl"])

const player = ref({});

// 生命周期钩子
onMounted(() => {
  console.log("audio onMounted", props)
  player.value = new Player({
    id: 'vs',
    mediaType: 'audio',
    volume: 0.8,
    width: 900,
    height: 60,
    url: props.previewUrl,
    controls: {
      initShow: true,
      mode: 'flex'
    },
    marginControls: false,
    videoConfig: {
      crossOrigin: "anonymous"
    },
    preset: ['default', MusicPreset],
  })

  nextTick(() => {
    player.value.play();
    window.analyze = new Analyze(player.value, document.querySelector('canvas'), {
      bgColor: 'rgba(0,0,0,0.65)',
      stroke: 1,
    })
  })


})

onUnmounted(() => {
  console.log("onUnmounted")
})

onBeforeUnmount(() => {
  console.log("onBeforeUnmount")
  // player.value.destroy()
})

</script>

