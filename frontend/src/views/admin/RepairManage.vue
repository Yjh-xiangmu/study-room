<template>
  <div class="repair-manage-container">
    <div class="header-action">
      <h2 style="margin: 0; color: #333;"><el-icon><Tools /></el-icon> 设备报修处理中心</h2>
      <el-button type="primary" icon="Refresh" @click="fetchRepairs">刷新列表</el-button>
    </div>

    <el-table :data="repairList" border stripe style="width: 100%; margin-top: 20px;" v-loading="loading">
      <el-table-column prop="nickname" label="报修学生" width="120" align="center"></el-table-column>
      <el-table-column prop="roomName" label="所在门店" width="180"></el-table-column>
      <el-table-column prop="seatInfo" label="故障座位" width="150" align="center">
        <template #default="scope"><strong style="color: #F56C6C;">{{ scope.row.seatInfo }}</strong></template>
      </el-table-column>
      <el-table-column prop="content" label="故障描述" show-overflow-tooltip></el-table-column>

      <el-table-column label="报修时间" width="160" align="center">
        <template #default="scope">{{ scope.row.createTime.replace('T', ' ').substring(0, 16) }}</template>
      </el-table-column>

      <el-table-column label="当前状态" width="120" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="info">待处理</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="danger" effect="dark">维修中</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="success">已解决</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="处理操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button v-if="scope.row.status === 0" size="small" type="danger" @click="handleStart(scope.row.id)">
            开始维修 (锁座)
          </el-button>

          <el-button v-if="scope.row.status === 1" size="small" type="success" @click="handleFinish(scope.row.id)">
            维修完成 (恢复座位)
          </el-button>

          <span v-if="scope.row.status === 2" style="color: #999; font-size: 13px;">处理完毕</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Tools, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const loading = ref(false)
const repairList = ref([])

onMounted(() => { fetchRepairs() })

const fetchRepairs = async () => {
  loading.value = true
  const res = await axios.get('http://localhost:8080/api/repair/list')
  if (res.data.code === 200) repairList.value = res.data.data
  loading.value = false
}

const handleStart = (id) => {
  ElMessageBox.confirm('确认已安排师傅前往维修吗？确定后，该座位将在学生端地图上变成红色，且无法被预约！', '锁座提醒', { type: 'warning' }).then(async () => {
    const res = await axios.put(`http://localhost:8080/api/repair/start/${id}`)
    if (res.data.code === 200) { ElMessage.success(res.data.msg); fetchRepairs() }
  }).catch(()=>{})
}

const handleFinish = (id) => {
  ElMessageBox.confirm('故障已排除？确定后，该座位将重新对学生开放预约。', '恢复座位', { type: 'success' }).then(async () => {
    const res = await axios.put(`http://localhost:8080/api/repair/finish/${id}`)
    if (res.data.code === 200) { ElMessage.success(res.data.msg); fetchRepairs() }
  }).catch(()=>{})
}
</script>

<style scoped>
.repair-manage-container { background-color: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); min-height: 80vh;}
.header-action { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; padding-bottom: 15px;}
</style>
