<template>

  <div class="common-layout">
    <el-container>
      <el-header>
        <el-button type="success"
          @click="auditApprovedCheck">通过选择</el-button>
      </el-header>
      <el-main>
        <el-table :data="tableData"
          style="width: 100%"
          @selection-change="handleSelectionChange"
          ref="multipleTableRef">
          <el-table-column type="selection"
            :selectable="selectable"
            width="55" />
          <el-table-column prop="content"
            label="内容"
            width="180" />

          <el-table-column label="名称"
            width="180">
            <template #default="scope">
              {{ scope.row.username }}
            </template>
          </el-table-column>
          <el-table-column prop="position"
            label="职位"
            width="180" />
          <el-table-column label="状态"
            width="180">
            <template #default="scope">
              <el-tag type="success"
                v-if="scope.row.state == 1">审核通过</el-tag>
              <el-tag type="info"
                v-else-if="scope.row.state == 0">未审核</el-tag>
              <el-tag type="danger"
                v-else-if="scope.row.state == -1">审核未通过</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="选择">
            <template #default="scope">
              <el-button size="small"
                type="success"
                @click="auditApproved(scope.$index, scope.row)">
                通过
              </el-button>
              <el-button size="small"
                type="danger"
                @click="auditNotApproved(scope.$index, scope.row)">
                拒绝
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-main>
    </el-container>
  </div>





</template>
<script setup>



import { onMounted, onUnmounted, ref } from 'vue'
import request from '@/utils/request.js'

const multipleTableRef = ref(null)
const auditApprovedCheck = () => {
  const checked = multipleTableRef.value.getSelectionRows()
  const res = request.post('/messages/approve', checked)
}

const auditApproved = (index, row) => {
  console.log(row);
  
  const rowArray = [row];
  const res = request.post('/messages/approve', rowArray)
}

const auditNotApproved = (index, row) => {
  const rowArray = [row];
  const res = request.post('/messages/reject', rowArray)
}

const tableData = ref([])

const ready = ref(false)
let socket = null


/* 2. 创建并等待 open */
function createSocket() {
  const socket = new WebSocket("ws://47.115.163.178:8080/ws/audit");

  socket.onopen = () => {
    console.log('✅ WebSocket 已连接')
    ready.value = true
    // 可选：第一次心跳
    socket.send(JSON.stringify({ type: 'ping' }))
  }

  socket.onmessage = e => {
    const data = JSON.parse(e.data);
    // console.log(data);
    tableData.value = mergeMessages(tableData.value, data)
  }

  socket.onclose = e => {
    console.warn('❌ WebSocket 关闭', e.code, e.reason)
    ready.value = false
    // 这里可以写重连逻辑
  }

  socket.onerror = e => {
    console.error('WebSocket 异常', e)
  }
}


const getAllmessages = () => {
  request.get('/messages/all').then((res) => {
    console.log(res)
    tableData.value = res.data
    console.log(tableData.value);
  })
}

function mergeMessages(oldArr, newData) {
  const map = new Map(oldArr.map(item => [item.id, item]));

  const newMsgs = Array.isArray(newData) ? newData : [newData];
  newMsgs.forEach(msg => map.set(msg.id, msg));

  return Array.from(map.values()).sort((a, b) => a.state - b.state);
}

/* 4. 生命周期 */
onMounted(
  () => {
    createSocket()
    getAllmessages()
  }
)
onUnmounted(() => socket && socket.close())



</script>

<style scoped>
.audit-message {
  padding: 20px;
}
</style>
