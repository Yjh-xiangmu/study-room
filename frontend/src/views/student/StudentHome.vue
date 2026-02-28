<template>
  <div class="student-home-container">
    <el-row :gutter="20" class="top-board">
      <el-col :span="8">
        <el-card shadow="hover" class="countdown-card">
          <div class="card-header">距离 2027 考研仅剩</div>
          <div class="countdown-days">
            <span class="num">{{ daysLeft }}</span> <span class="unit">天</span>
          </div>
          <div class="card-footer">稳住心态，顶峰相见！</div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="hover" class="stat-card" v-loading="loadingStats">
          <div class="stat-title">我的学习战绩</div>
          <div class="stat-content">
            <div class="stat-item">
              <div class="stat-label">累计学习时长</div>
              <div class="stat-value" style="color: #10b981;">
                {{ stats.totalHours }} <span class="unit">小时</span>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-label">完成预约次数</div>
              <div class="stat-value" style="color: #409EFF;">
                {{ stats.totalCount }} <span class="unit">次</span>
              </div>
            </div>
            <div class="stat-item">
              <div class="stat-label">超越了</div>
              <div class="stat-value" style="color: #E6A23C;">
                {{ stats.beatPercentage }}% <span class="unit">的研友</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="section-title">
      <h3>🔥 优质自习室推荐</h3>
      <el-button type="primary" link @click="$router.push('/student/booking')">查看全部 ></el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="8" v-for="room in roomList" :key="room.id" style="margin-bottom: 20px;">
        <el-card shadow="hover" class="room-card" :body-style="{ padding: '0px' }" @click="$router.push('/student/booking')">
          <div class="room-cover">
            <img :src="`http://localhost:8080/api/admin/room/cover/${room.id}`" @error="(e) => e.target.style.display='none'" class="cover-img" />
            <span class="room-status" :class="room.status === 1 ? 'open' : 'closed'">
               {{ room.status === 1 ? '营业中' : '休息中' }}
             </span>
          </div>
          <div class="room-info">
            <h4 class="room-name">{{ room.name }}</h4>
            <p class="room-desc"><el-icon><Location /></el-icon> {{ room.location }}</p>
            <p class="room-desc"><el-icon><Clock /></el-icon> {{ room.openTime }} - {{ room.closeTime }}</p>

            <div class="room-action">
              <el-button type="primary" round class="book-btn">立即预约</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Location, Clock } from '@element-plus/icons-vue'
import axios from 'axios'

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

// 考研倒计时逻辑
const daysLeft = ref(0)
const calculateDaysLeft = () => {
  const today = new Date()
  const targetDate = new Date(today.getFullYear(), 11, 20) // 每年12月20日左右
  if (today > targetDate) {
    targetDate.setFullYear(today.getFullYear() + 1)
  }
  const diffTime = Math.abs(targetDate - today)
  daysLeft.value = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
}

// 推荐门店列表
const roomList = ref([])
const fetchRooms = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/admin/room/list')
    if (res.data.code === 200) {
      roomList.value = res.data.data.slice(0, 3) // 首页最多展示3家
    }
  } catch (error) {
    console.error('获取门店失败', error)
  }
}

// === 真实数据统计逻辑 ===
const loadingStats = ref(false)
const stats = ref({
  totalHours: 0,
  totalCount: 0,
  beatPercentage: 0
})

const fetchStats = async () => {
  if (!userInfo.id) return
  loadingStats.value = true
  try {
    const res = await axios.get(`http://localhost:8080/api/student/reservation/stats?userId=${userInfo.id}`)
    if (res.data.code === 200) {
      stats.value = res.data.data
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  } finally {
    loadingStats.value = false
  }
}

onMounted(() => {
  calculateDaysLeft()
  fetchRooms()
  fetchStats()
})
</script>

<style scoped>
.student-home-container { max-width: 1200px; margin: 0 auto; }

/* 顶部卡片统一样式 */
.top-board { margin-bottom: 30px; }
.countdown-card { height: 180px; background: linear-gradient(135deg, #10b981 0%, #34d399 100%); color: white; text-align: center; border: none; }
.countdown-card .card-header { font-size: 16px; opacity: 0.9; margin-top: 10px; }
.countdown-card .countdown-days { margin: 15px 0; }
.countdown-card .num { font-size: 48px; font-weight: bold; }
.countdown-card .unit { font-size: 16px; }
.countdown-card .card-footer { font-size: 14px; opacity: 0.8; }

.stat-card { height: 180px; border: none; }
.stat-title { font-size: 18px; font-weight: bold; color: #333; margin-bottom: 30px; }
.stat-content { display: flex; justify-content: space-around; text-align: center; }
.stat-label { font-size: 14px; color: #909399; margin-bottom: 10px; }
.stat-value { font-size: 28px; font-weight: bold; }
.stat-value .unit { font-size: 14px; font-weight: normal; }

/* 推荐门店部分 */
.section-title { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.section-title h3 { margin: 0; color: #333; font-size: 20px; }

.room-card { border-radius: 12px; overflow: hidden; transition: all 0.3s; cursor: pointer; }
.room-card:hover { transform: translateY(-5px); box-shadow: 0 10px 20px rgba(0,0,0,0.1); }

/* 门店封面样式 */
.room-cover { height: 150px; background: linear-gradient(120deg, #a1c4fd 0%, #c2e9fb 100%); position: relative; }
.cover-img { width: 100%; height: 100%; object-fit: cover; position: absolute; top: 0; left: 0; z-index: 0; }
.room-status { position: absolute; top: 10px; right: 10px; padding: 4px 10px; border-radius: 20px; font-size: 12px; color: white; z-index: 1; }
.room-status.open { background-color: rgba(16, 185, 129, 0.8); }
.room-status.closed { background-color: rgba(245, 108, 108, 0.8); }

.room-info { padding: 20px; }
.room-name { margin: 0 0 10px 0; font-size: 18px; color: #333; }
.room-desc { margin: 5px 0; font-size: 14px; color: #666; display: flex; align-items: center; gap: 5px; }
.room-action { margin-top: 20px; text-align: right; }
.book-btn { width: 100%; background-color: #10b981; border-color: #10b981; }
.book-btn:hover { background-color: #059669; border-color: #059669; }
</style>
