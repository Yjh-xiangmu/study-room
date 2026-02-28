<template>
  <div class="forum-container">
    <div class="forum-header">
      <h2 style="margin: 0; color: #333;"><el-icon><ChatLineRound /></el-icon> 研友交流板</h2>
      <el-button type="primary" @click="dialogVisible = true" size="large" round icon="Edit">发布动态</el-button>
    </div>

    <div class="post-list" v-loading="loading">
      <el-empty v-if="postList.length === 0" description="暂无动态，快来发第一帖吧！"></el-empty>

      <el-card v-for="post in postList" :key="post.id" class="post-card" shadow="hover">
        <div class="post-header">
          <el-avatar style="background-color: #10b981;">{{ post.nickname.charAt(0) }}</el-avatar>
          <div class="post-user-info">
            <span class="nickname">{{ post.nickname }}</span>
            <span class="time">{{ formatTime(post.createTime) }}</span>
          </div>
        </div>

        <div class="post-content">
          {{ post.content }}
        </div>

        <div class="comments-section" v-if="post.comments && post.comments.length > 0">
          <div v-for="(comment, index) in post.comments" :key="index" class="comment-item">
            <span class="comment-nickname">{{ comment.nickname }}: </span>
            <span class="comment-text">{{ comment.content }}</span>
          </div>
        </div>

        <div class="comment-input-area">
          <el-input
            v-model="commentInputs[post.id]"
            placeholder="说点什么吧..."
            size="small"
            @keyup.enter="submitComment(post.id)"
          >
            <template #append>
              <el-button @click="submitComment(post.id)">发送</el-button>
            </template>
          </el-input>
        </div>
      </el-card>
    </div>

    <el-dialog title="分享备考日常" v-model="dialogVisible" width="500px">
      <el-input
        v-model="newPostContent"
        type="textarea"
        :rows="5"
        placeholder="今天复习得怎么样了？或者遇到了什么难题？和大家分享一下吧..."
        maxlength="500"
        show-word-limit
      />
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPost">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ChatLineRound, Edit } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const loading = ref(false)
const postList = ref([])
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

// 发帖状态
const dialogVisible = ref(false)
const newPostContent = ref('')

// 评论输入框状态 (用对象的结构存不同帖子的输入框内容)
const commentInputs = reactive({})

onMounted(() => {
  fetchPosts()
})

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16)
}

// 获取帖子列表
const fetchPosts = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/forum/list')
    if (res.data.code === 200) {
      postList.value = res.data.data
      // 初始化每个帖子的评论输入框为空
      postList.value.forEach(p => {
        if (commentInputs[p.id] === undefined) {
          commentInputs[p.id] = ''
        }
      })
    }
  } catch (error) {
    ElMessage.error('获取列表失败！')
  } finally {
    loading.value = false
  }
}

// 发布帖子
const submitPost = async () => {
  if (!newPostContent.value.trim()) return ElMessage.warning('内容不能为空哦！')
  try {
    const res = await axios.post('http://localhost:8080/api/forum/post/add', {
      userId: userInfo.id,
      content: newPostContent.value
    })
    if (res.data.code === 200) {
      ElMessage.success('发布成功！')
      dialogVisible.value = false
      newPostContent.value = ''
      fetchPosts() // 刷新列表
    }
  } catch (error) {
    ElMessage.error('发布失败！')
  }
}

// 发表评论
const submitComment = async (postId) => {
  const content = commentInputs[postId]
  if (!content || !content.trim()) return ElMessage.warning('评论内容不能为空！')

  try {
    const res = await axios.post('http://localhost:8080/api/forum/comment/add', {
      postId: postId,
      userId: userInfo.id,
      content: content.trim()
    })
    if (res.data.code === 200) {
      ElMessage.success('评论成功！')
      commentInputs[postId] = '' // 清空该帖子的输入框
      fetchPosts() // 刷新以展示新评论
    }
  } catch (error) {
    ElMessage.error('评论失败！')
  }
}
</script>

<style scoped>
.forum-container {
  max-width: 800px; /* 居中显示，类似朋友圈宽度 */
  margin: 0 auto;
  padding: 20px 0;
}
.forum-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
.post-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.post-card {
  border-radius: 8px;
  border: none;
}
.post-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}
.post-user-info {
  display: flex;
  flex-direction: column;
}
.nickname {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}
.time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.post-content {
  font-size: 15px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 15px;
  white-space: pre-wrap; /* 保持发帖时的换行 */
}
.comments-section {
  background-color: #f8f9fa;
  padding: 10px 15px;
  border-radius: 4px;
  margin-bottom: 15px;
}
.comment-item {
  font-size: 14px;
  margin-bottom: 5px;
}
.comment-item:last-child {
  margin-bottom: 0;
}
.comment-nickname {
  font-weight: bold;
  color: #409EFF;
}
.comment-text {
  color: #606266;
}
.comment-input-area {
  margin-top: 10px;
}
</style>
