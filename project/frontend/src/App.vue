<template>
  <el-config-provider namespace="ep">
    <el-container style="height: 100vh">
      <el-header><BaseNav/></el-header>
      <el-container>
        <el-aside width="height: calc(100vh - 44px)">
          <BaseSide></BaseSide>
        </el-aside>
        <el-main><router-view/></el-main>
      </el-container>
    </el-container>
  </el-config-provider>
</template>

<style>
#app {
  color: var(--ep-text-color-primary);
}
.container{
  max-width: 100% !important;
}
.ep-aside{
  width: 200px;
}
.ep-main{
  padding: 20px;
}
.ep-header{
  padding: 0;
  height: 44px;
}
.ep-dialog__body{
  padding: 10px 20px!important;
}

.ep-menu {
  border-right: solid 2px #edf5fc;
}
.main-container {
  height: calc(100vh - var(--ep-menu-item-height) - 3px);
}
</style>
<script setup lang="ts">
</script>
