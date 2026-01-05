<template>
  <div ref="containerRef" style="display: inline-block">
    <slot></slot>
    <Teleport to="body">
      <Transition @beforeEnter="handleBeforeEnter" @enter="handleEnter" @afterEnter="handleAfterEnter">
        <div v-if="showMenu" class="context-menu" :style="{ left: x + 'px', top: y + 'px' }">
          <div class="menu-list">
            <div @click="handleClick(item)" class="menu-item" v-for="(item, i) in menu" :key="item.label">
              {{ item.label }}
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>
<script setup>
import { ref } from 'vue';
import useContextMenu from './useContextMenu';
const props = defineProps({
  menu: {
    type: Array,
    default: () => [],
  },
});
const containerRef = ref(null);
const emit = defineEmits(['select']);
const { x, y, showMenu } = useContextMenu(containerRef);
// 菜单的点击事件
function handleClick(item) {
  // 选中菜单后关闭菜单
  showMenu.value = false;
  // 并返回选中的菜单
  emit('select', item);
}

function handleBeforeEnter(el) {
  el.style.height = 0;
}

function handleEnter(el) {
  el.style.height = 'auto';
  const h = el.clientHeight;
  el.style.height = 0;
  requestAnimationFrame(() => {
    el.style.height = h + 'px';
    el.style.transition = '.5s';
  });
}

function handleAfterEnter(el) {
  el.style.transition = 'none';
}
</script>
<style lang="scss" scoped>
.context-menu{
  position: absolute;
  .menu-list{
    padding:10px 10px;
    min-width: 80px;
    box-shadow: 0 0 2px 2px #f1f1f1;
    background: #fff;
    font-size: 14px;
    .menu-item{
      padding: 4px 20px 4px 5px;
      cursor: pointer;
      &:hover{
        color: green;
      }
    }
  }

}
</style>