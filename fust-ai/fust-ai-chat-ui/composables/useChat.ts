export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: Date
  isStreaming?: boolean
}

// 字符缓冲区管理
interface CharacterBuffer {
  buffer: string[]
  displayed: string
  intervalId: ReturnType<typeof setInterval> | null
}

// 会话消息状态
interface SessionState {
  messages: Message[]
  isLoading: boolean
}

export interface ChatState {
  messages: Ref<Message[]>
  isLoading: Ref<boolean>
  currentMessage: Ref<string>
  isInputEnabled: Ref<boolean>
  debugMode: Ref<boolean>
  sessionId: Ref<string | null>
}

/**
 * 聊天功能的组合式函数
 * 提供消息管理、API调用和流式响应处理
 * 支持多会话并发
 */
export function useChat(): ChatState & {
  sendMessage: () => Promise<void>
  sendMessageWithSession: (sessionId: string) => Promise<void>
  addMessage: (role: 'user' | 'assistant', content: string, isStreaming?: boolean) => Message
  updateMessage: (messageId: string, content: string) => void
  appendToMessage: (messageId: string, content: string) => void
  completeStreamingMessage: (messageId: string) => void
  clearMessages: () => void
  clearAllMessages: () => void
  resetToWelcomeState: () => void
  loadSessionMessages: (sessionId: string) => Promise<void>
  switchToSession: (sessionId: string) => void
  stopCurrentAnswer: () => void
  isCurrentSessionStreaming: () => boolean
  generateId: () => string
  log: (message: string, data?: any) => void
  closeEventSource: () => void
} {
  // 欢迎消息模板
  const welcomeMessage: Message = {
    id: 'welcome',
    role: 'assistant',
    content: '👋 你好！我是AI智能助手，有什么问题可以随时问我哦！',
    timestamp: new Date()
  }

  // 多会话状态管理
  const sessionStates = ref<Map<string, SessionState>>(new Map())
  
  // 当前显示的状态
  const messages = ref<Message[]>([welcomeMessage])
  const isLoading = ref(false)
  const currentMessage = ref('')
  const isInputEnabled = ref(true)
  const debugMode = ref(true)
  const sessionId = ref<string | null>(null)

  // 多 EventSource 连接管理
  const eventSources = new Map<string, EventSource>()

  // 字符缓冲区管理
  const characterBuffers = new Map<string, CharacterBuffer>()
  
  // 字符显示配置（固定默认值）
  const CHARACTER_DISPLAY_INTERVAL = 30 // 每30ms显示字符
  const BATCH_SIZE = 2 // 每次显示2个字符

  // 配置
  const config = useRuntimeConfig()
  const apiBase = config.public.apiBase || '/api'

  /**
   * 日志方法
   */
  function log(message: string, data?: any) {
    if (debugMode.value) {
      const timestamp = new Date().toISOString()
      if (data) {
        console.log(`[useChat ${timestamp}] ${message}`, data)
      } else {
        console.log(`[useChat ${timestamp}] ${message}`)
      }
    }
  }

  /**
   * 生成唯一ID
   */
  function generateId(): string {
    return Date.now().toString() + Math.random().toString(36).substr(2, 9)
  }

  /**
   * 创建字符缓冲区
   */
  function createCharacterBuffer(messageId: string): CharacterBuffer {
    const buffer: CharacterBuffer = {
      buffer: [],
      displayed: '',
      intervalId: null
    }
    characterBuffers.set(messageId, buffer)
    return buffer
  }

  /**
   * 启动字符显示定时器
   */
  function startCharacterDisplay(messageId: string) {
    const buffer = characterBuffers.get(messageId)
    if (!buffer || buffer.intervalId) return

    buffer.intervalId = setInterval(() => {
      if (buffer.buffer.length === 0) {
        return
      }

      // 每次显示批量字符
      const charactersToShow = buffer.buffer.splice(0, BATCH_SIZE)
      buffer.displayed += charactersToShow.join('')

      // 更新消息内容
      updateMessageContent(messageId, buffer.displayed)

      log('字符显示更新', { 
        messageId, 
        displayedLength: buffer.displayed.length,
        remainingBuffer: buffer.buffer.length,
        charactersShown: charactersToShow.join('')
      })

      // 如果缓冲区为空，检查是否需要停止定时器
      if (buffer.buffer.length === 0) {
        // 继续运行定时器，等待更多字符
      }
    }, CHARACTER_DISPLAY_INTERVAL)
  }

  /**
   * 停止字符显示定时器
   */
  function stopCharacterDisplay(messageId: string) {
    const buffer = characterBuffers.get(messageId)
    if (!buffer || !buffer.intervalId) return

    clearInterval(buffer.intervalId)
    buffer.intervalId = null

    // 立即显示剩余的所有字符
    if (buffer.buffer.length > 0) {
      buffer.displayed += buffer.buffer.join('')
      buffer.buffer = []
      updateMessageContent(messageId, buffer.displayed)
    }
  }

  /**
   * 清理字符缓冲区
   */
  function clearCharacterBuffer(messageId: string) {
    const buffer = characterBuffers.get(messageId)
    if (buffer && buffer.intervalId) {
      clearInterval(buffer.intervalId)
    }
    characterBuffers.delete(messageId)
  }

  /**
   * 辅助函数：更新会话中的消息并同步到当前显示
   */
  function updateSessionMessage(sessionState: SessionState, messageId: string, updater: (message: Message) => Message) {
    const messageIndex = sessionState.messages.findIndex(m => m.id === messageId)
    if (messageIndex !== -1) {
      // 创建新的消息数组
      const updatedMessages = [...sessionState.messages]
      updatedMessages[messageIndex] = updater(updatedMessages[messageIndex])
      
      // 更新会话状态
      sessionState.messages = updatedMessages
      
      // 如果这是当前显示的会话，同步更新 messages.value
      if (sessionId.value && sessionStates.value.has(sessionId.value)) {
        const currentSessionState = sessionStates.value.get(sessionId.value)!
        if (currentSessionState === sessionState) {
          messages.value = [...updatedMessages]
        }
      }
    }
  }

  /**
   * 更新消息内容（内部方法）
   */
  function updateMessageContent(messageId: string, content: string) {
    sessionStates.value.forEach((sessionState) => {
      updateSessionMessage(sessionState, messageId, (message) => ({
        ...message,
        content
      }))
    })
  }





  /**
   * 获取或创建会话状态
   */
  function getOrCreateSessionState(targetSessionId: string): SessionState {
    if (!sessionStates.value.has(targetSessionId)) {
      sessionStates.value.set(targetSessionId, {
        messages: [welcomeMessage],
        isLoading: false
      })
    }
    return sessionStates.value.get(targetSessionId)!
  }

  /**
   * 切换到指定会话
   */
  function switchToSession(targetSessionId: string) {
    const sessionState = getOrCreateSessionState(targetSessionId)
    
    // 创建新的数组引用以确保响应式更新
    messages.value = [...sessionState.messages]
    isLoading.value = sessionState.isLoading
    sessionId.value = targetSessionId
    
    // 始终启用输入框
    setInputEnabled(true)
    
    log('切换到会话', { 
      sessionId: targetSessionId, 
      messageCount: messages.value.length,
      isLoading: sessionState.isLoading
    })
  }

  /**
   * 添加消息到指定会话
   */
  function addMessageToSession(targetSessionId: string | null, role: 'user' | 'assistant', content: string, isStreaming = false): Message {
    const message: Message = {
      id: generateId(),
      role,
      content,
      timestamp: new Date(),
      isStreaming
    }
    
    if (targetSessionId) {
      // 添加到指定会话
      const sessionState = getOrCreateSessionState(targetSessionId)
      
      // 创建新的消息数组
      let updatedMessages = [...sessionState.messages]
      
      // 如果是第一条用户消息，移除欢迎消息
      if (role === 'user') {
        updatedMessages = updatedMessages.filter(msg => msg.id !== 'welcome')
      }
      
      // 添加新消息
      updatedMessages.push(message)
      
      // 更新会话状态
      sessionState.messages = updatedMessages
      
      // 如果这是当前显示的会话，同步更新 messages.value
      if (sessionId.value === targetSessionId) {
        messages.value = [...updatedMessages]
      }
      
      // 只有当前会话与显示会话不同步时才需要额外更新显示
      if (sessionId.value !== targetSessionId) {
        log('消息添加到非当前会话', { targetSessionId, currentSessionId: sessionId.value })
      }
    } else {
      // 添加到当前显示（无会话状态）
      let updatedMessages = [...messages.value]
      
      // 如果是第一条用户消息，移除欢迎消息
      if (role === 'user') {
        updatedMessages = updatedMessages.filter(msg => msg.id !== 'welcome')
      }
      
      // 添加新消息
      updatedMessages.push(message)
      messages.value = updatedMessages
    }
    
    log('添加消息', { sessionId: targetSessionId, currentSessionId: sessionId.value, role, contentLength: content.length, isStreaming })
    
    return message
  }

  /**
   * 添加消息（兼容原接口）
   */
  function addMessage(role: 'user' | 'assistant', content: string, isStreaming = false): Message {
    return addMessageToSession(sessionId.value, role, content, isStreaming)
  }

  /**
   * 更新消息内容
   */
  function updateMessage(messageId: string, content: string) {
    // 由于 messages.value 引用了 sessionState.messages，只需要更新会话状态
    sessionStates.value.forEach((sessionState) => {
      updateSessionMessage(sessionState, messageId, (message) => ({
        ...message,
        content
      }))
    })
  }

  /**
   * 追加消息内容（用于流式响应）
   * 使用字符缓冲机制实现平滑渲染
   */
  function appendToMessage(messageId: string, content: string) {
    if (!content) return

    // 获取或创建字符缓冲区
    let buffer = characterBuffers.get(messageId)
    if (!buffer) {
      buffer = createCharacterBuffer(messageId)
      startCharacterDisplay(messageId)
    }

    // 将新内容拆分为字符并添加到缓冲区
    const characters = content.split('')
    buffer.buffer.push(...characters)

    log('追加消息内容到缓冲区', { 
      messageId, 
      contentLength: content.length,
      bufferLength: buffer.buffer.length
    })
  }

  /**
   * 完成流式消息
   */
  function completeStreamingMessage(messageId: string) {
    // 停止字符显示定时器并清理缓冲区
    stopCharacterDisplay(messageId)
    clearCharacterBuffer(messageId)

    // 标记消息为非流式状态
    sessionStates.value.forEach((sessionState) => {
      updateSessionMessage(sessionState, messageId, (message) => ({
        ...message,
        isStreaming: false
      }))
    })

    log('完成流式消息', { messageId })
  }

  /**
   * 设置会话加载状态
   */
  function setSessionLoading(targetSessionId: string | null, loading: boolean) {
    if (targetSessionId) {
      const sessionState = getOrCreateSessionState(targetSessionId)
      sessionState.isLoading = loading
      
      // 如果是当前会话，也更新当前显示
      if (sessionId.value === targetSessionId) {
        isLoading.value = loading
        
        log('更新当前会话加载状态', { 
          sessionId: targetSessionId, 
          loading
        })
      }
    } else {
      isLoading.value = loading
    }
  }

  /**
   * 设置输入启用状态
   */
  function setInputEnabled(enabled: boolean) {
    isInputEnabled.value = enabled
    log('设置输入状态', { enabled })
  }

  /**
   * 停止当前会话的AI回答
   */
  function stopCurrentAnswer() {
    if (sessionId.value) {
      // 关闭当前会话的SSE连接
      closeSessionEventSource(sessionId.value)
      
      // 完成所有正在流式传输的消息
      const sessionState = sessionStates.value.get(sessionId.value)
      if (sessionState) {
        // 创建新的消息数组
        const updatedMessages = [...sessionState.messages]
        let hasChanges = false
        
        updatedMessages.forEach((msg, index) => {
          if (msg.isStreaming) {
            // 停止字符显示并清理缓冲区
            stopCharacterDisplay(msg.id)
            clearCharacterBuffer(msg.id)
            
            // 更新消息
            updatedMessages[index] = {
              ...msg,
              isStreaming: false,
              content: msg.content + '\n\n[回答已停止]'
            }
            hasChanges = true
          }
        })
        
        // 如果有变化，更新会话状态
        if (hasChanges) {
          sessionState.messages = updatedMessages
          // 同步更新当前显示
          messages.value = [...updatedMessages]
        }
        
        // 更新会话状态
        sessionState.isLoading = false
        isLoading.value = false
      }
      
      log('停止当前会话回答', { sessionId: sessionId.value })
    }
  }

  /**
   * 检查当前会话是否有AI正在回答
   */
  function isCurrentSessionStreaming(): boolean {
    if (!sessionId.value) return false
    
    const sessionState = sessionStates.value.get(sessionId.value)
    if (!sessionState) return false
    
    return sessionState.isLoading || sessionState.messages.some(msg => msg.isStreaming)
  }

  /**
   * 关闭指定会话的 EventSource 连接
   */
  function closeSessionEventSource(targetSessionId: string) {
    const eventSource = eventSources.get(targetSessionId)
    if (eventSource) {
      eventSource.close()
      eventSources.delete(targetSessionId)
      log('会话SSE连接已关闭', { sessionId: targetSessionId })
    }
  }

  /**
   * 关闭所有 EventSource 连接
   */
  function closeEventSource() {
    eventSources.forEach((eventSource, sessionId) => {
      eventSource.close()
      log('SSE连接已关闭', { sessionId })
    })
    eventSources.clear()

    // 清理所有字符缓冲区
    characterBuffers.forEach((buffer, messageId) => {
      if (buffer.intervalId) {
        clearInterval(buffer.intervalId)
      }
    })
    characterBuffers.clear()
    log('所有字符缓冲区已清理')
  }

  /**
   * 清空消息历史（保留欢迎消息）
   */
  function clearMessages() {
    if (sessionId.value) {
      const sessionState = getOrCreateSessionState(sessionId.value)
      sessionState.messages = [welcomeMessage]
    }
    messages.value = [welcomeMessage]
    log('消息历史已清空')
  }

  /**
   * 完全清空所有消息
   */
  function clearAllMessages() {
    if (sessionId.value) {
      const sessionState = getOrCreateSessionState(sessionId.value)
      sessionState.messages = []
    }
    messages.value = []
    log('所有消息已清空')
  }

  /**
   * 重置为欢迎状态
   */
  function resetToWelcomeState() {
    const newWelcomeMessage = {
      id: 'welcome',
      role: 'assistant' as const,
      content: '👋 你好！我是AI智能助手，开始新的对话吧！',
      timestamp: new Date()
    }
    
    if (sessionId.value) {
      const sessionState = getOrCreateSessionState(sessionId.value)
      sessionState.messages = [newWelcomeMessage]
    }
    
    messages.value = [newWelcomeMessage]
    sessionId.value = null
    isLoading.value = false
    log('重置为欢迎状态')
  }

  /**
   * 开始 EventSource 连接（不带会话）
   */
  function startEventSource(message: string, messageId: string): Promise<void> {
    return new Promise((resolve, reject) => {
      const url = `${apiBase}/chat/stream?message=${encodeURIComponent(message)}`
      const eventSource = new EventSource(url)
      
      log('SSE连接已创建', { url })

      eventSource.onopen = () => {
        log('SSE连接已打开')
      }

      eventSource.onmessage = (event) => {
        const content = event.data
        if (content.length > 0) {
          appendToMessage(messageId, content)
        }
      }

      eventSource.onerror = (error) => {
        eventSource.close()
        const readyState = eventSource?.readyState
        // 检查是否是正常结束
        if (readyState === EventSource.CLOSED) {
          log('SSE连接正常关闭')
          resolve()
        } else {
          console.error('SSE连接错误:', error)
          reject(error)
        }
      }
    })
  }

  /**
   * 开始 EventSource 连接（带会话）
   */
  function startEventSourceWithSession(targetSessionId: string, message: string, messageId: string): Promise<void> {
    return new Promise((resolve, reject) => {
      const url = `${apiBase}/chat/sessions/${targetSessionId}/stream?message=${encodeURIComponent(message)}`
      const eventSource = new EventSource(url)
      
      // 保存连接引用
      eventSources.set(targetSessionId, eventSource)
      
      log('会话SSE连接已创建', { url, sessionId: targetSessionId })

      eventSource.onopen = () => {
        log('会话SSE连接已打开', { sessionId: targetSessionId })
      }

      eventSource.onmessage = (event) => {
        const content = event.data
        if (content.length > 0) {
          appendToMessage(messageId, content)
        }
      }

      eventSource.onerror = (error) => {
        closeSessionEventSource(targetSessionId)
        const readyState = eventSource?.readyState
        // 检查是否是正常结束
        if (readyState === EventSource.CLOSED) {
          log('会话SSE连接正常关闭', { sessionId: targetSessionId })
          resolve()
        } else {
          console.error('会话SSE连接错误:', error)
          reject(error)
        }
      }
    })
  }

  /**
   * 发送消息并处理 SSE 流式响应
   */
  async function sendMessage(): Promise<void> {
    log('开始发送消息', { messageLength: currentMessage.value.length })
    
    if (!currentMessage.value.trim()) {
      log('消息为空，取消发送')
      return
    }

    const userMessage = currentMessage.value.trim()
    currentMessage.value = ''

    // 设置状态
    setSessionLoading(null, true)

    // 添加用户消息
    addMessage('user', userMessage)

    // 创建AI消息
    const aiMessage = addMessage('assistant', '', true)

    try {
      await startEventSource(userMessage, aiMessage.id)
      log('流式响应完成')
    } catch (error) {
      console.error('发送消息失败:', error)
      updateMessage(aiMessage.id, '抱歉，发送消息时出现错误，请重试。')
    } finally {
      completeStreamingMessage(aiMessage.id)
      setSessionLoading(null, false)
    }
  }

  /**
   * 发送消息并处理 SSE 流式响应（带会话）
   */
  async function sendMessageWithSession(targetSessionId: string): Promise<void> {
    log('开始发送会话消息', { sessionId: targetSessionId, messageLength: currentMessage.value.length })
    
    if (!currentMessage.value.trim()) {
      log('消息为空，取消发送')
      return
    }

    const userMessage = currentMessage.value.trim()
    currentMessage.value = ''

    // 设置状态
    setSessionLoading(targetSessionId, true)

    try {
      // 添加用户消息到指定会话
      const userMsg = addMessageToSession(targetSessionId, 'user', userMessage)
      
      // 添加AI消息占位符到指定会话
      const aiMsg = addMessageToSession(targetSessionId, 'assistant', '', true)
      
      // 开始流式连接
      await startEventSourceWithSession(targetSessionId, userMessage, aiMsg.id)
      
      // 完成流式消息
      completeStreamingMessage(aiMsg.id)
      
      log('会话消息发送完成', { sessionId: targetSessionId })
    } catch (error) {
      log('会话消息发送失败', { sessionId: targetSessionId, error })
      throw error
    } finally {
      setSessionLoading(targetSessionId, false)
    }
  }

  /**
   * 加载会话消息（不中断正在进行的对话）
   */
  async function loadSessionMessages(targetSessionId: string): Promise<void> {
    log('开始加载会话消息', { sessionId: targetSessionId })
    
    try {
      const response = await $fetch<any>(`${apiBase}/chat/sessions/${targetSessionId}`)
      
      // 获取或创建会话状态
      const sessionState = getOrCreateSessionState(targetSessionId)
      
      // 检查会话是否有正在进行的对话
      const hasStreamingMessage = sessionState.messages.some(msg => msg.isStreaming)
      
      if (hasStreamingMessage) {
        // 如果有正在进行的对话，不覆盖消息，只是切换显示
        log('会话有正在进行的对话，保持当前状态', { sessionId: targetSessionId })
        switchToSession(targetSessionId)
        return
      }
      
      // 清空当前会话的消息并加载新消息
      if (response.messages && response.messages.length > 0) {
        const loadedMessages: Message[] = []
        for (const msg of response.messages) {
          // 转换后端的枚举角色到前端期望的字符串角色
          const role = msg.type === 'USER' ? 'user' : 'assistant'
          loadedMessages.push({
            id: generateId(),
            role: role as 'user' | 'assistant',
            content: msg.content,
            timestamp: new Date(),
            isStreaming: false
          })
        }
        sessionState.messages = loadedMessages
        log('会话消息加载完成', { sessionId: targetSessionId, messageCount: loadedMessages.length })
      } else {
        // 如果会话没有消息，显示会话欢迎消息
        const welcomeMsg = {
          id: generateId(),
          role: 'assistant' as const,
          content: `欢迎回到这个对话！您可以继续之前的话题，或者开始新的讨论。`,
          timestamp: new Date(),
          isStreaming: false
        }
        sessionState.messages = [welcomeMsg]
        log('会话无消息，显示欢迎消息', { sessionId: targetSessionId })
      }
      
      // 切换到该会话
      switchToSession(targetSessionId)
      
      log('会话消息加载完成', { sessionId: targetSessionId })
    } catch (error) {
      log('会话消息加载失败', { sessionId: targetSessionId, error })
      // 出错时重置为欢迎状态
      resetToWelcomeState()
      throw error
    }
  }

  return {
    // 状态
    messages,
    isLoading,
    currentMessage,
    isInputEnabled,
    debugMode,
    sessionId,
    
    // 方法
    sendMessage,
    sendMessageWithSession,
    addMessage,
    updateMessage,
    appendToMessage,
    completeStreamingMessage,
    clearMessages,
    clearAllMessages,
    resetToWelcomeState,
    loadSessionMessages,
    switchToSession,
    stopCurrentAnswer,
    isCurrentSessionStreaming,
    generateId,
    log,
    closeEventSource
  }
} 