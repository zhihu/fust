<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- 侧边栏 - 现在是固定定位 -->
    <SessionList
      :current-session="currentSession"
      @session-selected="handleSessionSelected"
      @session-created="handleSessionCreated"
    />
    
    <!-- 主内容区域 - 调整左边距以适配固定侧边栏 -->
    <div class="ml-80 flex flex-col min-h-screen">
      <!-- 顶部导航栏 -->
      <header class="sticky top-0 z-40 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800">
        <UContainer class="py-4">
          <div class="flex items-center justify-between">
            <div class="flex items-center space-x-4">
              <UButton
                to="/chat"
                variant="ghost"
                color="gray"
                icon="i-heroicons-arrow-left"
                size="sm"
              />
              <div class="flex items-center space-x-3">
                <div class="relative">
                  <UIcon
                    name="i-heroicons-sparkles"
                    class="w-8 h-8 text-primary-500"
                  />
                  <div class="absolute -top-1 -right-1 w-3 h-3 bg-green-400 rounded-full border-2 border-white dark:border-gray-900" />
                </div>
                <div>
                  <h1 class="text-lg font-semibold text-gray-900 dark:text-white">
                    {{ currentSession?.title || 'AI智能助手' }}
                  </h1>
                  <p class="text-xs text-gray-500 dark:text-gray-400">
                    {{ currentSession ? `ID: ${currentSession.id}` : '' }}
                  </p>
                </div>
              </div>
            </div>
            
            <div class="flex items-center space-x-2">
              <!-- 清空对话 -->
              <UButton
                v-if="messages.length > 1"
                variant="ghost"
                color="gray"
                icon="i-heroicons-trash"
                size="sm"
                @click="handleClearMessages"
              >
                清空
              </UButton>
              

              <!-- 主题切换 -->
              <UButton
                :icon="isDark ? 'i-heroicons-sun' : 'i-heroicons-moon'"
                variant="ghost"
                color="gray"
                size="sm"
                @click="toggleColorMode"
              />
            </div>
          </div>
        </UContainer>
      </header>

      <!-- 通知提示 -->
      <div
        v-if="notification.show"
        class="fixed top-4 right-4 z-50 px-4 py-3 rounded-lg shadow-lg flex items-center space-x-2 animate-fade-in text-white"
        :class="notification.type === 'warning' ? 'bg-orange-500' : 'bg-green-500'"
      >
        <UIcon
          :name="notification.type === 'warning' ? 'i-heroicons-exclamation-triangle' : 'i-heroicons-check-circle'"
          class="w-5 h-5"
        />
        <div>
          <div class="font-medium">
            {{ notification.title }}
          </div>
          <div class="text-sm opacity-90">
            {{ notification.description }}
          </div>
        </div>
      </div>

      <!-- 聊天内容区域 -->
      <main class="flex-1 pb-32">
        <UContainer class="py-6">
          <!-- 欢迎界面 -->
          <div
            v-if="messages.filter(msg => msg.id !== 'welcome').length === 0"
            class="text-center py-16"
          >
            <div class="relative mb-6">
              <div class="w-20 h-20 mx-auto bg-gradient-to-br from-primary-500 to-primary-600 rounded-2xl flex items-center justify-center shadow-lg">
                <UIcon
                  name="i-heroicons-sparkles"
                  class="w-10 h-10 text-white"
                />
              </div>
              <div class="absolute -top-2 -right-2 w-6 h-6 bg-green-400 rounded-full border-4 border-gray-50 dark:border-gray-900" />
            </div>
          
            <h2 class="text-2xl font-bold text-gray-900 dark:text-white mb-3">
              欢迎使用AI智能助手
            </h2>
            <p class="text-gray-600 dark:text-gray-400 max-w-md mx-auto mb-8">
              我是 AI 智能助手，可以为你答疑解惑，轻松工作。
            </p>
          </div>
          <!-- 消息列表 -->
          <div
            v-else
            ref="messagesContainer"
            class="space-y-6"
          >
            <div
              v-for="message in messages.filter(msg => msg.id !== 'welcome')"
              :key="message.id"
              class="group"
            >
              <!-- 用户消息 -->
              <div
                v-if="message.role === 'user'"
                class="flex justify-end"
              >
                <div class="flex items-start space-x-3 max-w-2xl">
                  <div class="flex-1 min-w-0">
                    <div class="bg-primary-500 text-white rounded-2xl rounded-tr-md px-4 py-3">
                      <p class="text-sm whitespace-pre-wrap">
                        {{ message.content }}
                      </p>
                    </div>
                    <div class="text-xs text-gray-500 mt-1 text-right">
                      {{ formatTime(message.timestamp) }}
                    </div>
                  </div>
                  <UAvatar
                    icon="i-heroicons-user"
                    size="sm"
                    :ui="{ background: 'bg-primary-500 dark:bg-primary-400' }"
                  />
                </div>
              </div>

              <!-- AI消息 -->
              <div
                v-else
                class="flex justify-start"
              >
                <div class="flex items-start space-x-3 max-w-2xl">
                  <UAvatar
                    icon="i-heroicons-sparkles"
                    size="sm"
                    :ui="{ background: 'bg-green-500 dark:bg-green-400' }"
                  />
                  <div class="flex-1 min-w-0">
                    <div class="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-2xl rounded-tl-md px-4 py-3 shadow-sm">
                      <div class="prose prose-sm dark:prose-invert max-w-none">
                        <div v-html="renderMarkdown(message.content, message.isStreaming)"></div>
                        <!-- 流式加载指示器 -->
                        <div
                          v-if="message.isStreaming"
                          class="inline-flex items-center space-x-1 ml-1"
                        >
                          <div class="w-1 h-4 bg-green-500 rounded-full animate-pulse" />
                        </div>
                      </div>
                    </div>
                    <div class="flex items-center justify-between mt-1">
                      <div class="text-xs text-gray-500">
                        {{ formatTime(message.timestamp) }}
                      </div>
                      <UButton
                        v-if="!message.isStreaming && message.content"
                        variant="ghost"
                        color="gray"
                        icon="i-heroicons-clipboard-document"
                        size="2xs"
                        class="opacity-0 group-hover:opacity-100 transition-opacity"
                        @click="copyMessage(message.content)"
                      />
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 正在输入指示器 -->
            <div
              v-if="isLoading && !hasStreamingMessage"
              class="flex justify-start"
            >
              <div class="flex items-start space-x-3 max-w-2xl">
                <UAvatar
                  icon="i-heroicons-sparkles"
                  size="sm"
                  :ui="{ background: 'bg-green-500 dark:bg-green-400' }"
                />
                <div class="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-2xl rounded-tl-md px-4 py-3 shadow-sm">
                  <div class="flex items-center space-x-2">
                    <div class="flex space-x-1">
                      <div
                        class="w-2 h-2 bg-gray-400 rounded-full animate-bounce"
                        style="animation-delay: 0ms;"
                      />
                      <div
                        class="w-2 h-2 bg-gray-400 rounded-full animate-bounce"
                        style="animation-delay: 150ms;"
                      />
                      <div
                        class="w-2 h-2 bg-gray-400 rounded-full animate-bounce"
                        style="animation-delay: 300ms;"
                      />
                    </div>
                    <span class="text-sm text-gray-500">AI正在思考...</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </UContainer>
      </main>

      <!-- 底部输入区域 -->
      <div class="fixed bottom-0 left-80 right-0 bg-white dark:bg-gray-900 border-t border-gray-200 dark:border-gray-800 p-4 z-30">
        <UContainer>
          <!-- AI回答状态提示 -->
          <div 
            v-if="currentSession && isCurrentSessionStreaming()"
            class="mb-3 p-3 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg flex items-center justify-between"
          >
            <div class="flex items-center space-x-2">
              <div class="flex space-x-1">
                <div
                  class="w-2 h-2 bg-blue-500 rounded-full animate-bounce"
                  style="animation-delay: 0ms;"
                />
                <div
                  class="w-2 h-2 bg-blue-500 rounded-full animate-bounce"
                  style="animation-delay: 150ms;"
                />
                <div
                  class="w-2 h-2 bg-blue-500 rounded-full animate-bounce"
                  style="animation-delay: 300ms;"
                />
              </div>
              <span class="text-sm text-blue-700 dark:text-blue-300">AI正在回答中...</span>
            </div>
            <UButton
              variant="soft"
              color="red"
              size="sm"
              icon="i-heroicons-stop"
              @click="stopCurrentAnswer"
            >
              停止回答
            </UButton>
          </div>
          
          <div class="flex items-end space-x-3">
            <div class="flex-1">
              <UTextarea
                v-model="currentMessage"
                placeholder="输入您的问题..."
                :rows="1"
                class="resize-none"
                @keydown.enter.prevent="handleEnterKey"
              />
            </div>
            <UButton
              icon="i-heroicons-paper-airplane"
              :disabled="!currentMessage.trim()"
              :loading="isLoading && !currentSession"
              @click="handleSendMessage"
            />
          </div>
        </UContainer>
      </div>
    </div>


  </div>
</template>
  
<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { smartRenderMarkdown } from '~/composables/useSmartMarkdown'

// 聊天功能
const {
  messages,
  isLoading,
  currentMessage,
  isInputEnabled,
  sendMessage,
  sendMessageWithSession,
  clearMessages,
  resetToWelcomeState,
  loadSessionMessages,
  switchToSession,
  stopCurrentAnswer,
  isCurrentSessionStreaming,
  closeEventSource
} = useChat()

// 会话管理
const { 
  currentSession, 
  createSession,
  switchSession, 
  updateSessionTitle 
} = useSession()

// 页面元数据
useHead({
  title: computed(() => currentSession.value ? `${currentSession.value.title} - AI智能助手` : 'AI智能助手 - 聊天'),
  meta: [
    {
      name: 'description',
      content: '与AI智能助手进行对话，获取专业的金融投资建议'
    }
  ]
})

// 主题切换
const colorMode = useColorMode()
const isDark = computed(() => colorMode.value === 'dark')

const toggleColorMode = () => {
  colorMode.preference = colorMode.value === 'dark' ? 'light' : 'dark'
}

// 通知状态管理
const notification = ref({
  show: false,
  title: '',
  description: '',
  type: 'success' as 'success' | 'warning'
})



// 显示通知
const showNotification = (title: string, description: string, type: 'success' | 'warning' = 'success') => {
  notification.value = {
    show: true,
    title,
    description,
    type
  }
  
  // 2秒后自动隐藏
  setTimeout(() => {
    notification.value.show = false
  }, 2000)
}

// 处理清空消息
const handleClearMessages = () => {
  clearMessages()
}

// 处理发送消息（主要逻辑）
const handleSendMessage = async () => {
  const userMessage = currentMessage.value.trim()
  if (!userMessage) return

  // 检查当前会话是否有AI正在回答
  if (currentSession.value && isCurrentSessionStreaming()) {
    showNotification('回答输出中', '请稍后操作或停止回答', 'warning')
    return
  }

  try {
    // 如果当前没有会话，先创建一个新会话
    if (!currentSession.value) {
      console.log('没有当前会话，创建新会话...')
      
      const newSession = await createSession({ 
        title: '新对话'
      })
      
      console.log('新会话创建成功:', newSession.id)
      
      // 更新当前会话（useSession）
      await switchSession(newSession.id)
      
      // 更新 URL 查询参数
      await navigateTo(`/chat?session=${newSession.id}`, { replace: true })
      
      // 检查是否需要更新会话标题
      if (userMessage) {
        try {
          // 限制标题长度，取用户问题的前30个字符
          const newTitle = userMessage.length > 30 ? userMessage.substring(0, 30) + '...' : userMessage
          await updateSessionTitle(newSession.id, newTitle)
          console.log('已更新会话标题为:', newTitle)
        } catch (error) {
          console.error('更新会话标题失败:', error)
        }
      }
      
      // 先同步聊天状态（useChat），确保sessionId正确设置
      switchToSession(newSession.id)
      
      // 发送消息到新会话
      await sendMessageWithSession(newSession.id)
    } else {
      // 如果已有会话，检查是否需要更新标题
      const shouldUpdateTitle = currentSession.value.title === '新对话' && 
        userMessage && 
        // 检查是否是第一条用户消息
        (messages.value.length <= 1 || !messages.value.some(msg => msg.role === 'user'))
      
      if (shouldUpdateTitle) {
        try {
          const newTitle = userMessage.length > 30 ? userMessage.substring(0, 30) + '...' : userMessage
          await updateSessionTitle(currentSession.value.id, newTitle)
          console.log('已更新会话标题为:', newTitle)
        } catch (error) {
          console.error('更新会话标题失败:', error)
        }
      }
      
      // 发送消息到现有会话
      await sendMessageWithSession(currentSession.value.id)
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    // 如果有会话创建或切换的问题，回退到无会话模式
    if (!currentSession.value) {
      await sendMessage()
    }
  }
}

// 处理会话选择
const handleSessionSelected = async (session: any) => {
  try {
    console.log('切换到会话:', session.id)
    
    // 切换到当前会话（这会优先保持正在进行的对话）
    await switchSession(session.id)
    
    // 尝试切换到已有的会话状态
    switchToSession(session.id)
    
    // 如果会话还没有完整加载过，加载会话消息
    // loadSessionMessages 会检查是否有正在进行的对话，避免中断
    await loadSessionMessages(session.id)
    
    // 更新 URL 参数（不刷新页面）
    await navigateTo(`/chat?session=${session.id}`, { replace: true })
    
    console.log('会话切换完成')
  } catch (error) {
    console.error('切换会话失败:', error)
    resetToWelcomeState()
  }
}

// 处理会话创建（点击 New Chat）
const handleSessionCreated = async (session: any) => {
  // session 为 null 表示这是一个新建会话的请求，而不是实际创建的会话
  if (session === null) {
    // 检查当前是否已经是欢迎界面状态
    const isWelcomeState = !currentSession.value && messages.value.length <= 1
    
    if (isWelcomeState) {
      // 已经是欢迎界面，显示提示
      showNotification('已是最新对话', '当前已在新对话界面')
      console.log('已经是欢迎界面，显示提示')
    } else {
      // 当前在某个会话中，切换到欢迎界面
      console.log('切换到新对话界面')
      
      // 清除当前会话
      currentSession.value = null
      
      // 重置为欢迎状态
      resetToWelcomeState()
      
      // 更新 URL（移除会话参数）
      await navigateTo('/chat', { replace: true })
      
      console.log('已切换到新对话界面')
    }
  } else {
    // 这是实际的会话对象，处理会话切换
    try {
      console.log('切换到会话:', session.id)
      
      // 切换到新会话
      await switchSession(session.id)
      
      // 重置为欢迎状态（新会话没有消息）
      resetToWelcomeState()
      
      // 更新 URL 参数（不刷新页面）
      await navigateTo(`/chat?session=${session.id}`, { replace: true })
      
      console.log('新会话创建完成')
    } catch (error) {
      console.error('切换到新会话失败:', error)
      resetToWelcomeState()
    }
  }
}

// 处理回车键
const handleEnterKey = (event: KeyboardEvent) => {
  // 检查是否正在使用输入法组合输入（如中文输入法）
  if (event.isComposing) {
    // 正在输入法组合中，不处理回车键
    return
  }
  
  if (event.shiftKey) {
    // Shift+Enter 换行
    return
  }
  
  // 阻止默认行为
  event.preventDefault()
  
  // 发送消息
  if (currentMessage.value.trim() && isInputEnabled.value) {
    handleSendMessage()
  }
}

// 检查是否有流式消息
const hasStreamingMessage = computed(() => {
  return messages.value.some(msg => msg.isStreaming)
})

// 格式化时间
const formatTime = (timestamp: Date) => {
  const now = new Date()
  const messageTime = new Date(timestamp)
  
  const diffInMinutes = Math.floor((now.getTime() - messageTime.getTime()) / (1000 * 60))
  
  if (diffInMinutes < 1) {
    return '刚刚'
  } else if (diffInMinutes < 60) {
    return `${diffInMinutes}分钟前`
  } else if (diffInMinutes < 1440) {
    return `${Math.floor(diffInMinutes / 60)}小时前`
  } else {
    return messageTime.toLocaleDateString()
  }
}

// 复制消息
const copyMessage = async (content: string) => {
  try {
    await navigator.clipboard.writeText(content)
    // 可以添加toast提示
  } catch (err) {
    console.error('复制失败:', err)
  }
}



// 渲染 Markdown 为 HTML (使用智能渲染)
function renderMarkdown(content: string, isStreaming: boolean = false) {
  return smartRenderMarkdown(content || '', isStreaming)
}

// 页面离开时清理资源
onBeforeUnmount(() => {
  closeEventSource()
})

// 消息容器引用
const messagesContainer = ref<HTMLElement>()

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    // 滚动到页面底部，留出底部输入框的空间
    const scrollHeight = Math.max(
      document.body.scrollHeight,
      document.documentElement.scrollHeight
    )
    window.scrollTo({
      top: scrollHeight,
      behavior: 'smooth'
    })
  })
}

// 监听消息变化，自动滚动
watch(messages, () => {
  scrollToBottom()
}, { deep: true, flush: 'post' })

// 监听消息数组长度变化，确保新消息时滚动
watch(() => messages.value.length, () => {
  scrollToBottom()
})

// 监听消息内容变化，确保流式更新时滚动
watch(() => messages.value.map(m => m.content).join(''), () => {
  scrollToBottom()
})

// 页面初始化
onMounted(async () => {
  // 检查 URL 查询参数中是否有会话 ID
  const route = useRoute()
  const sessionIdFromQuery = route.query.session as string
  
  if (sessionIdFromQuery) {
    try {
      console.log('从 URL 参数加载会话:', sessionIdFromQuery)
      
      // 切换到查询参数中的会话
      await switchSession(sessionIdFromQuery)
      
      // 同步聊天状态
      switchToSession(sessionIdFromQuery)
      
      // 加载会话消息
      await loadSessionMessages(sessionIdFromQuery)
      
      console.log('从 URL 参数加载会话完成')
    } catch (error) {
      console.error('从 URL 参数加载会话失败:', error)
      // 如果加载失败，重置为欢迎状态
      resetToWelcomeState()
      // 清除无效的查询参数
      await navigateTo('/chat', { replace: true })
    }
  } else {
    // 没有会话参数，重置为欢迎状态
    resetToWelcomeState()
  }
  })
  </script>

<style scoped>
@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-fade-in {
  animation: fade-in 0.3s ease-out;
}
</style> 