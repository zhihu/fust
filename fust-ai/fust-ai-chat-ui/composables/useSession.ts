export interface SessionInfo {
  id: string
  title: string
  description?: string
  createTime: Date
  updateTime: Date
  messageCount: number
  lastMessage?: {
    id: string
    role: 'user' | 'assistant'
    content: string
    timestamp: Date
  }
}

export interface CreateSessionRequest {
  title?: string
  description?: string
  initialMessage?: string
}

export interface SessionState {
  sessions: Ref<SessionInfo[]>
  currentSession: Ref<SessionInfo | null>
  isLoading: Ref<boolean>
}

// 全局状态（单例模式）
const globalSessions = ref<SessionInfo[]>([])
const globalCurrentSession = ref<SessionInfo | null>(null)
const globalIsLoading = ref(false)

/**
 * 会话管理的组合式函数
 * 提供会话的创建、获取、切换和删除功能
 */
export function useSession(): SessionState & {
  createSession: (request: CreateSessionRequest) => Promise<SessionInfo>
  getSessions: () => Promise<SessionInfo[]>
  getSessionDetail: (sessionId: string) => Promise<any>
  deleteSession: (sessionId: string) => Promise<void>
  sessionExists: (sessionId: string) => Promise<boolean>
  switchSession: (sessionId: string) => Promise<void>
  refreshSessions: () => Promise<void>
  updateSessionTitle: (sessionId: string, title: string) => Promise<void>
} {
  // 使用全局状态
  const sessions = globalSessions
  const currentSession = globalCurrentSession
  const isLoading = globalIsLoading

  // 配置
  const config = useRuntimeConfig()
  const apiBase = config.public.apiBase || '/api'

  /**
   * 创建新会话
   */
  async function createSession(request: CreateSessionRequest): Promise<SessionInfo> {
    isLoading.value = true
    
    try {
      const response = await $fetch<SessionInfo>(`${apiBase}/chat/sessions`, {
        method: 'POST',
        body: request
      })
      
      // 添加到会话列表顶部
      sessions.value.unshift(response)
      // 设置为当前会话
      currentSession.value = response
      
      console.log('成功创建并切换到新会话:', response.id)
      return response
    } catch (error) {
      console.error('创建会话失败:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 获取会话列表
   */
  async function getSessions(): Promise<SessionInfo[]> {
    isLoading.value = true
    
    try {
      const response = await $fetch<SessionInfo[]>(`${apiBase}/chat/sessions`)
      sessions.value = response
      return response
    } catch (error) {
      console.error('获取会话列表失败:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 获取会话详情
   */
  async function getSessionDetail(sessionId: string): Promise<any> {
    isLoading.value = true
    
    try {
      const response = await $fetch(`${apiBase}/chat/sessions/${sessionId}`)
      return response
    } catch (error) {
      console.error('获取会话详情失败:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 删除会话
   */
  async function deleteSession(sessionId: string): Promise<void> {
    isLoading.value = true
    
    try {
      await $fetch(`${apiBase}/chat/sessions/${sessionId}`, {
        method: 'DELETE'
      })
      
      sessions.value = sessions.value.filter(session => session.id !== sessionId)
      
      if (currentSession.value?.id === sessionId) {
        currentSession.value = null
      }
    } catch (error) {
      console.error('删除会话失败:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 检查会话是否存在
   */
  async function sessionExists(sessionId: string): Promise<boolean> {
    try {
      // 先从本地会话列表查找
      let session = sessions.value.find(s => s.id === sessionId)
      
      // 如果本地没有，刷新会话列表再查找
      if (!session) {
        await getSessions()
        session = sessions.value.find(s => s.id === sessionId)
      }
      
      return session !== undefined
    } catch (error) {
      console.error('检查会话是否存在失败:', error)
      return false
    }
  }

  /**
   * 切换会话
   */
  async function switchSession(sessionId: string): Promise<void> {
    let session = sessions.value.find(s => s.id === sessionId)
    
    // 如果没找到会话，先刷新会话列表再尝试
    if (!session) {
      console.log('会话不在当前列表中，刷新会话列表...')
      await getSessions()
      session = sessions.value.find(s => s.id === sessionId)
    }
    
    if (session) {
      currentSession.value = session
      console.log('成功切换到会话:', sessionId)
    } else {
      console.error('会话真的不存在:', sessionId)
      throw new Error('会话不存在')
    }
  }

  /**
   * 刷新会话列表
   */
  async function refreshSessions(): Promise<void> {
    await getSessions()
  }

  /**
   * 更新会话标题
   */
  async function updateSessionTitle(sessionId: string, title: string): Promise<void> {
    try {
      // 调用后端API更新标题
      await $fetch(`${apiBase}/chat/sessions/${sessionId}/title`, {
        method: 'PUT',
        body: { title }
      })
      
      // 更新本地状态
      const session = sessions.value.find(s => s.id === sessionId)
      if (session) {
        session.title = title
      }
      
      // 如果是当前会话，也更新当前会话的标题
      if (currentSession.value?.id === sessionId) {
        currentSession.value.title = title
      }
      
      console.log('成功更新会话标题:', sessionId, title)
    } catch (error) {
      console.error('更新会话标题失败:', error)
      // 如果后端API不存在，先使用前端本地更新
      const session = sessions.value.find(s => s.id === sessionId)
      if (session) {
        session.title = title
      }
      if (currentSession.value?.id === sessionId) {
        currentSession.value.title = title
      }
    }
  }

  return {
    sessions,
    currentSession,
    isLoading,
    createSession,
    getSessions,
    getSessionDetail,
    deleteSession,
    sessionExists,
    switchSession,
    refreshSessions,
    updateSessionTitle
  }
} 