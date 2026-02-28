<template>
  <div class="booking-container">
    <div v-if="!currentRoom" class="room-list-view">
      <h2 class="page-title">📍 请选择要预约的门店</h2>
      <el-row :gutter="20">
        <el-col :span="8" v-for="room in roomList" :key="room.id" style="margin-bottom: 20px;">
          <el-card shadow="hover" class="room-card" @click="enterRoom(room)">
            <div class="room-cover">
              <img :src="`http://localhost:8080/api/admin/room/cover/${room.id}`" @error="(e) => e.target.style.display='none'" class="cover-img" />
              <span class="room-status" :class="room.status === 1 ? 'open' : 'closed'">
                {{ room.status === 1 ? '营业中' : '休息中' }}
              </span>
            </div>
            <div class="room-info">
              <h3>{{ room.name }}</h3>
              <p><el-icon><Location /></el-icon> {{ room.location }}</p>
              <p><el-icon><Clock /></el-icon> {{ room.openTime }} - {{ room.closeTime }}</p>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div v-else class="seat-map-view">
      <div class="map-header">
        <el-button icon="ArrowLeft" @click="currentRoom = null">返回门店列表</el-button>
        <h2 style="margin: 0; color: #333;">{{ currentRoom.name }}</h2>
      </div>

      <el-tabs v-model="activeMainTab" class="main-tabs" @tab-click="handleMainTabChange">

        <el-tab-pane label="座位预约" name="booking">
          <el-tabs v-model="activeZoneId" class="zone-tabs" @tab-click="handleZoneChange" type="border-card">
            <el-tab-pane v-for="zone in zoneList" :key="zone.id" :label="zone.name" :name="zone.id">

              <div class="zone-info-bar">
                <span><strong>配套设施：</strong>{{ zone.facilities || '无特殊设施' }}</span>
                <span style="color: #f56c6c; font-weight: bold;">收费标准：￥{{ zone.hourlyPrice }} / 小时</span>
              </div>

              <div class="zone-image-wrapper">
                <img :src="`http://localhost:8080/api/admin/zone/image/${zone.id}`" @error="(e) => e.target.style.display='none'" class="zone-img" />
              </div>

              <div class="seat-legend">
                <div class="legend-item"><div class="seat-box free"></div> 可选</div>
                <div class="legend-item"><div class="seat-box booked"></div> 已占</div>
                <div class="legend-item"><div class="seat-box broken"></div> 维修</div>
              </div>

              <div class="seat-grid" v-loading="loadingSeats">
                <div
                  v-for="seat in currentZoneSeats" :key="seat.id"
                  class="seat-box"
                  :class="{'free': seat.status === 0, 'booked': seat.status === 1, 'broken': seat.status === 2}"
                  @click="handleSelectSeat(seat, zone)"
                >
                  {{ seat.seatNo }}
                </div>
                <div v-if="currentZoneSeats.length === 0" class="empty-tip">该分区暂未配置座位</div>
              </div>

            </el-tab-pane>
          </el-tabs>
        </el-tab-pane>

        <el-tab-pane label="评价中心" name="reviews">
          <div class="review-header">
            <span style="color: #666; font-size: 14px;">仅限真实消费学习过的用户评价，保证绝对真实。</span>
            <el-button type="primary" icon="Edit" @click="handleOpenReview">写评价</el-button>
          </div>

          <div class="review-list" v-loading="loadingReviews">
            <el-empty v-if="reviewList.length === 0" description="该门店暂无评价，快来抢沙发！"></el-empty>

            <el-card v-for="review in reviewList" :key="review.id" class="review-card" shadow="hover">
              <div class="review-user-info">
                <el-avatar :size="40" :src="`http://localhost:8080/api/student/profile/avatar/${review.userId}`" @error="()=>true">
                  <img src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png"/>
                </el-avatar>
                <div class="review-meta">
                  <span class="nickname">{{ review.nickname }}</span>
                  <span class="time">{{ review.createTime.replace('T', ' ').substring(0, 16) }}</span>
                </div>
                <el-rate v-model="review.rating" disabled show-score text-color="#ff9900" style="margin-left: auto;"></el-rate>
              </div>
              <div class="review-content">
                {{ review.content }}
              </div>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-dialog title="确认预约信息" v-model="paymentDialogVisible" width="400px">
      <div class="payment-details">
        <p><strong>预约门店：</strong>{{ currentRoom?.name }}</p>
        <p><strong>所选座位：</strong>{{ selectedZone?.name }} - {{ selectedSeat?.seatNo }} 号</p>
        <div style="margin: 20px 0; display: flex; align-items: center;">
          <strong style="margin-right: 15px;">学习时长：</strong>
          <el-input-number v-model="duration" :min="0.5" :max="12" :step="0.5" @change="calculateTotal"></el-input-number>
          <span style="margin-left: 10px;">小时</span>
        </div>
        <el-divider></el-divider>
        <div class="total-price-box">
          <span>需支付金额：</span>
          <span class="price-num">￥{{ totalAmount.toFixed(2) }}</span>
        </div>
        <div class="balance-tip">
          您的钱包当前余额：￥{{ userInfo.balance?.toFixed(2) || '0.00' }}
        </div>
      </div>
      <template #footer>
        <el-button @click="paymentDialogVisible = false">取消</el-button>
        <el-button type="success" @click="submitPayment" :loading="paying">一键支付并预约</el-button>
      </template>
    </el-dialog>

    <el-dialog title="发布门店真实评价" v-model="reviewDialogVisible" width="450px">
      <div style="text-align: center; margin-bottom: 20px;">
        <span style="margin-right: 15px;">综合评分：</span>
        <el-rate v-model="reviewForm.rating" :colors="['#99A9BF', '#F7BA2A', '#FF9900']"></el-rate>
      </div>
      <el-input
        v-model="reviewForm.content"
        type="textarea"
        :rows="4"
        placeholder="环境怎么样？安静吗？给大家个参考吧！(最多200字)"
        maxlength="200"
        show-word-limit
      ></el-input>
      <template #footer>
        <el-button @click="reviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview" :loading="submittingReview">发布</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Location, Clock, ArrowLeft, Edit } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const roomList = ref([])
const currentRoom = ref(null)

// Tab 状态
const activeMainTab = ref('booking')

// 选座相关状态
const zoneList = ref([])
const activeZoneId = ref(null)
const currentZoneSeats = ref([])
const loadingSeats = ref(false)

// 评价相关状态
const reviewList = ref([])
const loadingReviews = ref(false)
const reviewDialogVisible = ref(false)
const submittingReview = ref(false)
const reviewForm = reactive({ rating: 5, content: '' })

// 支付弹窗状态
const paymentDialogVisible = ref(false)
const selectedSeat = ref(null)
const selectedZone = ref(null)
const duration = ref(2)
const totalAmount = ref(0)
const paying = ref(false)

// 本地用户信息
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

onMounted(() => {
  fetchRooms()
  refreshUserInfo()
})

const refreshUserInfo = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/admin/user/list', { params: { keyword: userInfo.value.username } })
    if (res.data.code === 200 && res.data.data.length > 0) {
      userInfo.value = res.data.data[0]
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }
  } catch (e) {}
}

const fetchRooms = async () => {
  const res = await axios.get('http://localhost:8080/api/admin/room/list')
  if (res.data.code === 200) roomList.value = res.data.data
}

// 进入门店
const enterRoom = async (room) => {
  if (room.status !== 1) return ElMessage.warning('该门店目前休息中，无法预约哦！')
  currentRoom.value = room
  activeMainTab.value = 'booking' // 默认进选座

  const res = await axios.get('http://localhost:8080/api/admin/zone/list', { params: { roomId: room.id } })
  if (res.data.code === 200 && res.data.data.length > 0) {
    zoneList.value = res.data.data
    activeZoneId.value = zoneList.value[0].id
    fetchSeatsForZone(activeZoneId.value)
  } else {
    ElMessage.info('该门店尚未配置分区')
  }
}

// 大 Tab 切换 (座位 / 评价)
const handleMainTabChange = (tab) => {
  if (tab.paneName === 'reviews') {
    fetchReviews()
  }
}

const handleZoneChange = (tab) => {
  fetchSeatsForZone(tab.paneName)
}

const fetchSeatsForZone = async (zoneId) => {
  loadingSeats.value = true
  const res = await axios.get('http://localhost:8080/api/admin/seat/list', { params: { roomId: currentRoom.value.id } })
  if (res.data.code === 200) {
    currentZoneSeats.value = res.data.data.filter(s => s.zoneId === zoneId)
  }
  loadingSeats.value = false
}

// === 评价逻辑 ===
const fetchReviews = async () => {
  loadingReviews.value = true
  try {
    const res = await axios.get(`http://localhost:8080/api/student/review/list?roomId=${currentRoom.value.id}`)
    if (res.data.code === 200) {
      reviewList.value = res.data.data
    }
  } catch (e) {
    ElMessage.error('获取评价失败！')
  } finally {
    loadingReviews.value = false
  }
}

const handleOpenReview = async () => {
  // 先去后端校验用户有没有资格评价
  try {
    const res = await axios.get(`http://localhost:8080/api/student/review/check?userId=${userInfo.value.id}&roomId=${currentRoom.value.id}`)
    if (res.data.code === 200 && res.data.data === true) {
      reviewForm.rating = 5
      reviewForm.content = ''
      reviewDialogVisible.value = true
    } else {
      ElMessage.warning('您尚未在该门店完成过学习，无法发表评价哦！')
    }
  } catch (e) {
    ElMessage.error('校验资格失败！')
  }
}

const submitReview = async () => {
  if (!reviewForm.content.trim()) return ElMessage.warning('评价内容不能为空！')
  submittingReview.value = true
  try {
    const res = await axios.post('http://localhost:8080/api/student/review/add', {
      roomId: currentRoom.value.id,
      userId: userInfo.value.id,
      rating: reviewForm.rating,
      content: reviewForm.content
    })
    if (res.data.code === 200) {
      ElMessage.success('评价发布成功！')
      reviewDialogVisible.value = false
      fetchReviews() // 刷新评价列表
    }
  } catch (e) {
    ElMessage.error('发布失败！')
  } finally {
    submittingReview.value = false
  }
}

// === 预约逻辑 ===
const handleSelectSeat = (seat, zone) => {
  if (seat.status === 1) return ElMessage.warning('该座位已被预约，选个其他的吧！')
  if (seat.status === 2) return ElMessage.error('该座位设备维修中！')

  selectedSeat.value = seat
  selectedZone.value = zoneList.value.find(z => z.id === seat.zoneId)
  duration.value = 2
  calculateTotal()
  paymentDialogVisible.value = true
}

const calculateTotal = () => {
  totalAmount.value = selectedZone.value.hourlyPrice * duration.value
}

const submitPayment = async () => {
  if (userInfo.value.balance < totalAmount.value) {
    return ElMessage.error('余额不足，请联系管理员充值后再来预约！')
  }

  paying.value = true
  try {
    const payload = {
      userId: userInfo.value.id,
      roomId: currentRoom.value.id,
      seatId: selectedSeat.value.id,
      duration: duration.value
    }
    const res = await axios.post('http://localhost:8080/api/student/reservation/book', payload)

    if (res.data.code === 200) {
      ElMessage.success(res.data.msg)
      paymentDialogVisible.value = false
      await refreshUserInfo()
      fetchSeatsForZone(activeZoneId.value)
    } else {
      ElMessage.error(res.data.msg)
    }
  } catch (error) {
    ElMessage.error('支付请求失败！')
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.booking-container { background-color: #fff; padding: 30px; border-radius: 8px; min-height: 80vh; }
.page-title { margin-bottom: 30px; color: #333; }

/* 门店卡片样式与图片 */
.room-card { cursor: pointer; transition: 0.3s; border-radius: 10px; }
.room-card:hover { transform: translateY(-5px); box-shadow: 0 8px 20px rgba(0,0,0,0.1); }
.room-cover { height: 120px; background: linear-gradient(135deg, #10b981 0%, #34d399 100%); position: relative; border-radius: 10px 10px 0 0;}
.cover-img { width: 100%; height: 100%; object-fit: cover; position: absolute; top: 0; left: 0; z-index: 0; border-radius: 10px 10px 0 0; }
.room-status { position: absolute; top: 10px; right: 10px; padding: 4px 10px; border-radius: 20px; font-size: 12px; color: white; z-index: 1; }
.room-status.open { background-color: rgba(0,0,0,0.4); }
.room-status.closed { background-color: #f56c6c; }

.room-info { padding: 15px; }
.room-info h3 { margin: 0 0 10px 0; font-size: 18px; }
.room-info p { margin: 5px 0; font-size: 13px; color: #666; display: flex; align-items: center; gap: 5px;}

/* 选座地图视图 */
.map-header { display: flex; align-items: center; gap: 20px; margin-bottom: 20px; padding-bottom: 20px; border-bottom: 1px solid #eee;}
.main-tabs { margin-top: 10px; }
.zone-info-bar { background-color: #f8f9fa; padding: 15px; border-radius: 8px; display: flex; justify-content: space-between; margin-bottom: 15px;}

/* 分区实景照片样式 */
.zone-image-wrapper { margin-bottom: 20px; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
.zone-img { width: 100%; max-height: 300px; object-fit: cover; display: block; }

/* 座位图例与矩阵 */
.seat-legend { display: flex; gap: 20px; margin-bottom: 20px; justify-content: flex-end;}
.legend-item { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #666;}
.seat-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(80px, 1fr)); gap: 15px;
  padding: 20px; background-color: #fafafa; border-radius: 8px; border: 1px solid #ebeef5;
}
.seat-box {
  height: 80px; border-radius: 8px; display: flex; justify-content: center; align-items: center;
  font-weight: bold; font-size: 16px; cursor: pointer; transition: 0.2s; color: white; box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}
.seat-box:hover { opacity: 0.8; transform: scale(1.05); }
.seat-box.free { background-color: #10b981; }
.seat-box.booked { background-color: #E6A23C; cursor: not-allowed; }
.seat-box.broken { background-color: #F56C6C; cursor: not-allowed; }

.empty-tip { grid-column: 1 / -1; text-align: center; color: #999; padding: 40px; }

/* 评价中心样式 */
.review-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; padding: 10px 0; border-bottom: 1px dashed #ebeef5; }
.review-list { display: flex; flex-direction: column; gap: 15px; }
.review-card { border-radius: 8px; border: none; }
.review-user-info { display: flex; align-items: center; gap: 15px; margin-bottom: 10px; }
.review-meta { display: flex; flex-direction: column; }
.review-meta .nickname { font-size: 15px; font-weight: bold; color: #333; }
.review-meta .time { font-size: 12px; color: #999; margin-top: 4px; }
.review-content { font-size: 14px; color: #606266; line-height: 1.6; padding-left: 55px; }

/* 支付弹窗 */
.payment-details p { margin: 10px 0; font-size: 15px; }
.total-price-box { display: flex; justify-content: space-between; align-items: center; font-size: 16px; margin-top: 20px; }
.price-num { font-size: 24px; font-weight: bold; color: #f56c6c; }
.balance-tip { margin-top: 15px; text-align: right; font-size: 13px; color: #909399; }
</style>
