<template>
  <div class="order-container">
    <h2 class="page-title"><el-icon><Document /></el-icon> 我的预约记录</h2>

    <el-table :data="orderList" border stripe style="width: 100%; margin-top: 20px;" v-loading="loading">
      <el-table-column prop="id" label="订单号" width="80" align="center"></el-table-column>
      <el-table-column prop="roomName" label="预约门店" width="180"></el-table-column>
      <el-table-column prop="seatInfo" label="分区与座位" width="150" align="center"></el-table-column>

      <el-table-column label="预约时间段" width="280" align="center">
        <template #default="scope">
          {{ formatTime(scope.row.startTime) }} <strong style="color: #409EFF;">至</strong> <br/>
          {{ formatTime(scope.row.endTime) }}
        </template>
      </el-table-column>

      <el-table-column label="支付明细" width="140" align="center">
        <template #default="scope">
          <div v-if="scope.row.status === 2 || scope.row.status === 3" style="font-size: 12px; color: #999; text-decoration: line-through;">
            预付: ￥{{ scope.row.totalAmount.toFixed(2) }}
          </div>
          <strong style="color: #E6A23C; font-size: 16px;">
            实扣: ￥{{ (scope.row.actualAmount !== null && scope.row.actualAmount !== undefined ? scope.row.actualAmount : scope.row.totalAmount).toFixed(2) }}
          </strong>
        </template>
      </el-table-column>

      <el-table-column label="当前状态" width="120" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="primary">待到店核销</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="warning" effect="dark">学习中</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="success">已完成</el-tag>
          <el-tag v-else-if="scope.row.status === 3" type="info">已取消</el-tag>
          <el-tag v-else-if="scope.row.status === 4" type="danger">超时违约</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="160" align="center" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === 0"
            size="small"
            type="danger"
            plain
            @click="handleCancel(scope.row)">
            取消预约
          </el-button>

          <el-button
            v-if="scope.row.status === 1"
            size="small"
            type="warning"
            @click="handleCheckout(scope.row.id)">
            结束离座
          </el-button>

          <span v-if="scope.row.status > 1" style="color: #999; font-size: 13px;">无可用操作</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const loading = ref(false)
const orderList = ref([])

// 把 userInfo 变成响应式 ref，方便后续更新本地缓存
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

onMounted(() => {
  fetchOrders()
})

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16)
}

// 【新增核心方法】拉取后端最新用户信息并更新本地缓存
const refreshUserInfo = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/admin/user/list', { params: { keyword: userInfo.value.username } })
    if (res.data.code === 200 && res.data.data.length > 0) {
      userInfo.value = res.data.data[0] // 拿到最新余额
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value)) // 覆盖本地旧缓存
    }
  } catch (e) {
    console.error('刷新余额失败', e)
  }
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await axios.get(`http://localhost:8080/api/student/reservation/list?userId=${userInfo.value.id}`)
    if (res.data.code === 200) { orderList.value = res.data.data }
  } finally {
    loading.value = false
  }
}

// 取消预约
const handleCancel = (row) => {
  ElMessageBox.confirm(
    '15分钟内取消全额退款不扣分；超过15分钟取消将扣除1点信用分。确定取消吗？',
    '取消提示',
    { confirmButtonText: '确定取消', cancelButtonText: '暂不取消', type: 'warning' }
  ).then(async () => {
    const res = await axios.put(`http://localhost:8080/api/student/reservation/cancel/${row.id}`)
    if (res.data.code === 200) {
      ElMessage.success(res.data.msg)
      await refreshUserInfo() // 退款成功后，立刻拉取最新余额存入浏览器
      fetchOrders()
    }
  }).catch(() => {})
}

// 学生主动结束离座结算
const handleCheckout = (id) => {
  ElMessageBox.confirm(
    '确认现在结束学习离开吗？系统将自动核算并退还【未开始的完整小时】费用。不足1小时不予退费。',
    '离座结算提示',
    { confirmButtonText: '确认离座', cancelButtonText: '继续学习', type: 'info' }
  ).then(async () => {
    const res = await axios.put(`http://localhost:8080/api/student/reservation/checkout/${id}`)
    if (res.data.code === 200) {
      ElMessage({ message: res.data.msg, type: 'success', duration: 5000 })
      await refreshUserInfo() // 退款成功后，立刻拉取最新余额存入浏览器
      fetchOrders() // 刷新订单列表，看到最新的真实扣费
    } else {
      ElMessage.error(res.data.msg)
    }
  }).catch(() => {})
}
</script>

<style scoped>
.order-container { background-color: #fff; padding: 30px; border-radius: 8px; min-height: 80vh; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.page-title { color: #333; display: flex; align-items: center; gap: 10px; margin-top: 0; padding-bottom: 20px; border-bottom: 1px solid #eee;}
</style>
