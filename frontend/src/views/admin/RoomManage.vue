<template>
  <div class="room-manage-container">
    <div class="header-action">
      <div class="search-box">
        <el-input v-model="searchName" placeholder="请输入自习室名称" clearable style="width: 250px; margin-right: 15px;"></el-input>
        <el-button type="primary" @click="fetchData">搜索</el-button>
      </div>
      <el-button type="success" @click="handleAdd">+ 新增自习室门店</el-button>
    </div>

    <el-table :data="roomList" border stripe style="width: 100%; margin-top: 20px;" v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" align="center"></el-table-column>
      <el-table-column prop="name" label="门店名称" width="180"></el-table-column>
      <el-table-column prop="location" label="物理位置" show-overflow-tooltip></el-table-column>
      <el-table-column label="营业时间" width="150" align="center">
        <template #default="scope">{{ scope.row.openTime }} - {{ scope.row.closeTime }}</template>
      </el-table-column>
      <el-table-column prop="status" label="营业状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ scope.row.status === 1 ? '正常营业' : '维护停业' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" align="center" fixed="right">
        <template #default="scope">
          <el-button size="small" type="warning" plain @click="handleManageZone(scope.row)">管理分区</el-button>
          <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px" @close="resetForm">
      <el-form :model="form" label-width="100px">
        <el-form-item label="门店名称"><el-input v-model="form.name"></el-input></el-form-item>
        <el-form-item label="物理位置"><el-input v-model="form.location"></el-input></el-form-item>
        <el-form-item label="营业时间">
          <el-time-select v-model="form.openTime" start="06:00" step="00:30" end="23:30" style="width: 140px; margin-right: 10px;" />
          至
          <el-time-select v-model="form.closeTime" start="06:00" step="00:30" end="23:59" style="width: 140px; margin-left: 10px;" />
        </el-form-item>
        <el-form-item label="门店状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常营业</el-radio>
            <el-radio :label="0">维护停业</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="门店简介"><el-input v-model="form.description" type="textarea" rows="3"></el-input></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定保存</el-button>
      </template>
    </el-dialog>

    <el-dialog :title="`${currentRoomName} - 分区管理`" v-model="zoneListDialogVisible" width="700px">
      <div style="margin-bottom: 15px;">
        <el-button type="primary" @click="handleAddZone">+ 新增分区</el-button>
      </div>
      <el-table :data="zoneList" border size="small">
        <el-table-column prop="name" label="分区名称" width="120"></el-table-column>
        <el-table-column label="收费标准" width="120" align="center">
          <template #default="scope">
            <strong style="color: #f56c6c;">￥{{ scope.row.hourlyPrice }} / 时</strong>
          </template>
        </el-table-column>
        <el-table-column prop="facilities" label="硬件设施(标配)" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" text @click="handleEditZone(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" text @click="handleDeleteZone(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog :title="zoneForm.id ? '编辑分区' : '新增分区'" v-model="zoneFormDialogVisible" width="450px" @close="resetZoneForm">
      <el-form :model="zoneForm" label-width="100px">
        <el-form-item label="分区名称">
          <el-input v-model="zoneForm.name" placeholder="如: 沉浸区"></el-input>
        </el-form-item>
        <el-form-item label="收费标准">
          <el-input-number v-model="zoneForm.hourlyPrice" :min="0" :precision="2" :step="0.5"></el-input-number>
          <span style="margin-left: 10px; color: #909399;">元 / 小时</span>
        </el-form-item>
        <el-form-item label="硬件设施">
          <el-input v-model="zoneForm.facilities" type="textarea" rows="3" placeholder="如: 独立插座, 护眼灯, 储物柜"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="zoneFormDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitZoneForm">确定保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const searchName = ref('')
const loading = ref(false)
const roomList = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增门店')
const form = reactive({ id: null, name: '', location: '', openTime: '08:00', closeTime: '22:00', description: '', status: 1 })

// --- 分区管理状态 ---
const zoneListDialogVisible = ref(false)
const currentRoomId = ref(null)
const currentRoomName = ref('')
const zoneList = ref([])

// --- 单个分区表单状态 (增加了 hourlyPrice 字段) ---
const zoneFormDialogVisible = ref(false)
const zoneForm = reactive({ id: null, roomId: null, name: '', facilities: '', hourlyPrice: 0.00 })

onMounted(() => { fetchData() })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/admin/room/list', { params: { name: searchName.value } })
    roomList.value = res.data.data
  } finally {
    loading.value = false
  }
}

// 门店操作
const handleAdd = () => { dialogTitle.value = '新增门店'; dialogVisible.value = true }
const handleEdit = (row) => { dialogTitle.value = '编辑门店'; dialogVisible.value = true; Object.assign(form, row) }
const resetForm = () => { Object.assign(form, { id: null, name: '', location: '', openTime: '08:00', closeTime: '22:00', description: '', status: 1 }) }
const submitForm = async () => {
  if (!form.name || !form.location) return ElMessage.warning('名称和位置不能为空')
  const url = form.id ? 'update' : 'add'
  const method = form.id ? 'put' : 'post'
  await axios[method](`http://localhost:8080/api/admin/room/${url}`, form)
  ElMessage.success('保存成功'); dialogVisible.value = false; fetchData()
}
const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除门店?', '提示').then(async () => {
    await axios.delete(`http://localhost:8080/api/admin/room/delete/${id}`)
    ElMessage.success('删除成功'); fetchData()
  }).catch(()=>{})
}

// --- 分区列表业务 ---
const handleManageZone = (row) => {
  currentRoomId.value = row.id
  currentRoomName.value = row.name
  zoneListDialogVisible.value = true
  fetchZones()
}
const fetchZones = async () => {
  const res = await axios.get('http://localhost:8080/api/admin/zone/list', { params: { roomId: currentRoomId.value } })
  zoneList.value = res.data.data
}

// --- 分区表单业务 ---
const handleAddZone = () => {
  zoneForm.roomId = currentRoomId.value
  zoneFormDialogVisible.value = true
}
const handleEditZone = (row) => {
  Object.assign(zoneForm, row)
  zoneFormDialogVisible.value = true
}
const resetZoneForm = () => {
  Object.assign(zoneForm, { id: null, roomId: null, name: '', facilities: '', hourlyPrice: 0.00 })
}
const submitZoneForm = async () => {
  if (!zoneForm.name) return ElMessage.warning('请输入分区名称')
  const url = zoneForm.id ? 'update' : 'add'
  const method = zoneForm.id ? 'put' : 'post'
  await axios[method](`http://localhost:8080/api/admin/zone/${url}`, zoneForm)
  ElMessage.success('保存成功')
  zoneFormDialogVisible.value = false
  fetchZones()
}
const handleDeleteZone = async (id) => {
  ElMessageBox.confirm('确定要删除此分区吗?', '警告').then(async () => {
    await axios.delete(`http://localhost:8080/api/admin/zone/delete/${id}`)
    ElMessage.success('删除成功'); fetchZones()
  }).catch(()=>{})
}
</script>

<style scoped>
.room-manage-container { background-color: #fff; padding: 20px; border-radius: 8px; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.header-action { display: flex; justify-content: space-between; align-items: center; }
.search-box { display: flex; align-items: center; }
</style>
