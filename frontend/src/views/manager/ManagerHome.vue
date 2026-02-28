<template>
  <div class="manager-home">
    <div class="page-title">
      <h2><el-icon><DataBoard /></el-icon> 门店实时看板</h2>
      <span class="subtitle">实时监控线下门店运营状况</span>
    </div>

    <el-row :gutter="20" class="data-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="card-item bg-blue">
          <div class="card-info">
            <div class="card-title">当前正在学习</div>
            <div class="card-num">{{ stats.studyingCount }} <span class="unit">人</span></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="card-item bg-orange">
          <div class="card-info">
            <div class="card-title">待到店核销</div>
            <div class="card-num">{{ stats.pendingCount }} <span class="unit">人</span></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="card-item bg-green">
          <div class="card-info">
            <div class="card-title">今日完成订单</div>
            <div class="card-num">{{ stats.todayOrderCount }} <span class="unit">笔</span></div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="card-item bg-red">
          <div class="card-info">
            <div class="card-title">今日实际营收</div>
            <div class="card-num">￥{{ stats.todayRevenue?.toFixed(2) }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="welcome-box">
      <h3>店长工作指南：</h3>
      <p>1. 当学生到店时，请点击左侧【入座核销】，输入手机号并确认入座，座位会自动通电计费。</p>
      <p>2. 学生如果需要充值，请在线下收取费用后，点击【余额充值】为学生账户增加余额。</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { DataBoard } from '@element-plus/icons-vue'
import axios from 'axios'

const stats = ref({ studyingCount: 0, pendingCount: 0, todayOrderCount: 0, todayRevenue: 0 })

onMounted(async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/manager/home/stats')
    if (res.data.code === 200) {
      stats.value = res.data.data
    }
  } catch (e) {}
})
</script>

<style scoped>
.page-title { margin-bottom: 25px; display: flex; align-items: flex-end; gap: 15px; }
.page-title h2 { margin: 0; color: #303133; }
.subtitle { font-size: 13px; color: #909399; margin-bottom: 3px; }

.data-cards { margin-bottom: 30px; }
.card-item { border-radius: 10px; border: none; color: white; padding: 10px; }
.bg-blue { background: linear-gradient(135deg, #409EFF 0%, #79bbff 100%); }
.bg-orange { background: linear-gradient(135deg, #E6A23C 0%, #f3d19e 100%); }
.bg-green { background: linear-gradient(135deg, #67C23A 0%, #95d475 100%); }
.bg-red { background: linear-gradient(135deg, #F56C6C 0%, #f89898 100%); }

.card-title { font-size: 15px; opacity: 0.9; margin-bottom: 8px; }
.card-num { font-size: 30px; font-weight: bold; }
.unit { font-size: 14px; font-weight: normal; }

.welcome-box { background-color: #fff; padding: 25px; border-radius: 8px; line-height: 1.8; color: #606266; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
</style>
