<template>
  <div class="h-screen bg-gray-900 flex flex-col fixed left-0 top-0 w-80 z-10">
    <!-- 新建对话按钮 -->
    <div class="flex-shrink-0 p-4 space-y-2">
      <UButton
        block
        color="blue"
        variant="solid"
        size="sm"
        class="justify-center"
        @click="handleCreateSession"
      >
        <template #leading>
          <UIcon name="i-heroicons-plus" />
        </template>
        New chat
      </UButton>
      

    </div>

    <!-- 会话列表 -->
    <div class="flex-1 overflow-hidden min-h-0">
      <div 
        class="h-full overflow-y-auto scrollbar-dark scroll-smooth"
        style="scroll-behavior: smooth;"
      >
        <div
          v-if="isLoading"
          class="px-2 py-4"
        >
          <div class="animate-pulse space-y-2">
            <div
              v-for="i in 3"
              :key="i"
              class="h-10 bg-gray-700 rounded-md"
            />
          </div>
        </div>

        <div
          v-else-if="sessions.length === 0"
          class="px-4 py-8 text-center"
        >
          <div class="text-gray-400">
            <p class="text-sm">
              还没有对话
            </p>
            <p class="text-xs mt-1 opacity-70">
              点击上方按钮开始新对话
            </p>
          </div>
        </div>

        <div
          v-else
          class="px-2 py-2 space-y-1"
        >
          <div
            v-for="session in sessions"
            :key="session.id"
            class="group relative session-item"
          >
            <div
              class="px-3 py-2.5 rounded-md cursor-pointer transition-all duration-200 flex items-center justify-between"
              :class="[
                currentSession?.id === session.id
                  ? 'bg-gray-700 text-white shadow-sm ring-1 ring-gray-600'
                  : 'text-gray-300 hover:bg-gray-800/80'
              ]"
              @click="handleSessionClick(session)"
            >
              <div class="flex-1 min-w-0">
                <h3 
                  class="text-sm font-medium truncate transition-colors"
                  :class="[
                    currentSession?.id === session.id
                      ? 'text-white'
                      : 'text-gray-300 group-hover:text-gray-200'
                  ]"
                >
                  {{ session.title }}
                </h3>
              </div>
              
              <!-- 删除按钮 -->
              <UButton
                icon="i-heroicons-trash"
                size="2xs"
                variant="ghost"
                color="gray"
                class="opacity-0 group-hover:opacity-100 transition-all duration-200 ml-2 text-gray-400 hover:text-red-400 hover:bg-red-500/10"
                @click.stop="handleDeleteSession(session)"
              />
            </div>
          </div>
        </div>
        
        <!-- 底部间距，避免最后一项被遮挡 -->
        <div class="h-4" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useSession } from '~/composables/useSession'

interface Props {
  currentSession?: any
}

interface Emits {
  (e: 'session-selected', session: any): void
  (e: 'session-created', session: any): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 使用会话管理
const { 
  sessions, 
  isLoading, 
  getSessions,
  deleteSession 
} = useSession()

// 处理会话点击
const handleSessionClick = (session: any) => {
  emit('session-selected', session)
}

// 处理创建会话
const handleCreateSession = async () => {
  // 不再实际创建会话，只是发出新建会话的信号
  emit('session-created', null)
}

// 处理删除会话
const handleDeleteSession = async (session: any) => {
  try {
    await deleteSession(session.id)
  } catch (error) {
    console.error('删除会话失败:', error)
  }
}

// 初始化加载会话列表
onMounted(async () => {
  try {
    await getSessions()
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
})
</script> 