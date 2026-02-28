<template>
  <div class="verify-container">
    <div class="search-header">
      <h2 style="margin: 0; color: #333;"><el-icon><Check /></el-icon> 学生入座核销</h2>
      <div class="search-box">
        <el-input
          v-model="searchPhone"
          placeholder="可输入手机号精准搜索..."
          size="large"
          clearable
          prefix-icon="Search"
          style="width: 300px; margin-right: 15px;"
          @keyup.enter="fetchOrders"
          @clear="fetchOrders"
        ></el-input>
        <el-button type="primary" size="large" @click="fetchOrders" :loading="loading">搜索 / 刷新</el-button>
      </div>
    </div>

    <el-empty v-if="orderList.length === 0" description="当前自习室没有待核销或正在学习的订单哦~"></el-empty>

    <el-table v-if="orderList.length > 0" :data="orderList" border stripe style="width: 100%; margin-top: 20px;" v-loading="loading">
      <el-table-column prop="nickname" label="学生姓名" width="120" align="center"></el-table-column>
      <el-table-column prop="phone" label="联系电话" width="150" align="center"></el-table-column>
      <el-table-column prop="roomName" label="预约门店" width="180"></el-table-column>
      <el-table-column prop="seatInfo" label="座位信息" width="150" align="center">
        <template #default="scope">
          <strong style="color: #409EFF;">{{ scope.row.seatInfo }}</strong>
        </template>
      </el-table-column>

      <el-table-column label="预约时间段" width="280" align="center">
        <template #default="scope">
          {{ formatTime(scope.row.startTime) }} <strong style="color: #909399;">至</strong> {{ formatTime(scope.row.endTime) }}
        </template>
      </el-table-column>

      <el-table-column label="已付金额" width="100" align="center">
        <template #default="scope">
          <span style="color: #E6A23C; font-weight: bold;">￥{{ scope.row.totalAmount.toFixed(2) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="当前状态" width="120" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="primary" effect="dark">待到店核销</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="warning" effect="dark">学习中</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="店长操作" width="150" align="center" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 0"
            type="success"
            @click="handleCheckin(scope.row.id)">
            确认入座
          </el-button>
          <span v-else style="color: #999; font-size: 13px;">等待学生主动离座</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Check, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const searchPhone = ref('')
const loading = ref(false)
const orderList = ref([])

// 页面一加载就去获取数据
onMounted(() => {
  fetchOrders()
})

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16)
}

// 核心查询逻辑（不再强制要求必须有手机号）
const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await axios.get(`http://localhost:8080/api/manager/reservation/search?phone=${searchPhone.value}`)
    if (res.data.code === 200) {
      orderList.value = res.data.data
    } else {
      orderList.value = []
      ElMessage.error(res.data.msg)
    }
  } catch (error) {
    ElMessage.error('获取订单列表失败，请检查网络！')
  } finally {
    loading.value = false
  }
}

// 核销入座
const handleCheckin = (id) => {
  ElMessageBox.confirm('确认学生已到店并安排入座吗？(系统将为该座位通电)', '核销确认', {
    confirmButtonText: '确认入座',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      const res = await axios.put(`http://localhost:8080/api/manager/reservation/checkin/${id}`)
      if (res.data.code === 200) {
        ElMessage.success(res.data.msg)
        fetchOrders() // 刷新列表
      } else {
        ElMessage.error(res.data.msg)
      }
    } catch (error) {
      ElMessage.error('操作失败！')
    }
  }).catch(() => {})
}
</script>

<style scoped>
.verify-container {
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  min-height: 80vh;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
.search-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f2f5;
}
.search-box {
  display: flex;
  align-items: center;
}
</style>
