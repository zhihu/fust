// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  devtools: { enabled: true },
  
  // 启用SPA模式
  ssr: false,
  
  // 模块配置
  modules: [
    '@nuxt/ui',
    '@pinia/nuxt',
    '@vueuse/nuxt'
  ],
  
  // UI配置
  ui: {
    global: true
  },
  
  // CSS配置
  css: ['~/assets/css/main.css'],
  
  // 开发服务器配置
  devServer: {
    port: 3000,
    host: '0.0.0.0'
  },
  
  // 代理配置（开发时使用）
  nitro: {
    preset: 'static',
    devProxy: {
      '/api': {
        target: 'http://localhost:8080/api',
        changeOrigin: true,
        prependPath: true
      }
    }
  },
  
  // 构建配置
  build: {
    analyze: false
  },
  
  // 运行时配置
  runtimeConfig: {
    public: {
      apiBase: process.env.NODE_ENV === 'development' ? '/api' : '/api'
    }
  },
  
  // 路由规则配置
  routeRules: {
    '/': { redirect: '/chat' }
  },
  
  // 应用配置
  app: {
    baseURL: '/',
    buildAssetsDir: '/_nuxt/',
    head: {
      title: 'AI Chat',
      meta: [
        { charset: 'utf-8' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
        { key: 'description', name: 'description', content: 'AI Chat' }
      ],
      link: [
        { rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' }
      ],
      script: [
        { 
          src: 'https://cdn.jsdelivr.net/npm/markdown-it@14.1.0/dist/markdown-it.min.js',
          defer: true
        }
      ]
    }
  },
  
  // TypeScript配置
  typescript: {
    strict: true,
    typeCheck: true
  }
}) 