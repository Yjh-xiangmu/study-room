<template>
  <div class="feedback-container">
    <div class="header-action">
      <h2 style="margin: 0; color: #333;"><el-icon><Message /></el-icon> 意见反馈箱</h2>
      <el-button type="primary" icon="Refresh" @click="fetchFeedbacks">刷新列表</el-button>
    </div>

    <el-table :data="feedbackList" border stripe style="width: 100%; margin-top: 20px;" v-loading="loading">
      <el-table-column type="index" label="序号" width="60" align="center"></el-table-column>
      <el-table-column label="反馈用户" width="180">
        <template #default="scope">
          <div style="font-weight: bold;">{{ scope.row.nickname }}</div>
          <div style="font-size: 12px; color: #999;">{{ scope.row.phone }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="content" label="反馈内容" show-overflow-tooltip></el-table-column>
      <el-table-column label="提交时间" width="180" align="center">
        <template #default="scope">
          {{ scope.row.createTime.replace('T', ' ').substring(0, 16) }}
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Message, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const loading = ref(false)
const feedbackList = ref([])

onMounted(() => {
  fetchFeedbacks()
})

const fetchFeedbacks = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/admin/feedback/list')
    if (res.data.code === 200) {
      feedbackList.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取反馈失败！')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.feedback-container { background-color: #fff; padding: 30px; border-radius: 8px; min-height: 80vh; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.header-action { display: flex; justify-content: space-between; align-items: center; padding-bottom: 20px; border-bottom: 1px solid #f0f2f5; }
</style>
