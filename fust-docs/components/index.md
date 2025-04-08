# FUST 框架组件

FUST 框架提供了一系列开箱即用的组件，帮助开发者快速构建高性能、可观测、易扩展的应用程序。

## 核心组件

### [gRPC 服务](/components/grpc.md)

FUST的gRPC服务组件基于Armeria构建，提供高性能的gRPC服务器和客户端，支持HTTP/JSON转码、健康检查、请求/响应监控、拦截器扩展和分布式追踪等特性。

### [Apollo 配置中心](/components/apollo.md)

Apollo配置组件提供与Apollo配置中心的无缝集成，支持热更新、配置变更监听、多环境配置管理和多种配置文件格式。

### [日志系统](/components/logging.md)

基于Log4j2构建的高性能日志系统，支持动态日志级别调整、自动配置文件生成、多环境支持和自定义日志模板。

### [可观测性](/components/telemetry.md)

基于OpenTelemetry的可观测性组件，提供分布式追踪、服务调用监控、指标收集和自动链路追踪等能力。

### [JDBC 支持](/components/jdbc.md)

JDBC支持组件提供了主从分离、读写分离、多数据源管理等功能，通过灵活的连接策略配置，帮助开发者高效管理数据库连接。

### [Redis 支持](/components/redis.md)

Redis组件提供了对Redis的全面支持，包括Lettuce和Jedis客户端集成、主从复制、读写分离、多实例管理和可观测性集成。

### [MyBatis 增强](/components/mybatis.md)

MyBatis增强组件提供SQL打印、慢查询监控、多数据源支持、分页增强等功能，帮助开发者更高效地使用MyBatis。

## 框架特性

FUST 框架组件的核心特性：

- **开箱即用**: 提供自动配置，减少开发者的配置工作
- **高性能**: 优化的实现确保应用程序具有高吞吐量和低延迟
- **可观测性**: 内置的监控和追踪能力，帮助开发者了解应用程序的运行状态
- **灵活扩展**: 通过SPI机制提供的扩展点，方便开发者自定义功能
- **最佳实践**: 组件的设计和实现基于大规模生产环境的实践经验

## 技术栈

FUST 框架使用了以下核心技术：

- **Armeria**: 高性能HTTP/2和gRPC服务器
- **OpenTelemetry**: 分布式追踪和指标收集
- **Log4j2**: 高性能日志框架
- **Apollo**: 分布式配置中心
- **Redis**: 分布式缓存
- **MyBatis**: 持久层框架

## 使用流程

1. 添加FUST组件依赖到您的项目中
2. 配置组件相关参数
3. 使用组件提供的API进行开发
4. 利用FUST提供的监控和追踪能力进行应用程序监控

## 获取帮助

- [GitHub 仓库](https://github.com/zhihu/fust)
- [问题反馈](https://github.com/zhihu/fust/issues)
- [贡献指南](https://github.com/zhihu/fust/blob/main/CONTRIBUTING.md)
