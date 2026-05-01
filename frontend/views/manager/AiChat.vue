<template>
  <div class="ai-chat-container">
    <el-card class="chat-card">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <div style="display: flex; align-items: center;">
            <el-icon :size="24" style="margin-right: 10px; color: #409EFF;">
              <ChatDotRound />
            </el-icon>
            <span style="font-weight: bold; font-size: 18px;">AI智能助手</span>
          </div>
          <el-radio-group v-model="data.aiType" size="small">
            <el-radio-button label="qianwen">千问AI</el-radio-button>
            <el-radio-button label="gpt">GPT AI</el-radio-button>
            <el-radio-button label="image">AI绘图</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <!-- 聊天消息区域 -->
      <div class="chat-messages" ref="messagesContainer">
        <div 
          v-for="(msg, index) in data.messages" 
          :key="index" 
          :class="['message-item', msg.type]"
        >
          <div class="message-avatar">
            <el-avatar v-if="msg.type === 'user'" :src="data.user.photos" :size="40">
              {{ data.user.nickname || data.user.uname || '用户' }}
            </el-avatar>
            <el-avatar v-else :size="40" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <el-icon :size="24"><Cpu /></el-icon>
            </el-avatar>
          </div>
          <div class="message-content">
            <div class="message-time">{{ msg.time }}</div>
            <div class="message-text">
              <!-- 文本消息 -->
              <div v-if="!msg.isImage" v-html="formatMessage(msg.content)"></div>
              <!-- 图片消息 -->
              <div v-else class="image-message">
                <el-image 
                  :src="msg.content" 
                  :preview-src-list="[msg.content]"
                  fit="cover"
                  style="max-width: 300px; border-radius: 8px;"
                >
                  <template #error>
                    <div class="image-slot">
                      <el-icon><Picture /></el-icon>
                      <div>加载失败</div>
                    </div>
                  </template>
                </el-image>
              </div>
            </div>
          </div>
        </div>

        <!-- 加载中提示 -->
        <div v-if="data.loading" class="message-item ai">
          <div class="message-avatar">
            <el-avatar :size="40" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
              <el-icon :size="24"><Cpu /></el-icon>
            </el-avatar>
          </div>
          <div class="message-content">
            <div class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="chat-input">
        <el-input
          v-model="data.inputMessage"
          :placeholder="getPlaceholder()"
          @keyup.enter="sendMessage"
          :disabled="data.loading"
        >
          <template #append>
            <el-button 
              type="primary" 
              @click="sendMessage" 
              :loading="data.loading"
              :disabled="!data.inputMessage.trim()"
            >
              <el-icon v-if="!data.loading"><Promotion /></el-icon>
              发送
            </el-button>
          </template>
        </el-input>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, nextTick, onMounted } from 'vue'
import { ChatDotRound, Cpu, Promotion, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request.js'

const messagesContainer = ref(null)

const data = reactive({
  user: JSON.parse(localStorage.getItem('login-user') || '{}'),
  aiType: 'qianwen', // qianwen, gpt, image
  messages: [],
  inputMessage: '',
  loading: false
})

// 获取占位符文本
const getPlaceholder = () => {
  switch (data.aiType) {
    case 'qianwen':
      return '请输入您想问的问题，千问AI为您解答...'
    case 'gpt':
      return '请输入您想问的问题，GPT AI为您解答...'
    case 'image':
      return '请描述您想生成的图片内容...'
    default:
      return '请输入消息...'
  }
}

// 格式化消息（简单的换行处理）
const formatMessage = (content) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br/>')
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 发送消息
const sendMessage = async () => {
  if (!data.inputMessage.trim()) {
    return
  }

  const userMessage = data.inputMessage.trim()
  data.inputMessage = ''

  // 添加用户消息
  data.messages.push({
    type: 'user',
    content: userMessage,
    time: new Date().toLocaleTimeString('zh-CN'),
    isImage: false
  })
  scrollToBottom()

  data.loading = true

  try {
    let response
    
    if (data.aiType === 'qianwen') {
      // 千问AI
      response = await request.get(`/qianwen/${encodeURIComponent(userMessage)}`)
      data.messages.push({
        type: 'ai',
        content: response.data || '抱歉，我暂时无法回答这个问题。',
        time: new Date().toLocaleTimeString('zh-CN'),
        isImage: false
      })
    } else if (data.aiType === 'gpt') {
      // GPT AI
      response = await request.get(`/test1/${encodeURIComponent(userMessage)}`)

      data.messages.push({
        type: 'ai',
        content: response.data || '抱歉，我暂时无法回答这个问题。',
        time: new Date().toLocaleTimeString('zh-CN'),
        isImage: false
      })
    } else if (data.aiType === 'image') {
      // AI绘图
      response = await request.get(`/createimg/${encodeURIComponent(userMessage)}`)
      // 假设返回的是图片URL
      data.messages.push({
        type: 'ai',
        content:  response.data || '',
        time: new Date().toLocaleTimeString('zh-CN'),
        isImage: true
      })
    }
    
    scrollToBottom()
  } catch (error) {
    console.error('AI请求失败', error)
    data.messages.push({
      type: 'ai',
      content: '抱歉，服务暂时不可用，请稍后再试。',
      time: new Date().toLocaleTimeString('zh-CN'),
      isImage: false
    })
    scrollToBottom()
  } finally {
    data.loading = false
  }
}

onMounted(() => {
  // 添加欢迎消息
  data.messages.push({
    type: 'ai',
    content: '您好！我是AI智能助手，很高兴为您服务。您可以选择不同的AI模式与我对话，或者让我为您生成图片。',
    time: new Date().toLocaleTimeString('zh-CN'),
    isImage: false
  })
})
</script>

<style scoped>
.ai-chat-container {
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
}

.chat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: #f5f7fa;
}

.message-item {
  display: flex;
  margin-bottom: 20px;
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 10px;
}

.message-content {
  max-width: 60%;
  display: flex;
  flex-direction: column;
}

.message-item.user .message-content {
  align-items: flex-end;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 5px;
  padding: 0 10px;
}

.message-text {
  background: white;
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  word-wrap: break-word;
  line-height: 1.6;
}

.message-item.user .message-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.image-message {
  padding: 0;
}

.image-slot {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  width: 300px;
  background: #f5f7fa;
  color: #999;
}

.typing-indicator {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #409EFF;
  margin: 0 3px;
  animation: typing 1.4s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

.chat-input {
  padding: 20px;
  background: white;
  border-top: 1px solid #eee;
}

.chat-input :deep(.el-input-group__append) {
  background: transparent;
  border: none;
  padding: 0;
}
</style>
