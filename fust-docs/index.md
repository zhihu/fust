---
# https://vitepress.dev/reference/default-theme-home-page
layout: home

hero:
  name: "FUST"
  text: "基于Spring Boot的企业级微服务开发框架"
  tagline: 知乎开源的高性能、可扩展微服务框架
  actions:
    - theme: brand
      text: 快速开始
      link: /guide/getting-started
    - theme: alt
      text: 组件概览
      link: /components/

features:
  - title: 微服务架构
    details: 基于Spring Boot 3.x，集成gRPC、Apollo配置中心等核心组件，提供完整的微服务解决方案
  - title: 多环境管理
    details: 支持开发、测试、预发、生产等多环境部署策略，简化环境配置管理
  - title: 灰度发布
    details: 内置灰度发布能力，支持按比例发布和白名单/黑名单，降低发布风险
  - title: 可观测性
    details: 集成OpenTelemetry，提供完整的监控、追踪方案，让系统运行状态一目了然
  - title: 数据访问
    details: 支持Redis多实例、MySQL读写分离等特性，提供高效的数据访问方案
  - title: 配置管理
    details: 集成Apollo配置中心，支持配置热更新，实现配置统一管理
---

