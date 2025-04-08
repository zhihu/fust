<p align="center">
    <strong>🛠️ 一个基于 Spring Boot 的企业级微服务开发框架</strong>
</p>

<p align="center">
    <a href="https://www.apache.org/licenses/LICENSE-2.0">
        <img src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg" alt="License">
    </a>
    <a href="#">
        <img src="https://img.shields.io/maven-central/v/com.zhihu.fust/fust-boot-starter" alt="Maven Version">
    </a>
    <a href="#">
        <img src="https://img.shields.io/badge/JDK-17+-green.svg" alt="JDK Version">
    </a>
</p>

-------------------------------------------------------------------------------

## 📚简介

FUST 谐音 Fast， 是一个基于 Spring Boot 的快速开发框架，提供了：

* 🏗️ **完整的微服务架构** - 基于 Spring Boot 3.x，集成 gRPC、Apollo 配置中心等核心组件
* 🔐 **多环境管理** - 支持开发、测试、预发、生产等多环境部署策略
* 🚀 **灰度发布** - 内置灰度发布能力，支持按比例发布和白名单/黑名单
* 📊 **可观测性** - 集成 OpenTelemetry，提供完整的监控、追踪方案
* 💾 **数据访问** - 支持 Redis 多实例、MySQL 读写分离等特性
* 📝 **配置管理** - 集成 Apollo 配置中心，支持配置热更新
* 🎯 **动态日志** - 自动化的日志配置文件生成，且支持配置中心动态控制

## 📝快速开始

1. 确保您的开发环境满足以下要求：
   - JDK 17 或更高版本
   - Gradle 8.3+（用于构建FUST框架）
   - Buf（用于生成Protocol Buffers代码）

2. 克隆项目：

```bash
git clone https://github.com/zhihu/fust
```

3. 构建项目：

```bash
# 发布到本地Maven仓库
./gradlew publishToMavenLocal
```


## 📝文档

FUST 官方文档网站已上线：[https://zhihu.github.io/fust/](https://zhihu.github.io/fust/)

推荐通过官方文档网站学习和了解 FUST 框架的各项功能和最佳实践。

如需本地构建文档，fust-docs 使用 vitepress 构建：

- 安装 vitepress
```bash
npm add -D vitepress
```

- 运行文档
```bash
npm run docs:dev
```

## ⚙️Gradle 配置发布到远程仓库

### gradle.properties 远程仓库配置

在用户目录下创建 `~/.gradle/gradle.properties` 文件，添加远程仓库配置：

```properties
# Maven 发布配置
fustRepoUrlRelease=https://your-release-repo-url
fustRepoUrlSnapshots=https://your-snapshots-repo-url
fustRepoUser=your-repo-username
fustRepoPwd=your-repo-password
```

根据您的实际环境修改上述配置，特别是仓库地址和认证信息。

### 发布到远程仓库
```bash
./gradlew publish
```


## 🎨核心功能

### 1. 环境管理

- 开发环境 (Development)
- 集成环境 (Integration)
- 测试环境 (Testing)
- 预发环境 (Staging)
- 生产环境 (Production)

### 2. 配置管理

- Apollo 配置中心集成
- 多环境配置支持
- 配置热更新
- 本地缓存支持

### 3. 服务通信

- gRPC 服务支持
- HTTP JSON 转码
- API 文档自动生成
- 健康检查

### 4. 数据访问

- Redis 多实例管理
- 数据库连接池配置
- MyBatis 增强功能
- 读写分离支持

### 5. 灰度发布

- 支持按比例灰度
- 支持白名单/黑名单
- 支持动态调整

### 6. 可观测性

- 日志管理
- 链路追踪
- 指标收集
- 性能监控

## 🧰技术栈

### 基础框架

- Spring Boot 3.4.1
- MyBatis 3.0.4
- Armeria 1.31.3 (RPC框架)

### 存储

- Redis (支持 Lettuce/Jedis)
- MySQL

### 可观测性

- OpenTelemetry 1.46.0

## 📦模块说明

### fust-base (基础模块)

- fust-core: 框架核心功能，包含基础接口定义和通用实现
- fust-provider: SPI 提供者接口，定义框架扩展点
- fust-commons: 通用工具类库

### fust-components (组件模块)

- fust-config-apollo: Apollo 配置中心集成
- fust-config-extension: 配置扩展功能，支持配置热更新
- fust-armeria-grpc: gRPC 服务框架集成
- fust-armeria-commons: Armeria 通用功能库
- fust-logging-log4j2: Log4j2 日志框架集成
- fust-telemetry: OpenTelemetry 可观测性集成

### fust-boot (自动配置模块)

- fust-boot-jdbc: 数据库连接池自动配置，支持多数据源
- fust-boot-lettuce: Redis Lettuce 客户端自动配置
- fust-boot-jedis: Redis Jedis 客户端自动配置
- fust-boot-grpc: gRPC 服务自动配置
- fust-boot-web: Web 应用自动配置
- fust-boot-log4j2: 日志自动配置
- fust-boot-config: 配置中心自动配置
- fust-boot-mybatis: MyBatis 自动配置

### fust-spring (Spring 集成模块)

- fust-spring-web: Web 开发支持，统一异常处理
- fust-spring-redis-common: Redis 通用功能，支持多实例配置
- fust-spring-mybatis: MyBatis 集成增强，支持动态 SQL
- fust-spring-jdbc: JDBC 相关功能增强
- fust-spring-jedis: Jedis 客户端集成
- fust-spring-lettuce: Lettuce 客户端集成

### examples (示例项目)

- fust-boot-example: 快速启动示例
  - fust-boot-example-business: 业务逻辑层示例
  - fust-boot-example-grpc: gRPC 服务示例
  - fust-boot-example-api: web api 服务示例

## 🎯Maven Archetype 使用指南

FUST 提供了项目脚手架，帮助您快速创建基于 FUST 框架的项目。

### 命令行创建(MacOS)

```bash
export ORIGIN_HOME=$JAVA_HOME && \
export JAVA_HOME="$(/usr/libexec/java_home -v 17)" && \
mvn archetype:generate -DarchetypeGroupId=com.zhihu.fust \
-DarchetypeArtifactId=fust-boot-archetype -DarchetypeVersion=0.1.0 \
-DinteractiveMode=false -DarchetypeCatalog=local -DgroupId=demo -DartifactId=demo-yoda && \
export JAVA_HOME=$ORIGIN_HOME && unset ORIGIN_HOME
```

### 项目结构

使用 archetype 创建的项目将包含以下结构：

```
demo-yoda/
├── build.sh                     # 构建脚本
├── run.sh                       # 运行脚本
├── buf.gen.yaml                 # Buf配置文件
├── checkstyle.xml               # 代码风格检查配置
├── pom.xml                      # 项目父POM
├── proto/                       # Proto定义目录
│   ├── buf.yaml                 # Buf模块配置
│   └── hello/                   # 示例服务Proto定义
│       └── hello.proto          # 示例Proto文件
├── demo-yoda-api/               # API模块
│   ├── pom.xml                  # API模块POM
│   └── src/                     # API源码
├── demo-yoda-business/          # 业务逻辑模块
│   ├── pom.xml                  # 业务模块POM
│   ├── sql/                     # SQL脚本目录
│   └── src/                     # 业务源码
└── demo-yoda-grpc/              # gRPC服务模块
    ├── buf.gen.yaml             # gRPC模块的Buf配置
    ├── pom.xml                  # gRPC模块POM
    └── src/                     # gRPC服务实现源码
```

### 模块说明

- **demo-yoda-api**: 包含API接口定义、数据模型及公共组件，使用传统的Spring MVC提供HTTP REST接口。
- **demo-yoda-business**: 包含业务逻辑实现、数据访问层等核心业务代码。
- **demo-yoda-grpc**: 包含gRPC服务实现，对外提供gRPC接口。


## 🏗️参与贡献

欢迎各种形式的贡献，包括：

- 提交问题和需求
- 修复 bug
- 改进文档
- 提交功能优化

## 📄开源协议

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)
