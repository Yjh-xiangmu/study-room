<template>
  <div class="repair-container">
    <div class="header-action">
      <h2 style="margin: 0; color: #333;"><el-icon><Warning /></el-icon> 设备故障报修</h2>
      <el-button type="danger" icon="Plus" @click="dialogVisible = true" size="large">我要报修</el-button>
    </div>

    <el-table :data="repairList" border stripe style="width: 100%; margin-top: 20px;" v-loading="loading">
      <el-table-column prop="roomName" label="所在门店" width="180"></el-table-column>
      <el-table-column prop="seatInfo" label="故障座位" width="150" align="center"></el-table-column>
      <el-table-column prop="content" label="故障描述" show-overflow-tooltip></el-table-column>
      <el-table-column label="提交时间" width="180" align="center">
        <template #default="scope">{{ scope.row.createTime.replace('T', ' ').substring(0, 16) }}</template>
      </el-table-column>
      <el-table-column label="处理进度" width="150" align="center" fixed="right">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="info">待处理</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="danger" effect="dark">维修中</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="success">已解决</el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="填写报修单" v-model="dialogVisible" width="500px" @close="resetForm">
      <el-form :model="form" label-width="100px">
        <el-form-item label="故障门店">
          <el-select v-model="form.roomId" placeholder="请选择门店" style="width: 100%" @change="handleRoomChange">
            <el-option v-for="room in roomList" :key="room.id" :label="room.name" :value="room.id"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="所在分区">
          <el-select v-model="form.zoneId" placeholder="请选择分区" style="width: 100%" :disabled="!form.roomId" @change="handleZoneChange">
            <el-option v-for="zone in zoneList" :key="zone.id" :label="zone.name" :value="zone.id"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="具体座位号">
          <el-select v-model="form.seatId" placeholder="请选择出故障的座位" style="width: 100%" :disabled="!form.zoneId">
            <el-option v-for="seat in seatList" :key="seat.id" :label="seat.seatNo + ' 号'" :value="seat.id"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="故障描述">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="例如：台灯不亮了 / 插座没电 / 椅子腿松动..."></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="danger" @click="submitRepair">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Warning, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const loading = ref(false)
const repairList = ref([])

// 弹窗与表单数据
const dialogVisible = ref(false)
const roomList = ref([])
const zoneList = ref([])
const seatList = ref([])
const form = reactive({ roomId: null, zoneId: null, seatId: null, content: '' })

onMounted(() => {
  fetchMyRepairs()
  fetchRooms()
})

const fetchMyRepairs = async () => {
  loading.value = true
  const res = await axios.get(`http://localhost:8080/api/repair/list?userId=${userInfo.id}`)
  if (res.data.code === 200) repairList.value = res.data.data
  loading.value = false
}

// 级联获取数据
const fetchRooms = async () => {
  const res = await axios.get('http://localhost:8080/api/admin/room/list')
  if (res.data.code === 200) roomList.value = res.data.data
}
const handleRoomChange = async (roomId) => {
  form.zoneId = null; form.seatId = null; seatList.value = [];
  const res = await axios.get(`http://localhost:8080/api/admin/zone/list?roomId=${roomId}`)
  if (res.data.code === 200) zoneList.value = res.data.data
}
const handleZoneChange = async (zoneId) => {
  form.seatId = null
  const res = await axios.get(`http://localhost:8080/api/admin/seat/list?roomId=${form.roomId}`)
  if (res.data.code === 200) {
    seatList.value = res.data.data.filter(s => s.zoneId === zoneId)
  }
}

const submitRepair = async () => {
  if (!form.seatId || !form.content) return ElMessage.warning('请选择故障座位并填写描述！')
  const res = await axios.post('http://localhost:8080/api/repair/submit', {
    userId: userInfo.id, roomId: form.roomId, seatId: form.seatId, content: form.content
  })
  if (res.data.code === 200) {
    ElMessage.success(res.data.msg)
    dialogVisible.value = false
    fetchMyRepairs()
  }
}
const resetForm = () => { Object.assign(form, { roomId: null, zoneId: null, seatId: null, content: '' }); zoneList.value = []; seatList.value = []; }
</script>

<style scoped>
.repair-container { background-color: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); min-height: 80vh;}
.header-action { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; padding-bottom: 15px;}
</style>
