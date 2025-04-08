# 快速开始

本指南将帮助你快速上手FUST框架，包括环境准备、项目创建、工程结构说明以及如何启动服务。

## 环境准备

在开始使用FUST框架前，你需要确保已经安装了以下工具：

1. JDK 17或以上版本
2. Maven 3.8+
3. Gradle 8.3+（用于构建FUST框架）
4. Buf（用于生成Protocol Buffers代码）

### 发布FUST到本地仓库

目前FUST框架尚未发布到Maven中央仓库，需要先将其发布到本地Maven仓库才能使用：

```bash
# 克隆FUST仓库
git clone https://github.com/zhihu/fust.git
cd fust

# 发布到本地Maven仓库
./gradlew publishToMavenLocal
```

## 创建项目

FUST提供了Maven Archetype用于快速创建项目骨架。通过以下命令创建一个名为`demo-yoda`的项目：

```bash
export ORIGIN_HOME=$JAVA_HOME && \
export JAVA_HOME="$(/usr/libexec/java_home -v 17)" && \
mvn archetype:generate -DarchetypeGroupId=com.zhihu.fust \
-DarchetypeArtifactId=fust-boot-archetype -DarchetypeVersion=0.1.0 \
-DinteractiveMode=false -DarchetypeCatalog=local -DgroupId=demo -DartifactId=demo-yoda && \
export JAVA_HOME=$ORIGIN_HOME && unset ORIGIN_HOME
```

> 注意：上述命令中设置了Java 17环境，并在完成后恢复原始环境变量。如果你已经默认使用Java 17，可以简化此命令。

## 工程结构说明

创建完成后，`demo-yoda`项目的目录结构如下：

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

### 脚本说明

- **build.sh**: 构建脚本，用于编译项目并处理依赖关系，分离出不同层次的依赖，便于Docker镜像分层。
- **run.sh**: 运行脚本，用于启动服务，自动处理内存配置和Java运行参数。

## 项目构建

首先，生成Protocol Buffers代码：

```bash
# 安装Buf（如果尚未安装）
brew install bufbuild/buf/buf

# 更新Buf模块依赖
cd proto
buf mod update
cd ..

# 生成Protocol Buffers代码
buf generate --template buf.gen.yaml
```

然后，构建项目：

```bash
# 编译Maven项目
mvn clean package

# 运行构建脚本
bash build.sh
```

## 启动HTTP服务

FUST项目中，HTTP服务由`demo-yoda-api`模块提供，它使用传统的Spring MVC框架实现REST接口。启动服务的方法如下：

```bash
# 启动demo-yoda-api服务
bash run.sh services/demo-yoda-api
```

服务启动后，可以通过以下URL访问HTTP接口：

- API基础路径: http://127.0.0.1:8080/v1/hello

### 测试示例接口

你可以使用curl工具测试Hello服务：

```bash
curl -X GET "http://localhost:8080/v1/hello?name=FUST"
```

期望的响应如下：

```json
{"data":"Hello, FUST!"}
```

## 启动gRPC服务

如果需要启动gRPC服务，可以使用以下命令：

```bash
# 启动demo-yoda-grpc服务
bash run.sh services/demo-yoda-grpc
```
服务启动后，在开发环境与测试环境，可以通过以下URL查看gRPC协议以及调试gRPC

- 文档路径: http://127.0.0.1:8888/_docs
- 调试 HEELO 接口：http://localhost:8888/_docs/#/methods/hello.HelloService/Hello/POST?debug_form_is_open=true

### 测试接口

在开发环境与测试环境，也可以使用curl工具测试Hello服务

```bash
curl 'http://localhost:8888/hello.HelloService/Hello' \
  -H 'content-type: application/json; charset=utf-8' \
  --data-raw $'{"name":"FUST"}'
  
```

期望的响应如下：
```json
{"message":"Hello, FUST!","now":"1970-01-01T00:00:16Z"}%
```

## 下一步

- 查看[架构概述](./architecture.md)了解FUST的设计理念
- 阅读[组件文档](../components/)了解各个组件的使用方法
- 参考[示例代码](../examples/)了解更多使用场景