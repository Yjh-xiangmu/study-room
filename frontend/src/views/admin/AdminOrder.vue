<template>
  <div class="admin-order-container">
    <div class="header-action">
      <h2 style="margin: 0; color: #333;"><el-icon><List /></el-icon> 订单流水管理</h2>
      <el-button type="primary" icon="Refresh" @click="fetchOrders">刷新流水</el-button>
    </div>

    <div class="search-bar">
      <el-input
        v-model="searchPhone"
        placeholder="输入学生手机号精确查找..."
        clearable
        prefix-icon="Search"
        style="width: 250px; margin-right: 15px;"
        @keyup.enter="fetchOrders"
      ></el-input>

      <el-select v-model="searchStatus" placeholder="筛选订单状态" clearable style="width: 180px; margin-right: 15px;" @change="fetchOrders">
        <el-option label="全部状态" value=""></el-option>
        <el-option label="待核销 (0)" :value="0"></el-option>
        <el-option label="学习中 (1)" :value="1"></el-option>
        <el-option label="已完成 (2)" :value="2"></el-option>
        <el-option label="已取消 (3)" :value="3"></el-option>
        <el-option label="超时违约 (4)" :value="4"></el-option>
      </el-select>

      <el-button type="primary" @click="fetchOrders">检索流水</el-button>
    </div>

    <el-table :data="orderList" border stripe style="width: 100%; margin-top: 20px;" v-loading="loading" max-height="650">
      <el-table-column prop="id" label="订单号" width="80" align="center"></el-table-column>

      <el-table-column label="学生信息" width="180">
        <template #default="scope">
          <div style="font-weight: bold;">{{ scope.row.nickname }}</div>
          <div style="font-size: 12px; color: #999;">{{ scope.row.phone }}</div>
        </template>
      </el-table-column>

      <el-table-column label="门店与座位" width="220">
        <template #default="scope">
          <div><el-icon><OfficeBuilding /></el-icon> {{ scope.row.roomName }}</div>
          <div style="font-size: 12px; color: #666; margin-top: 4px;"><el-icon><Location /></el-icon> {{ scope.row.seatInfo }}</div>
        </template>
      </el-table-column>

      <el-table-column label="预约时间段" width="260" align="center">
        <template #default="scope">
          {{ formatTime(scope.row.startTime) }} <br/>
          <span style="color: #909399;">至</span> <br/>
          {{ formatTime(scope.row.endTime) }}
        </template>
      </el-table-column>

      <el-table-column label="订单金额" width="120" align="center">
        <template #default="scope">
          <strong style="color: #E6A23C; font-size: 16px;">￥{{ scope.row.totalAmount.toFixed(2) }}</strong>
        </template>
      </el-table-column>

      <el-table-column label="当前状态" width="120" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="primary">待核销</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="warning" effect="dark">学习中</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="success">已完成</el-tag>
          <el-tag v-else-if="scope.row.status === 3" type="info">已取消</el-tag>
          <el-tag v-else-if="scope.row.status === 4" type="danger">超时违约</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="下单时间" width="160" align="center">
        <template #default="scope">
          <span style="font-size: 13px; color: #999;">{{ formatTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="scope">
          <el-button size="small" type="danger" text @click="handleDelete(scope.row.id)">强制删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="orderList.length === 0 && !loading" description="未检索到符合条件的订单流水"></el-empty>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { List, Search, Refresh, OfficeBuilding, Location } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const loading = ref(false)
const orderList = ref([])

// 检索条件
const searchPhone = ref('')
const searchStatus = ref('')

onMounted(() => {
  fetchOrders()
})

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return timeStr.replace('T', ' ').substring(0, 16)
}

// 获取订单流水列表
const fetchOrders = async () => {
  loading.value = true
  try {
    let url = `http://localhost:8080/api/admin/reservation/list?phone=${searchPhone.value}`
    if (searchStatus.value !== '') {
      url += `&status=${searchStatus.value}`
    }
    const res = await axios.get(url)
    if (res.data.code === 200) {
      orderList.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取订单流水失败！')
  } finally {
    loading.value = false
  }
}

// 删除订单 (管理员硬核清理)
const handleDelete = (id) => {
  ElMessageBox.confirm(
    '确定要从数据库中彻底抹除这条订单记录吗？此操作不可恢复，且不触发任何退款逻辑！',
    '高危操作警告',
    { confirmButtonText: '强制删除', cancelButtonText: '取消', type: 'error' }
  ).then(async () => {
    const res = await axios.delete(`http://localhost:8080/api/admin/reservation/delete/${id}`)
    if (res.data.code === 200) {
      ElMessage.success(res.data.msg)
      fetchOrders() // 刷新列表
    }
  }).catch(() => {})
}
</script>

<style scoped>
.admin-order-container {
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  min-height: 80vh;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
.header-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f2f5;
}
.search-bar {
  display: flex;
  align-items: center;
  background-color: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
}
</style>
