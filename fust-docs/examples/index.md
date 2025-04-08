# FUST 示例

本节提供了一系列示例，展示如何使用 FUST 框架开发各种类型的应用。通过这些示例，您可以快速掌握 FUST 的核心功能和最佳实践。

## 示例列表

- [Boot 示例](./boot.md) - FUST Boot 基础示例，展示如何使用 FUST 开发 Spring Boot 应用
- [gRPC 服务示例](./grpc.md) - 如何使用 FUST 开发高性能的 gRPC 服务
- [Web 应用示例](./web.md) - 如何使用 FUST 开发 Web 应用
- [自定义组件示例](./custom.md) - 如何扩展 FUST 框架，开发自定义组件

## 示例项目结构

所有示例都采用类似的项目结构：

```
example-project/
├── proto/                 # 协议定义（gRPC服务使用）
├── example-api/           # API模块，对外提供HTTP接口
├── example-grpc/          # gRPC模块，提供gRPC服务
├── example-business/      # 业务逻辑模块，包含核心业务代码
├── pom.xml                # 父项目POM文件
└── README.md              # 项目说明
```

## 运行示例

所有示例都可以通过以下方式运行：

1. 克隆 FUST 仓库

```bash
git clone https://github.com/zhihu/fust.git
```

2. 进入示例目录

```bash
cd fust/examples/fust-boot-example
```

3. 构建示例

```bash
./gradlew build
```

4. 运行示例

```bash
./run.sh
```

## 示例源码

您可以在 FUST 仓库的 `examples` 目录下找到所有示例的源码：

- [fust-boot-example](https://github.com/zhihu/fust/tree/main/examples/fust-boot-example)
- [fust-components-example](https://github.com/zhihu/fust/tree/main/examples/fust-components-example)
