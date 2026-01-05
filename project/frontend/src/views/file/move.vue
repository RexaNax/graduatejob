<template>
  <div class="container">
    <div class="tree">
      <el-tree :data="folderList" :props="defaultProps" node-key="id" @node-click="handleNodeClick" :lazy="true" :load="loadNode"
        :expand-on-click-node="false" :highlight-current="true" :default-expanded-keys="[0]">
        <template #default="{ node, data }">
          <el-icon class="icon">
            <icon-folder-opened />
          </el-icon>
          <span class="label" :title="node.label">{{ node.label }}</span>
        </template>
      </el-tree>
    </div>
  </div>
</template>


<script setup>
import {ref, shallowRef, nextTick, onMounted} from 'vue'
import api from "~/api/config";
import { ElMessage,ElLoading } from 'element-plus'

import {
  VideoCamera as IconVideoCamera,
  Document as IconDocument,
  FolderOpened as IconFolderOpened,
  Headset as IconHeadset,
  Picture as IconPicture,
  MoreFilled as IconMoreFilled,
} from "@element-plus/icons-vue";

const emits = defineEmits();
const props = defineProps(["parentId", "backOptDuration", "notifyMsgCache", "fileIds"])
const dirId = ref(0)
const targetDirId = ref(0)
const selectedIds = ref([])
const moveResourceVisible = ref(false)
const folderList = ref([{id:0, label: "全部文件", children: [], isLeaf: false}])
const selectNodeId = ref(0)
const treeNodeMap = ref({})
const successList = ref([])
const fileRepeatList = ref([])
const defaultProps = ref({
  label: 'label',
  children: 'children',
  isLeaf: 'isLeaf'
})
const confirmRepeatDialogVisible = ref(false)
const repeatFileListVisible = ref(false)
const editNodeInputVal = ref("")
const editNodeInputSaved = ref(false)
const editNodeInputVisible = ref(false)
const editNodeItem = ref(null)
const editCellFocus = ref(false)
const editCellErrorTip = ref("")
const editCellToolTipTimer = ref(0)
const editCellToolTipVisible = ref(false)

onMounted(() => {
  // getDirTree();
})

const getDirTree = async (id, parentNode, resolve) => {
  const result = await api.get(`/file/dirTree`, {
    params: {
      dirId: id,
    }
  });
  console.log("getDirTree", result)
  if (typeof (parentNode) == "undefined" || parentNode == null) {
    return;
  }
  if(typeof (resolve) != "undefined") {
    resolve(result.data);
  }
}

const handleNodeClick = (data) => {
  // if (moveForm.value.id !== data.id){
  //   if(typeof (this.$refs.resMoveTree.store.nodesMap[data.id]) != "undefined") {
  //     this.$refs.resMoveTree.store.nodesMap[data.id].expanded = true;
  //   }
  // }
  emits("setMoveTargetDirId", data.id)
}

const handleNodeExpand = (data) => {
  this.resetMoveTreeCurrentNode(data.id)
}

const handleNodeCollapse = (data) => {
  this.resetMoveTreeCurrentNode(data.id)
}

const loadNode = (node, resolve) => {
  console.log('loadNode', node.data.id, node.level, typeof (resolve),new Date());
  if(node.level == 0) {
    return resolve(folderList.value)
  } else  {
    getDirTree(node.data.id, node, resolve);
  }
}
</script>

<style scoped>
.tree{
  height: 360px;
  overflow: auto;
  margin-bottom: 20px;
}
.label{
  margin-left: 10px;
}
/*:deep(.ep-tree){*/
/*  background: #faf8f8;*/
/*}*/

:deep(.ep-tree-node__content){
  height: 40px;
}
</style>