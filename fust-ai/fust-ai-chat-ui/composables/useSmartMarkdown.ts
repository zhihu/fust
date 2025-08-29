import { ref, computed, type Ref } from 'vue'

// 使用 CDN 引入的 markdown-it
declare global {
  interface Window {
    markdownit: any
  }
}

// 获取全局的 markdown-it 实例
const getMarkdownIt = () => {
  if (typeof window !== 'undefined' && window.markdownit) {
    return new window.markdownit({ 
      html: false, 
      linkify: true, 
      breaks: true 
    })
  }
  // 如果 CDN 还未加载，返回一个简单的降级方案
  return {
    render: (content: string) => content.replace(/\n/g, '<br>')
  }
}

// 创建全局markdown-it实例
const md = getMarkdownIt()

// 调试模式开关
const DEBUG_MODE = false

/**
 * 检测内容是否包含未闭合的代码块
 */
function hasUnclosedCodeBlocks(content: string): boolean {
  const codeBlockPattern = /```[\s\S]*?```/g
  const unclosedPattern = /```[^`]*$/
  
  // 移除所有完整的代码块
  const withoutClosedBlocks = content.replace(codeBlockPattern, '')
  
  // 检查是否有未闭合的代码块
  const hasUnclosed = unclosedPattern.test(withoutClosedBlocks)
  
  if (DEBUG_MODE) {
    console.log('🔍 检测未闭合代码块:', {
      content: content.substring(0, 100) + '...',
      withoutClosedBlocks,
      hasUnclosed
    })
  }
  
  return hasUnclosed
}

/**
 * 检测内容是否包含其他不完整的Markdown结构
 */
function hasIncompleteMarkdown(content: string): boolean {
  // 检查未闭合的代码块
  if (hasUnclosedCodeBlocks(content)) {
    if (DEBUG_MODE) console.log('⚠️ 发现未闭合代码块')
    return true
  }
  
  // 检查不完整的链接格式 [text](
  const incompleteLinks = /\[([^\]]*)\]\s*\(\s*$/
  if (incompleteLinks.test(content)) {
    if (DEBUG_MODE) console.log('⚠️ 发现不完整链接')
    return true
  }
  
  // 检查不完整的图片格式 ![alt](
  const incompleteImages = /!\[([^\]]*)\]\s*\(\s*$/
  if (incompleteImages.test(content)) {
    if (DEBUG_MODE) console.log('⚠️ 发现不完整图片')
    return true
  }
  
  return false
}

/**
 * 智能Markdown渲染策略
 */
export function useSmartMarkdown(content: Ref<string>, isStreaming: Ref<boolean>) {
  const renderingStrategy = ref<'safe' | 'preview' | 'complete'>('safe')
  
  // 计算是否应该渲染为Markdown
  const shouldRenderMarkdown = computed(() => {
    if (!isStreaming.value) {
      // 流式传输完成，始终渲染Markdown
      return true
    }
    
    // 流式传输中，检查是否安全渲染
    return !hasIncompleteMarkdown(content.value)
  })
  
  // 计算渲染后的HTML
  const renderedHtml = computed(() => {
    if (!shouldRenderMarkdown.value) {
      // 不安全渲染时，返回格式化的纯文本
      return formatPlainText(content.value)
    }
    
    try {
      return md.render(content.value || '')
    } catch (error) {
      console.warn('Markdown渲染错误:', error)
      return formatPlainText(content.value)
    }
  })
  
  // 计算渲染模式
  const renderMode = computed(() => {
    if (!isStreaming.value) {
      return 'markdown'
    }
    return shouldRenderMarkdown.value ? 'safe-markdown' : 'plain-text'
  })
  
  return {
    renderedHtml,
    renderMode,
    shouldRenderMarkdown
  }
}

/**
 * 自动修正Markdown格式问题
 */
function autoFixMarkdown(content: string): string {
  if (!content) return ''
  
  return content
    // 修正标题格式：### 后面加空格
    .replace(/^(#{1,6})([^\s#])/gm, '$1 $2')
    // 修正列表格式：- 后面加空格
    .replace(/^(-|\*|\+)([^\s])/gm, '$1 $2')
    // 修正数字列表格式：1. 后面加空格
    .replace(/^(\d+\.)([^\s])/gm, '$1 $2')
    // 修正任务列表格式：- [ ] 和 - [x] 后面加空格
    .replace(/^(-|\*|\+)(\[[x\s]\])([^\s])/gm, '$1$2 $3')
}

/**
 * 格式化纯文本，保持基本的换行结构
 */
function formatPlainText(text: string): string {
  if (!text) return ''
  
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
    .replace(/\n/g, '<br>')
}

/**
 * 渲染Markdown内容的工具函数
 */
export function renderMarkdown(content: string): string {
  if (!content) return ''
  
  try {
    return md.render(content)
  } catch (error) {
    console.warn('Markdown渲染错误:', error)
    return formatPlainText(content)
  }
}

/**
 * 快速检测是否为安全的Markdown内容
 */
export function isSafeMarkdown(content: string): boolean {
  return !hasIncompleteMarkdown(content)
}

/**
 * 简化版本：仅用于替换现有的renderMarkdown函数
 */
export function smartRenderMarkdown(content: string, isStreaming: boolean = false): string {
  if (!content) return ''
  
  // 调试信息
  if (DEBUG_MODE) {
    console.log('🎯 智能Markdown渲染:', {
      contentLength: content.length,
      isStreaming,
      contentPreview: content.substring(0, 100) + '...'
    })
  }
  
  // 如果正在流式传输且包含不完整的Markdown，返回格式化的纯文本
  if (isStreaming && hasIncompleteMarkdown(content)) {
    if (DEBUG_MODE) console.log('📝 使用纯文本渲染')
    return formatPlainText(content)
  }
  
  // 否则正常渲染Markdown（先自动修正格式）
  try {
    const fixedContent = autoFixMarkdown(content)
    const result = md.render(fixedContent)
    if (DEBUG_MODE) console.log('✨ 使用Markdown渲染（已自动修正格式）')
    return result
  } catch (error) {
    console.warn('Markdown渲染错误:', error)
    return formatPlainText(content)
  }
}

/**
 * 调试版本的智能渲染函数
 */
export function debugSmartRenderMarkdown(content: string, isStreaming: boolean = false): {
  html: string
  debug: {
    contentLength: number
    isStreaming: boolean
    hasIncompleteMarkdown: boolean
    renderMode: string
    contentPreview: string
  }
} {
  const hasIncomplete = hasIncompleteMarkdown(content)
  const renderMode = isStreaming && hasIncomplete ? 'plain-text' : 'markdown'
  
  let html: string
  
  if (isStreaming && hasIncomplete) {
    html = formatPlainText(content)
  } else {
    try {
      html = md.render(content)
    } catch (error) {
      console.warn('Markdown渲染错误:', error)
      html = formatPlainText(content)
    }
  }
  
  return {
    html,
    debug: {
      contentLength: content.length,
      isStreaming,
      hasIncompleteMarkdown: hasIncomplete,
      renderMode,
      contentPreview: content.substring(0, 100) + (content.length > 100 ? '...' : '')
    }
  }
} 