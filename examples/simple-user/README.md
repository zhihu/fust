## Simple User Service 示例项目

本项目展示了如何使用 FUST 框架开发 HTTP 和 gRPC 微服务。

### 功能特性

- 提供完整的用户管理服务
  - 支持用户的创建、查询、更新和删除（CRUD）操作
  - 支持批量创建和删除用户
  - 支持部分字段更新（Patch）操作
  - 集成 Redis 缓存，提高查询性能
  - 完整的异常处理机制

- 多协议支持
  - HTTP RESTful API 服务（端口：8080）
  - gRPC 服务（端口：9090）
  - 内置 gRPC 服务文档和调试工具

- 技术特点
  - 基于 FUST 框架开发
  - 采用分层架构设计（API/gRPC/Business）
  - 使用 MyBatis 进行数据访问
  - Redis 缓存集成
  - 完整的单元测试覆盖
  - 支持 Proto 文件自动生成代码



### 数据库准备

#### 1. 数据库配置

1. 创建数据库
```sql
CREATE DATABASE db1;
```

2. 创建用户表
```sql
CREATE TABLE `simple_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `birthday` DATE NOT NULL,
    `name` VARCHAR(64) NOT NULL,
    PRIMARY KEY (`id`)
);
```

#### 2. 配置数据库连接

修改 `simple-user-business/src/main/resources/db-dev.json` 文件：

```json
[
  {
    "name": "db1",
    "ds": [
      {
        "name": "db1-master",
        "url": "jdbc:mysql://127.0.0.1:3306/db1",
        "username": "your_username",
        "password": "your_password"
      }
    ]
  }
]
```

#### 3. Redis 配置

确保 Redis 服务已启动，默认配置如下：
- 主机：localhost
- 端口：6379
- 用户缓存过期时间：30分钟

### 环境准备

#### 安装 buf 工具

```shell
brew install bufbuild/buf/buf
```

#### 生成 Proto 代码

```shell
buf generate --template buf.gen.yaml
```

### 构建与运行

1. 构建服务
```shell
mvn package
bash build.sh
```

2. 运行服务
```shell
# 启动 HTTP 服务（默认端口 8080）
bash run.sh services/simple-user-api/

# 启动 gRPC 服务（默认端口 9090）
bash run.sh services/simple-user-grpc/
```

### 测试服务

#### HTTP 服务测试

1. 创建用户
```shell
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","birthday":"1990-01-01"}'
```

2. 查询用户
```shell
curl -X GET http://localhost:8080/api/users/1
```

3. 更新用户
```shell
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"张三(已更新)","birthday":"1990-01-01"}'
```

4. 删除用户
```shell
curl -X DELETE http://localhost:8080/api/users/1
```

#### gRPC 服务测试

##### 使用内置文档和调试工具

gRPC 服务提供了内置的文档和调试工具，可以通过浏览器访问：

1. 访问 gRPC 服务文档页面：
```
http://localhost:9090/_docs/
```

2. 用户服务调试页面：
```
# 创建用户
http://localhost:9090/_docs/#/methods/demo.yoda.user.UserService/CreateUser/POST?debug_form_is_open=true

# 获取用户
http://localhost:9090/_docs/#/methods/demo.yoda.user.UserService/GetUser/POST?debug_form_is_open=true

# 更新用户
http://localhost:9090/_docs/#/methods/demo.yoda.user.UserService/UpdateUser/POST?debug_form_is_open=true

# 删除用户
http://localhost:9090/_docs/#/methods/demo.yoda.user.UserService/DeleteUser/POST?debug_form_is_open=true
```

##### HTTP 访问 gRPC 服务 

本项目的 gRPC 服务基于 Armeria 框架实现，通过 `enableHttpJsonTranscoding` 特性支持 HTTP/JSON 方式访问 gRPC 服务。这种方式让客户端可以直接使用 HTTP 协议调用 gRPC 服务，无需配置 gRPC 客户端，极大地提升了服务的可访问性。

##### 接口访问规则

- 基础路径格式：`http://{host}:{port}/{package}.{service}/{method}`
- 请求格式：Content-Type: application/json
- 响应格式：application/json
- 示例：
  ```
  http://localhost:9090/simple.user.UserService/CreateUser
  ```

##### 接口调用示例

以下示例展示了如何使用 curl 命令访问用户服务的各个接口：
1. 创建用户
```bash
curl -X POST 'http://localhost:9090/simple.user.UserService/CreateUser' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "张三",
    "birthday": "1990-01-01"
  }'
```

2. 查询用户
```bash
curl -X POST 'http://localhost:9090/simple.user.UserService/GetUser' \
  -H 'Content-Type: application/json' \
  -d '{
    "id": 1
  }'
```

3. 更新用户
```bash
curl -X POST 'http://localhost:9090/simple.user.UserService/UpdateUser' \
  -H 'Content-Type: application/json' \
  -d '{
    "id": 1,
    "name": "张三(已更新)",
    "birthday": "1990-01-01"
  }'
```

4. 删除用户
```bash
curl -X POST 'http://localhost:9090/simple.user.UserService/DeleteUser' \
  -H 'Content-Type: application/json' \
  -d '{
    "id": 1
  }'
```

##### 注意事项

- 所有请求都使用 POST 方法，这是 Armeria 的 gRPC 转码规范要求
- 请求体必须是合法的 JSON 格式
- 日期类型字段使用 ISO 格式（YYYY-MM-DD）
- 确保设置正确的 Content-Type 头部

### 项目结构

```
simple-user/
├── proto/                   # Proto 文件目录
├── simple-user-api/         # HTTP 服务模块
├── simple-user-grpc/        # gRPC 服务模块
├── simple-user-business/    # 业务逻辑模块
├── buf.gen.yaml             # buf 代码生成配置
└── pom.xml                  # 项目依赖管理
```
### 分层构建介绍

本项目采用 Spring 分层构建（Layered Jars）技术，通过 `build.sh` 和 `run.sh` 脚本实现。这种构建方式可以显著减少最终容器大小，提高微服务部署效率。

#### 分层结构

分层结构如下:

- lib/ - 通用依赖层
  - 包含所有第三方依赖 JAR 包
  - 包含 Spring Boot、Spring Framework 等基础框架
  - 这些依赖不易变更,可以在多个版本间共享
  
- mod_lib/ - 模块依赖层  
  - 包含业务模块相关的 JAR 包
  - 包含本地模块依赖
  - 这些依赖随应用更新而变化
  
- services/ - 应用层
  - 每个微服务一个独立目录
  - 包含应用特定的配置和资源
  - 通过软链接引用 lib/ 和 mod_lib/ 中的依赖
  - 不包含实际的 JAR 文件,减少磁盘占用

这种分层结构的优势:

1. 依赖复用 - 相同的依赖只需要存储一份
2. 快速部署 - 只需要更新变化的层
3. 节省空间 - 通过软链接避免重复存储
4. 清晰管理 - 依赖按照稳定性分层存储
