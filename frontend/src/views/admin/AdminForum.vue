<template>
  <div class="admin-forum-container">
    <div class="header-action">
      <h2 style="margin: 0; color: #333;"><el-icon><ChatDotSquare /></el-icon> 交流板内容审核</h2>
      <el-button type="primary" icon="Refresh" @click="fetchPosts">刷新数据</el-button>
    </div>

    <el-table :data="postList" border stripe style="width: 100%; margin-top: 20px;" v-loading="loading">
      <el-table-column prop="id" label="帖子ID" width="80" align="center"></el-table-column>
      <el-table-column prop="nickname" label="发布人" width="150" align="center"></el-table-column>

      <el-table-column prop="content" label="帖子内容" show-overflow-tooltip>
        <template #default="scope">
          <span style="font-weight: bold; color: #303133;">{{ scope.row.content }}</span>
        </template>
      </el-table-column>

      <el-table-column prop="createTime" label="发布时间" width="180" align="center">
        <template #default="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>

      <el-table-column label="包含评论" width="120" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.comments && scope.row.comments.length > 0 ? 'success' : 'info'">
            {{ scope.row.comments ? scope.row.comments.length : 0 }} 条评论
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" plain @click="handleViewDetails(scope.row)">查看详情</el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(scope.row.id)">违规删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog title="帖子与评论详情" v-model="detailsVisible" width="600px">
      <div v-if="currentPost" class="post-details">
        <div class="detail-item">
          <span class="label">发布人：</span> {{ currentPost.nickname }}
        </div>
        <div class="detail-item">
          <span class="label">发布时间：</span> {{ formatTime(currentPost.createTime) }}
        </div>
        <div class="detail-item">
          <span class="label">帖子正文：</span>
          <div class="post-text">{{ currentPost.content }}</div>
        </div>

        <el-divider>评论列表</el-divider>

        <div v-if="currentPost.comments && currentPost.comments.length > 0" class="comment-list">
          <div v-for="comment in currentPost.comments" :key="comment.id" class="comment-box">
            <span class="comment-user">{{ comment.nickname }}:</span>
            <span class="comment-content">{{ comment.content }}</span>
          </div>
        </div>
        <el-empty v-else description="该帖子暂无评论" :image-size="60"></el-empty>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ChatDotSquare, Refresh } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

const loading = ref(false)
const postList = ref([])

// 详情弹窗状态
const detailsVisible = ref(false)
const currentPost = ref(null)

onMounted(() => {
  fetchPosts()
})

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.replace('T', ' ').substring(0, 16)
}

const fetchPosts = async () => {
  loading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/forum/list')
    if (res.data.code === 200) {
      postList.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('获取帖子列表失败！')
  } finally {
    loading.value = false
  }
}

// 查看详情
const handleViewDetails = (row) => {
  currentPost.value = row
  detailsVisible.value = true
}

// 删除违规帖子
const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除这条帖子吗？删除后，该帖子下的所有评论也会被一并清除！', '高危操作警告', {
    confirmButtonText: '强制删除',
    cancelButtonText: '取消',
    type: 'error'
  }).then(async () => {
    try {
      const res = await axios.delete(`http://localhost:8080/api/forum/post/delete/${id}`)
      if (res.data.code === 200) {
        ElMessage.success('违规内容清理成功！')
        fetchPosts() // 刷新列表
      } else {
        ElMessage.error(res.data.msg)
      }
    } catch (error) {
      ElMessage.error('删除失败，请检查网络！')
    }
  }).catch(() => {})
}
</script>

<style scoped>
.admin-forum-container {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
.header-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.post-details {
  font-size: 14px;
}
.detail-item {
  margin-bottom: 15px;
}
.label {
  font-weight: bold;
  color: #606266;
}
.post-text {
  margin-top: 8px;
  padding: 12px;
  background-color: #f4f4f5;
  border-radius: 4px;
  color: #303133;
  line-height: 1.5;
  white-space: pre-wrap;
}
.comment-box {
  padding: 8px 0;
  border-bottom: 1px dashed #ebeef5;
}
.comment-box:last-child {
  border-bottom: none;
}
.comment-user {
  font-weight: bold;
  color: #409eff;
  margin-right: 8px;
}
</style>
