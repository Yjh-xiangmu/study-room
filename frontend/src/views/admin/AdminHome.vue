<template>
  <div class="admin-home-container">
    <div class="page-title">
      <h2><el-icon><DataLine /></el-icon> 实时数据中心</h2>
      <span class="subtitle">数据更新时间: {{ currentTime }}</span>
    </div>

    <el-row :gutter="20" class="data-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="card-item bg-blue">
          <div class="card-icon"><el-icon><User /></el-icon></div>
          <div class="card-info">
            <div class="card-title">注册学生总数</div>
            <div class="card-num">{{ stats.userCount }} <span class="unit">人</span></div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="card-item bg-orange">
          <div class="card-icon"><el-icon><Money /></el-icon></div>
          <div class="card-info">
            <div class="card-title">系统累计营收</div>
            <div class="card-num">￥{{ stats.totalRevenue?.toFixed(2) }}</div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="card-item bg-green">
          <div class="card-icon"><el-icon><List /></el-icon></div>
          <div class="card-info">
            <div class="card-title">历史订单总数</div>
            <div class="card-num">{{ stats.orderCount }} <span class="unit">笔</span></div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card shadow="hover" class="card-item bg-purple">
          <div class="card-icon"><el-icon><OfficeBuilding /></el-icon></div>
          <div class="card-info">
            <div class="card-title">营业门店数量</div>
            <div class="card-num">{{ stats.roomCount }} <span class="unit">家</span></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span style="font-weight: bold; color: #333;">🔥 各大门店预约热度分布</span>
          </template>
          <div ref="pieChartRef" class="chart-box"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover" class="chart-card">
          <template #header>
            <span style="font-weight: bold; color: #333;">📊 全站订单状态监控</span>
          </template>
          <div ref="barChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { DataLine, User, List, OfficeBuilding, Money } from '@element-plus/icons-vue'
import axios from 'axios'
import * as echarts from 'echarts'

const currentTime = ref(new Date().toLocaleString())
const stats = ref({ userCount: 0, totalRevenue: 0, orderCount: 0, roomCount: 0 })

// 图表 DOM 引用
const pieChartRef = ref(null)
const barChartRef = ref(null)

let pieChart = null
let barChart = null

onMounted(async () => {
  await fetchStatsData()
  initCharts()

  // 监听窗口大小变化，让图表自动缩放适配屏幕
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (pieChart) pieChart.dispose()
  if (barChart) barChart.dispose()
})

const handleResize = () => {
  if (pieChart) pieChart.resize()
  if (barChart) barChart.resize()
}

let roomChartData = []
let statusChartData = []

const fetchStatsData = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/admin/home/stats')
    if (res.data.code === 200) {
      stats.value = res.data.data
      roomChartData = res.data.data.roomChartData
      statusChartData = res.data.data.statusChartData
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

// 初始化 ECharts 图表
const initCharts = () => {
  // 1. 绘制饼图
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
    pieChart.setOption({
      tooltip: { trigger: 'item', formatter: '{b} : {c}单 ({d}%)' },
      legend: { orient: 'vertical', left: 'left' },
      series: [
        {
          name: '预约门店',
          type: 'pie',
          radius: '60%',
          center: ['55%', '50%'],
          data: roomChartData,
          emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } }
        }
      ]
    })
  }

  // 2. 绘制柱状图
  if (barChartRef.value) {
    barChart = echarts.init(barChartRef.value)
    barChart.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: ['待核销', '学习中', '已完成', '已取消', '超时违约'],
        axisTick: { alignWithLabel: true }
      },
      yAxis: { type: 'value' },
      series: [
        {
          name: '订单数量',
          type: 'bar',
          barWidth: '40%',
          // 给不同状态的柱子配置不同的颜色
          data: [
            { value: statusChartData[0], itemStyle: { color: '#409EFF' } }, // 蓝
            { value: statusChartData[1], itemStyle: { color: '#E6A23C' } }, // 黄
            { value: statusChartData[2], itemStyle: { color: '#67C23A' } }, // 绿
            { value: statusChartData[3], itemStyle: { color: '#909399' } }, // 灰
            { value: statusChartData[4], itemStyle: { color: '#F56C6C' } }  // 红
          ]
        }
      ]
    })
  }
}
</script>

<style scoped>
.admin-home-container { min-height: 80vh; }
.page-title { margin-bottom: 25px; display: flex; align-items: flex-end; gap: 15px; }
.page-title h2 { margin: 0; color: #303133; }
.subtitle { font-size: 13px; color: #909399; margin-bottom: 3px; }

/* 顶部卡片样式 */
.data-cards { margin-bottom: 30px; }
.card-item {
  border-radius: 10px; border: none; color: white;
  display: flex; align-items: center; padding: 10px;
}
.bg-blue { background: linear-gradient(135deg, #409EFF 0%, #79bbff 100%); }
.bg-orange { background: linear-gradient(135deg, #E6A23C 0%, #f3d19e 100%); }
.bg-green { background: linear-gradient(135deg, #67C23A 0%, #95d475 100%); }
.bg-purple { background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%); }

.card-item :deep(.el-card__body) {
  display: flex; align-items: center; width: 100%; padding: 10px;
}
.card-icon { font-size: 48px; margin-right: 20px; opacity: 0.8; }
.card-info { display: flex; flex-direction: column; }
.card-title { font-size: 14px; opacity: 0.9; margin-bottom: 8px; }
.card-num { font-size: 28px; font-weight: bold; letter-spacing: 1px; }
.card-num .unit { font-size: 14px; font-weight: normal; margin-left: 2px;}

/* 图表样式 */
.chart-row { margin-bottom: 20px; }
.chart-card { border-radius: 8px; border: none; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05); }
.chart-box { height: 350px; width: 100%; }
</style>
