<template>
  <div class="profile-container">
    <el-row :gutter="20">

      <el-col :span="8">
        <el-card shadow="hover" class="user-card">
          <div class="avatar-wrapper">
            <el-avatar :size="100" :src="avatarUrl" @error="() => true">
              <img src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png"/>
            </el-avatar>

            <el-upload
              class="upload-btn"
              action="http://localhost:8080/api/student/profile/avatar/upload"
              :data="{ userId: userInfo.id }"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              name="file"
            >
              <el-button size="small" round>更换头像</el-button>
            </el-upload>
          </div>

          <div class="user-info-text">
            <h3>{{ userInfo.nickname }}</h3>
            <p>登录账号：{{ userInfo.username }}</p>
            <p>当前信用分：<strong style="color: #67c23a; font-size: 18px;">{{ userInfo.creditScore }}</strong></p>
          </div>
        </el-card>

        <el-card shadow="hover" class="balance-card" style="margin-top: 20px;">
          <div class="balance-title">钱包余额</div>
          <div class="balance-num">￥{{ userInfo.balance?.toFixed(2) }}</div>
          <p style="font-size: 12px; color: #999;">(若需充值，请联系前台店长)</p>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: bold;">基本资料修改</span>
          </template>
          <el-form :model="editForm" label-width="100px" style="max-width: 400px; padding: 20px 0;">
            <el-form-item label="用户昵称">
              <el-input v-model="editForm.nickname"></el-input>
            </el-form-item>
            <el-form-item label="修改密码">
              <el-input v-model="editForm.password" show-password placeholder="不修改请留空"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="updateProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="hover" style="margin-top: 20px;">
          <template #header>
            <span style="font-weight: bold;">附加服务</span>
          </template>
          <div class="service-btns" style="padding: 10px 0;">
            <el-button type="success" size="large" plain @click="showMyPosts" icon="Document">管理我的发帖</el-button>
            <el-button type="warning" size="large" plain @click="feedbackVisible = true" icon="Service">提交意见反馈</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog title="我的发帖记录" v-model="postsVisible" width="600px">
      <el-table :data="myPosts" border stripe v-loading="loadingPosts">
        <el-table-column prop="content" label="帖子内容" show-overflow-tooltip></el-table-column>
        <el-table-column label="发布时间" width="160">
          <template #default="scope">{{ scope.row.createTime.replace('T', ' ').substring(0, 16) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="scope">
            <el-button size="small" type="danger" text @click="deleteMyPost(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-dialog title="意见反馈" v-model="feedbackVisible" width="500px">
      <el-input
        v-model="feedbackContent"
        type="textarea"
        :rows="5"
        placeholder="请留下您的宝贵意见或遇到的Bug，系统管理员会及时查看处理..."
      />
      <template #footer>
        <el-button @click="feedbackVisible = false">取消</el-button>
        <el-button type="primary" @click="submitFeedback">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

// 头像 URL
const avatarUrl = ref(`http://localhost:8080/api/student/profile/avatar/${userInfo.value.id}?t=${new Date().getTime()}`)

const editForm = reactive({
  id: userInfo.value.id,
  nickname: userInfo.value.nickname,
  password: ''
})

// 【核心逻辑】每次进入个人中心页面，自动向后端拉取最新的账户余额和数据
onMounted(async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/admin/user/list', { params: { keyword: userInfo.value.username } })
    if (res.data.code === 200 && res.data.data.length > 0) {
      userInfo.value = res.data.data[0] // 获取最新余额
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value)) // 更新本地缓存
    }
  } catch (e) {
    console.error('获取最新用户信息失败', e)
  }
})

// === 头像上传逻辑 ===
const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) ElMessage.error('头像只能是图片格式!')
  if (!isLt2M) ElMessage.error('头像文件大小不能超过 2MB!')
  return isImage && isLt2M
}

const handleAvatarSuccess = (res) => {
  if (res.code === 200) {
    ElMessage.success('头像更换成功！')
    avatarUrl.value = `http://localhost:8080/api/student/profile/avatar/${userInfo.value.id}?t=${new Date().getTime()}`
  } else {
    ElMessage.error(res.msg || '上传失败')
  }
}

// === 修改资料逻辑 ===
const updateProfile = async () => {
  try {
    const res = await axios.put('http://localhost:8080/api/student/profile/update', editForm)
    if (res.data.code === 200) {
      ElMessage.success('个人资料修改成功！')
      userInfo.value.nickname = res.data.data.nickname
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
      editForm.password = ''
    }
  } catch (error) {
    ElMessage.error('修改失败！')
  }
}

// === 我的发帖逻辑 ===
const postsVisible = ref(false)
const myPosts = ref([])
const loadingPosts = ref(false)

const showMyPosts = async () => {
  postsVisible.value = true
  loadingPosts.value = true
  try {
    const res = await axios.get(`http://localhost:8080/api/student/profile/myposts/${userInfo.value.id}`)
    if (res.data.code === 200) myPosts.value = res.data.data
  } finally {
    loadingPosts.value = false
  }
}

const deleteMyPost = (id) => {
  ElMessageBox.confirm('确定要删除这条动态吗？', '提示', { type: 'warning' }).then(async () => {
    const res = await axios.delete(`http://localhost:8080/api/forum/post/delete/${id}`)
    if (res.data.code === 200) {
      ElMessage.success('删除成功')
      showMyPosts()
    }
  }).catch(() => {})
}

// === 意见反馈逻辑 ===
const feedbackVisible = ref(false)
const feedbackContent = ref('')

const submitFeedback = async () => {
  if (!feedbackContent.value.trim()) return ElMessage.warning('反馈内容不能为空！')
  try {
    const res = await axios.post('http://localhost:8080/api/student/profile/feedback', {
      userId: userInfo.value.id,
      content: feedbackContent.value
    })
    if (res.data.code === 200) {
      ElMessage.success(res.data.msg)
      feedbackVisible.value = false
      feedbackContent.value = ''
    }
  } catch (e) {
    ElMessage.error('提交反馈失败！')
  }
}
</script>

<style scoped>
.profile-container { max-width: 1000px; margin: 0 auto; }
.user-card { text-align: center; padding: 20px 0; }
.avatar-wrapper { position: relative; display: inline-block; margin-bottom: 15px; }
.upload-btn { margin-top: 15px; }
.user-info-text h3 { margin: 10px 0; font-size: 20px; color: #333; }
.user-info-text p { margin: 5px 0; font-size: 14px; color: #666; }
.balance-card { text-align: center; padding: 10px 0; }
.balance-title { font-size: 16px; color: #333; margin-bottom: 10px; }
.balance-num { font-size: 32px; font-weight: bold; color: #e6a23c; margin-bottom: 10px; }
.service-btns { display: flex; gap: 20px; }
</style>
