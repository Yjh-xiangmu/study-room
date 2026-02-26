<template>
  <div class="seat-manage-container">
    <div class="header-action">
      <div class="search-box">
        <el-select v-model="searchRoomId" placeholder="按门店筛选座位" clearable @change="fetchSeatData" style="width: 250px; margin-right: 15px;">
          <el-option v-for="room in roomList" :key="room.id" :label="room.name" :value="room.id"></el-option>
        </el-select>
        <el-button type="primary" @click="fetchSeatData">刷新数据</el-button>
      </div>
      <el-button type="success" @click="handleAdd">+ 新增座位</el-button>
    </div>

    <el-table :data="seatList" border stripe style="width: 100%; margin-top: 20px;" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" align="center"></el-table-column>
      <el-table-column label="所属门店" width="180">
        <template #default="scope">{{ getRoomName(scope.row.roomId) }}</template>
      </el-table-column>
      <el-table-column label="所属分区" width="120">
        <template #default="scope">
          <el-tag size="small">{{ getZoneName(scope.row.zoneId) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分区配套设施" show-overflow-tooltip>
        <template #default="scope">
          <span style="color: #666; font-size: 13px;">{{ getZoneFacilities(scope.row.zoneId) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="seatNo" label="具体座位号" width="120" align="center">
        <template #default="scope"><strong style="color:#409EFF">{{ scope.row.seatNo }}</strong></template>
      </el-table-column>
      <el-table-column label="当前状态" width="120" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="success">空闲</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="warning">使用中</el-tag>
          <el-tag v-else type="danger">维修中</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" @close="resetForm">
      <el-form :model="form" label-width="100px">
        <el-form-item label="归属门店">
          <el-select v-model="form.roomId" placeholder="请选择自习室门店" style="width: 100%;" @change="form.zoneId = null">
            <el-option v-for="room in roomList" :key="room.id" :label="room.name" :value="room.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="所属分区">
          <el-select v-model="form.zoneId" placeholder="请先选择门店, 再选分区" style="width: 100%;" :disabled="!form.roomId">
            <el-option
              v-for="zone in allZoneList.filter(z => z.roomId === form.roomId)"
              :key="zone.id" :label="zone.name" :value="zone.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="该区设施" v-if="form.zoneId">
          <span style="color: #909399;">{{ getZoneFacilities(form.zoneId) || '暂无专属设施' }}</span>
        </el-form-item>

        <el-form-item label="座位号">
          <el-input v-model="form.seatNo" placeholder="例如: 01 或者 A01"></el-input>
        </el-form-item>

        <el-form-item label="当前状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">空闲</el-radio>
            <el-radio :label="1">使用中</el-radio>
            <el-radio :label="2">设备维修中</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const searchRoomId = ref(null)
const loading = ref(false)
const seatList = ref([])
const roomList = ref([])
const allZoneList = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增座位')

const form = reactive({ id: null, roomId: null, zoneId: null, seatNo: '', status: 0 })

onMounted(async () => {
  await fetchRoomData()
  await fetchAllZoneData()
  fetchSeatData()
})

const fetchRoomData = async () => {
  const res = await axios.get('http://localhost:8080/api/admin/room/list')
  if (res.data.code === 200) roomList.value = res.data.data
}

const fetchAllZoneData = async () => {
  const res = await axios.get('http://localhost:8080/api/admin/zone/list')
  if (res.data.code === 200) allZoneList.value = res.data.data
}

const fetchSeatData = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/admin/seat/list', { params: { roomId: searchRoomId.value || null } })
    if (res.data.code === 200) seatList.value = res.data.data
  } catch (error) {
    ElMessage.error('获取座位失败！')
  } finally {
    loading.value = false
  }
}

const getRoomName = (roomId) => {
  const room = roomList.value.find(r => r.id === roomId)
  return room ? room.name : '未知'
}
const getZoneName = (zoneId) => {
  const zone = allZoneList.value.find(z => z.id === zoneId)
  return zone ? zone.name : '未知分区'
}
// 新增：用于在座位列表里反向查出该分区的硬件设施
const getZoneFacilities = (zoneId) => {
  const zone = allZoneList.value.find(z => z.id === zoneId)
  return zone ? zone.facilities : ''
}

const handleAdd = () => { dialogTitle.value = '新增座位'; dialogVisible.value = true }
const handleEdit = (row) => { dialogTitle.value = '编辑座位'; dialogVisible.value = true; Object.assign(form, row) }
const resetForm = () => { Object.assign(form, { id: null, roomId: null, zoneId: null, seatNo: '', status: 0 }) }

const submitForm = async () => {
  if (!form.roomId || !form.zoneId || !form.seatNo) return ElMessage.warning('门店、分区、座位号为必填项！')
  const url = form.id ? 'update' : 'add'
  const method = form.id ? 'put' : 'post'
  const res = await axios[method](`http://localhost:8080/api/admin/seat/${url}`, form)
  if (res.data.code === 200) { ElMessage.success('保存成功'); dialogVisible.value = false; fetchSeatData() }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这个座位吗?', '提示').then(async () => {
    await axios.delete(`http://localhost:8080/api/admin/seat/delete/${id}`)
    ElMessage.success('删除成功'); fetchSeatData()
  }).catch(()=>{})
}
</script>

<style scoped>
.seat-manage-container { background-color: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.header-action { display: flex; justify-content: space-between; align-items: center; }
.search-box { display: flex; align-items: center; }
</style>
