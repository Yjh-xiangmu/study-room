<template>
  <div class="ai-container">
    <el-card shadow="hover" class="chat-card">
      <div class="chat-header">
        <div class="ai-info">
          <el-avatar :size="45" src="https://api.dicebear.com/7.x/bottts/svg?seed=deepseek" style="background-color: #f0f2f5;" />
          <div class="ai-title">
            <h3 style="margin: 0; color: #333;">研途 AI 伴学助理</h3>
            <span style="font-size: 12px; color: #10b981;">● DeepSeek 模型持续赋能中</span>
          </div>
        </div>
        <el-button type="warning" plain icon="DataLine" @click="generateReport" :loading="isGenerating">
          分析我的本周学情
        </el-button>
      </div>

      <div class="chat-window" ref="chatWindow">
        <div v-for="(msg, index) in messageList" :key="index" :class="['msg-row', msg.role === 'user' ? 'msg-right' : 'msg-left']">

          <el-avatar v-if="msg.role === 'ai'" :size="36" src="https://api.dicebear.com/7.x/bottts/svg?seed=deepseek" class="msg-avatar" style="background-color: #f0f2f5;" />

          <div :class="['msg-bubble', msg.role === 'user' ? 'bubble-user' : 'bubble-ai']">

            <div v-if="msg.isLoading" style="color: #909399; font-weight: bold; font-size: 14px; display:flex; align-items:center;">
              <el-icon class="is-loading" style="margin-right: 5px;"><Loading /></el-icon>
              DeepSeek 思考中，请稍等...
            </div>

            <div v-else-if="msg.role === 'ai'" class="markdown-body" v-html="renderMarkdown(msg.content)"></div>
            <span v-else style="white-space: pre-wrap; line-height: 1.6;">{{ msg.content }}</span>

          </div>

          <el-avatar v-if="msg.role === 'user'" :size="36" :src="avatarUrl" class="msg-avatar" @error="()=>true">
            <img src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png"/>
          </el-avatar>
        </div>
      </div>

      <div class="chat-input-area">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="3"
          placeholder="遇到不会的高数题？或者学得太累想找人聊聊？发给 AI 助手吧..."
          resize="none"
          @keyup.enter.exact.prevent="sendMessage"
        ></el-input>
        <div class="action-bar">
          <span class="tip">Enter 发送，Shift + Enter 换行</span>
          <el-button type="primary" icon="Position" @click="sendMessage" :disabled="!inputText.trim() || isSending">发送</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { DataLine, Position, Loading } from '@element-plus/icons-vue'
import axios from 'axios'
// 引入刚刚安装的 marked
import { marked } from 'marked'

// 配置 marked，允许回车换行
marked.setOptions({
  breaks: true,
  gfm: true
})

const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const avatarUrl = ref(`http://localhost:8080/api/student/profile/avatar/${userInfo.id}?t=${new Date().getTime()}`)

const inputText = ref('')
const isSending = ref(false)
const isGenerating = ref(false)
const chatWindow = ref(null)

const messageList = ref([
  { role: 'ai', content: `你好，${userInfo.nickname || '同学'}！我是由 DeepSeek 强力驱动的专属伴学助理。无论是学术答疑还是心理疏导，我都在这里陪你一战成硕！有什么我可以帮你的吗？`, isLoading: false }
])

const scrollToBottom = async () => {
  await nextTick()
  if (chatWindow.value) {
    chatWindow.value.scrollTop = chatWindow.value.scrollHeight
  }
}

// 封装一个解析 Markdown 的方法
const renderMarkdown = (text) => {
  if (!text) return ''
  return marked(text)
}

const sendMessage = async () => {
  if (!inputText.value.trim() || isSending.value) return

  const userMsg = inputText.value.trim()
  messageList.value.push({ role: 'user', content: userMsg, isLoading: false })
  inputText.value = ''
  isSending.value = true
  scrollToBottom()

  messageList.value.push({ role: 'ai', content: '', isLoading: true })
  scrollToBottom()

  try {
    const res = await axios.post('http://localhost:8080/api/ai/chat', { message: userMsg }, { timeout: 65000 })
    messageList.value.pop()

    let aiContent = res.data.data || res.data.msg || res.data.message;
    if (!aiContent) {
      aiContent = '【前端防空保护】后端未返回有效文字内容，原始报文：\n' + JSON.stringify(res.data);
    } else if (typeof aiContent !== 'string') {
      aiContent = JSON.stringify(aiContent);
    }

    messageList.value.push({ role: 'ai', content: aiContent, isLoading: false })
  } catch (error) {
    messageList.value.pop()
    messageList.value.push({ role: 'ai', content: '⚠️ 网络异常或请求超时，请重试。', isLoading: false })
  } finally {
    isSending.value = false
    scrollToBottom()
  }
}

const generateReport = async () => {
  isGenerating.value = true

  messageList.value.push({ role: 'user', content: '请帮我分析一下我最近在自习室的学习情况。', isLoading: false })
  scrollToBottom()

  messageList.value.push({ role: 'ai', content: '', isLoading: true })
  scrollToBottom()

  try {
    const res = await axios.get(`http://localhost:8080/api/ai/report?userId=${userInfo.id}`, { timeout: 65000 })
    messageList.value.pop()

    let aiContent = res.data.data || res.data.msg || res.data.message;
    if (!aiContent) {
      aiContent = '【前端防空保护】后端未返回有效文字内容，原始报文：\n' + JSON.stringify(res.data);
    } else if (typeof aiContent !== 'string') {
      aiContent = JSON.stringify(aiContent);
    }

    messageList.value.push({ role: 'ai', content: aiContent, isLoading: false })
  } catch (error) {
    messageList.value.pop()
    messageList.value.push({ role: 'ai', content: '⚠️ 网络异常，生成报告失败。', isLoading: false })
  } finally {
    isGenerating.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.ai-container { max-width: 900px; margin: 0 auto; height: 85vh; display: flex; flex-direction: column; }
.chat-card { flex: 1; display: flex; flex-direction: column; border-radius: 12px; }
:deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; padding: 0; overflow: hidden; }

.chat-header { display: flex; justify-content: space-between; align-items: center; padding: 15px 25px; border-bottom: 1px solid #ebeef5; background-color: #fafafa; }
.ai-info { display: flex; align-items: center; gap: 15px; }

.chat-window { flex: 1; padding: 25px; overflow-y: auto; background-color: #f4f7f6; display: flex; flex-direction: column; gap: 20px; }

.msg-row { display: flex; align-items: flex-start; gap: 12px; }
.msg-left { flex-direction: row; }
.msg-right { flex-direction: row-reverse; }

.msg-bubble { max-width: 75%; padding: 12px 16px; border-radius: 8px; font-size: 15px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); word-wrap: break-word; }
.bubble-ai { background-color: #fff; color: #333; border-top-left-radius: 2px; }
.bubble-user { background-color: #10b981; color: #fff; border-top-right-radius: 2px; }

.chat-input-area { padding: 15px 25px; border-top: 1px solid #ebeef5; background-color: #fff; }
.chat-input-area :deep(.el-textarea__inner) { border: none; box-shadow: none; padding: 0; font-size: 15px; }
.chat-input-area :deep(.el-textarea__inner:focus) { box-shadow: none; }
.action-bar { display: flex; justify-content: space-between; align-items: center; margin-top: 10px; }
.tip { font-size: 12px; color: #999; }

/* 专门美化 Markdown 富文本渲染后的样式 */
:deep(.markdown-body) {
  line-height: 1.6;
}
:deep(.markdown-body p) {
  margin: 0 0 10px 0;
}
:deep(.markdown-body p:last-child) {
  margin-bottom: 0;
}
:deep(.markdown-body strong) {
  font-weight: 600;
  color: #111;
}
:deep(.markdown-body ul), :deep(.markdown-body ol) {
  margin-top: 8px;
  margin-bottom: 8px;
  padding-left: 20px;
}
</style>
