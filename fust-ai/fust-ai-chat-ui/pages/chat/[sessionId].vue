<template>
  <div class="min-h-screen bg-gray-50 dark:bg-gray-900 flex">
    <!-- 侧边栏 -->
    <div class="w-80 flex-shrink-0">
      <SessionList
        :current-session="currentSession"
        @session-selected="handleSessionSelected"
        @session-created="handleSessionCreated"
      />
    </div>
    
    <!-- 主内容区域 -->
    <div class="flex-1 flex flex-col">
      <!-- 顶部导航栏 -->
      <header class="sticky top-0 z-50 bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800">
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
                    ID: {{ sessionId }}
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
    </div>

    <!-- 底部输入区域 -->
    <div class="fixed bottom-0 left-80 right-0 bg-white dark:bg-gray-900 border-t border-gray-200 dark:border-gray-800 p-4">
      <UContainer>
        <div class="flex items-end space-x-3">
          <div class="flex-1">
            <UTextarea
              v-model="currentMessage"
              placeholder="输入您的问题..."
              :rows="1"
              :disabled="!isInputEnabled"
              class="resize-none"
              @keydown.enter.prevent="handleEnterKey"
            />
          </div>
          <UButton
            icon="i-heroicons-paper-airplane"
            :disabled="!currentMessage.trim() || !isInputEnabled"
            :loading="isLoading"
            @click="handleSendMessage"
          />
        </div>
      </UContainer>
    </div>


  </div>
</template>
  
<script setup lang="ts">
import { smartRenderMarkdown } from '~/composables/useSmartMarkdown'
// 获取路由参数
const route = useRoute()
const router = useRouter()
const sessionId = computed(() => route.params.sessionId as string)

// 聊天功能
const {
  messages,
  isLoading,
  currentMessage,
  isInputEnabled,
  sendMessageWithSession,
  clearMessages,
  clearAllMessages,
  resetToWelcomeState,
  loadSessionMessages,
  closeEventSource
} = useChat()

// 会话管理
const { 
  currentSession, 
  switchSession, 
  updateSessionTitle, 
  getSessions,
  sessionExists 
} = useSession()

// 页面初始化
const isPageLoading = ref(true)
const errorMessage = ref('')



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

// 渲染 Markdown 为 HTML (使用智能渲染)
function renderMarkdown(content: string, isStreaming: boolean = false) {
  return smartRenderMarkdown(content || '', isStreaming)
}

// 初始化会话
const initializeSession = async () => {
  try {
    isPageLoading.value = true
    errorMessage.value = ''
    
    console.log('初始化会话:', sessionId.value)
    
    // 先尝试切换到会话，如果成功则继续，失败则检查存在性
    try {
      await switchSession(sessionId.value)
      console.log('成功切换到会话')
    } catch (switchError) {
      console.log('切换会话失败，检查会话是否存在:', switchError)
      
      // 检查会话是否存在
      const exists = await sessionExists(sessionId.value)
      if (!exists) {
        errorMessage.value = '会话不存在'
        console.error('会话不存在:', sessionId.value)
        // 重定向到新会话
        await navigateTo('/chat', { replace: true })
        return
      }
      
      // 如果存在但切换失败，重试切换
      await switchSession(sessionId.value)
    }
    
    // 加载会话消息
    await loadSessionMessages(sessionId.value)
    
    console.log('会话初始化完成')
  } catch (error) {
    console.error('初始化会话失败:', error)
    errorMessage.value = '加载会话失败'
    // 重定向到新会话
    await navigateTo('/chat', { replace: true })
  } finally {
    isPageLoading.value = false
  }
}

// 监听路由变化
watch(() => sessionId.value, async (newSessionId) => {
  if (newSessionId) {
    await initializeSession()
  }
}, { immediate: true })

// 处理清空消息
const handleClearMessages = () => {
  clearMessages()
}

// 处理发送消息
const handleSendMessage = async () => {
  // 保存当前用户输入，用于可能的标题更新
  const userMessage = currentMessage.value.trim()
  
  // 检查是否需要更新会话标题
  const shouldUpdateTitle = currentSession.value && 
    currentSession.value.title === '新对话' && 
    userMessage && 
    // 检查是否是第一条用户消息（消息历史中只有欢迎消息或为空）
    (messages.value.length <= 1 || !messages.value.some(msg => msg.role === 'user'))
  
  // 如果需要，在发送消息前立即更新标题
  if (shouldUpdateTitle && currentSession.value) {
    try {
      // 限制标题长度，取用户问题的前30个字符
      const newTitle = userMessage.length > 30 ? userMessage.substring(0, 30) + '...' : userMessage
      await updateSessionTitle(currentSession.value.id, newTitle)
      console.log('已立即更新会话标题为:', newTitle)
    } catch (error) {
      console.error('自动更新会话标题失败:', error)
    }
  }
  
  await sendMessageWithSession(sessionId.value)
}

// 处理会话选择
const handleSessionSelected = async (session: any) => {
  try {
    console.log('切换到会话:', session.id)
    // 导航到新的会话 URL
    await navigateTo(`/chat/${session.id}`)
  } catch (error) {
    console.error('切换会话失败:', error)
  }
}

// 处理会话创建
const handleSessionCreated = async (session: any) => {
  try {
    console.log('创建新会话:', session.id)
    // 导航到新的会话 URL
    await navigateTo(`/chat/${session.id}`)
  } catch (error) {
    console.error('切换到新会话失败:', error)
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
</script> 