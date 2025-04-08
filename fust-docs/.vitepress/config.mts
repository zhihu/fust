import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "FUST文档",
  description: "一个基于Spring Boot的企业级微服务开发框架",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: '首页', link: '/' },
      { text: '指南', link: '/guide/' },
      { text: '组件', link: '/components/' },
      { text: '示例', link: '/examples/' },
      { text: 'API', link: '/api/' }
    ],

    sidebar: {
      '/guide/': [
        {
          text: '介绍',
          items: [
            { text: '框架概述', link: '/guide/' }
          ]
        },
        {
          text: '开发指南',
          items: [
            { text: '快速开始', link: '/guide/getting-started' },
            { text: '数据访问', link: '/guide/data-access' },
            { text: '服务层开发', link: '/guide/service-layer' },
            { text: 'Redis集成', link: '/guide/redis-integration' },
            { text: 'HTTP服务开发', link: '/guide/http-service' },
            { text: 'gRPC服务开发', link: '/guide/grpc-service' }
          ]
        }
      ],
      '/components/': [
        {
          text: '组件',
          items: [
            { text: '组件概述', link: '/components/' },
            { text: 'gRPC服务', link: '/components/grpc' },
            { text: 'Apollo配置中心', link: '/components/apollo' },
            { text: '日志系统', link: '/components/logging' },
            { text: '可观测性', link: '/components/telemetry' },
            { text: 'JDBC支持', link: '/components/jdbc' },
            { text: 'Redis支持', link: '/components/redis' },
            { text: 'MyBatis增强', link: '/components/mybatis' }
          ]
        }
      ],
      // 其他侧边栏配置...
    },
    socialLinks: [
      { icon: 'github', link: 'https://github.com/zhihu/fust' }
    ]
  },
  ignoreDeadLinks: true,
  base: '/fust/'
})
