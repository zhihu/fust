# gRPC服务开发

本章将介绍如何在FUST框架中开发gRPC服务，我们将以用户管理功能为例，通过gRPC方式实现User的CRUD操作。gRPC是一种高性能、跨语言的RPC框架，非常适合微服务之间的通信。

## gRPC概述

gRPC是Google开发的一种现代化的开源高性能RPC框架，它使用HTTP/2作为传输协议，使用Protocol Buffers作为接口描述语言和序列化格式。相比传统的REST API，gRPC具有以下优势：

1. 基于HTTP/2，支持双向流、流控制、头部压缩等特性
2. 使用Protocol Buffers进行IDL定义和序列化，更加高效
3. 强类型定义，支持多种语言的代码生成
4. 支持双向流式调用，更加灵活

在FUST框架中，我们通过`fust-boot-grpc`模块提供了对gRPC的支持，它基于Armeria框架实现，提供了更加灵活的配置和更好的性能。

## 项目结构

在开发gRPC服务时，我们推荐采用以下项目结构：

```
demo-yoda/
├── proto/                   # 存放proto文件
│   ├── buf.yaml             # buf配置文件
│   └── user/                # 按业务领域组织proto文件
│       └── user.proto       # 用户服务proto定义
├── buf.gen.yaml             # buf代码生成配置
├── demo-yoda-grpc/          # gRPC服务模块
│   ├── gen-src/             # 生成的proto代码
│   ├── src/
│   │   └── main/java/demo/yoda/grpc/
│   │       ├── handler/     # gRPC服务实现
│   │       └── GrpcMain.java  # 启动类
│   └── pom.xml              # gRPC模块依赖
└── demo-yoda-business/      # 业务逻辑模块
    └── ...
```

这种结构将proto文件与实现代码分离，便于跨语言使用，同时也方便管理和维护。

## 添加gRPC依赖

首先，在`demo-yoda-grpc`模块的`pom.xml`文件中添加FUST gRPC依赖：

```xml
<dependencies>
    <!-- FUST gRPC支持 -->
    <dependency>
        <groupId>com.zhihu.fust</groupId>
        <artifactId>fust-boot-grpc</artifactId>
    </dependency>
    
    <!-- 业务模块依赖 -->
    <dependency>
        <groupId>demo.yoda</groupId>
        <artifactId>demo-yoda-business</artifactId>
    </dependency>
    
    <!-- Telemetry支持 -->
    <dependency>
        <groupId>com.zhihu.fust</groupId>
        <artifactId>fust-telemetry-sdk</artifactId>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- Spring Boot Maven插件 -->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
        
        <!-- 添加生成的源代码目录 -->
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>build-helper-maven-plugin</artifactId>
            <version>3.2.0</version>
            <executions>
                <execution>
                    <id>add-source</id>
                    <phase>generate-sources</phase>
                    <goals>
                        <goal>add-source</goal>
                    </goals>
                    <configuration>
                        <sources>
                            <source>gen-src/protobuf/java</source>
                        </sources>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## 使用Buf管理Proto文件

在FUST框架中，我们推荐使用[Buf](https://buf.build/)工具来管理Protocol Buffers，它提供了更现代化的工作流程和更好的开发体验。

### 安装Buf

首先，我们需要安装Buf工具：

```bash
# macOS
brew install bufbuild/buf/buf

# Linux
curl -sSL "https://github.com/bufbuild/buf/releases/download/v1.28.1/buf-$(uname -s)-$(uname -m)" -o /usr/local/bin/buf
chmod +x /usr/local/bin/buf
```

### 配置Buf

在项目根目录创建`buf.gen.yaml`文件，配置代码生成规则：

```yaml
version: v1
plugins:
  # 生成Java消息类
  - plugin: buf.build/protocolbuffers/java:v25.2
    out: ./demo-yoda-grpc/gen-src/protobuf/java
  # 生成Java gRPC服务类
  - plugin: buf.build/grpc/java:v1.61.0
    out: ./demo-yoda-grpc/gen-src/protobuf/java
  # 如果需要生成Go代码
  - plugin: buf.build/protocolbuffers/go
    out: ./demo-yoda-grpc/gen-src/gen-go
  # 如果需要生成Go gRPC代码
  - plugin: buf.build/grpc/go:v1.3.0
    out: ./demo-yoda-grpc/gen-src/gen-go
  # 如果需要生成gRPC Gateway
  - plugin: buf.build/grpc-ecosystem/gateway:v2.19.0
    out: ./demo-yoda-grpc/gen-src/gen-go-gateway
```

在`proto`目录创建`buf.yaml`文件，配置依赖和lint规则：

```yaml
version: v1
deps:
  # 依赖Google API库
  - buf.build/googleapis/googleapis
  # 依赖gRPC Gateway库
  - buf.build/grpc-ecosystem/grpc-gateway
lint:
  use:
    - DEFAULT
  except:
    - PACKAGE_DIRECTORY_MATCH
    - PACKAGE_VERSION_SUFFIX
breaking:
  use:
    - FILE
```

### 定义Proto文件

在`proto/user`目录创建`user.proto`文件：

```protobuf
syntax = "proto3";

package demo.yoda.user;

// 引入Google API注解，支持gRPC Gateway
import "google/api/annotations.proto";
import "google/protobuf/empty.proto";
import "google/protobuf/timestamp.proto";

option go_package = "demo/yoda/user";
option java_multiple_files = true;
option java_outer_classname = "UserProto";
option java_package = "demo.yoda.proto.user";

// 用户服务定义
service UserService {
  // 创建用户
  rpc CreateUser (CreateUserRequest) returns (User) {
    option (google.api.http) = {
      post: "/api/v1/users"
      body: "*"
    };
  }
  
  // 获取用户
  rpc GetUser (GetUserRequest) returns (User) {
    option (google.api.http) = {
      get: "/api/v1/users/{id}"
    };
  }
  
  // 更新用户
  rpc UpdateUser (UpdateUserRequest) returns (User) {
    option (google.api.http) = {
      put: "/api/v1/users/{id}"
      body: "*"
    };
  }
  
  // 删除用户
  rpc DeleteUser (DeleteUserRequest) returns (DeleteUserResponse) {
    option (google.api.http) = {
      delete: "/api/v1/users/{id}"
    };
  }
  
  // 获取所有用户
  rpc ListUsers (google.protobuf.Empty) returns (ListUsersResponse) {
    option (google.api.http) = {
      get: "/api/v1/users"
    };
  }
}

// 用户消息定义
message User {
  int64 id = 1;
  string name = 2;
  string birthday = 3; // 格式：yyyy-MM-dd
  google.protobuf.Timestamp created_at = 4;
  google.protobuf.Timestamp updated_at = 5;
}

// 创建用户请求
message CreateUserRequest {
  string name = 1;
  string birthday = 2; // 格式：yyyy-MM-dd
}

// 获取用户请求
message GetUserRequest {
  int64 id = 1;
}

// 更新用户请求
message UpdateUserRequest {
  int64 id = 1;
  string name = 2;
  string birthday = 3; // 格式：yyyy-MM-dd
}

// 删除用户请求
message DeleteUserRequest {
  int64 id = 1;
}

// 删除用户响应
message DeleteUserResponse {
  bool success = 1;
}

// 用户列表响应
message ListUsersResponse {
  repeated User users = 1;
}
```

### 生成代码

使用Buf生成代码：

```bash
# 生成代码
buf generate
```

这个命令会根据`buf.gen.yaml`配置生成相应的代码文件。生成的Java代码会位于`demo-yoda-grpc/gen-src/protobuf/java`目录下。

## 实现gRPC服务

接下来，我们需要实现gRPC服务。首先，创建一个转换工具类，用于在Model和Proto对象之间进行转换：

在`demo-yoda-grpc`模块中创建`src/main/java/demo/yoda/grpc/util/UserProtoConverter.java`文件：

```java
package demo.yoda.grpc.util;

import com.google.protobuf.Timestamp;
import demo.yoda.business.model.UserModel;
import demo.yoda.proto.user.User;
import demo.yoda.proto.user.CreateUserRequest;
import demo.yoda.proto.user.UpdateUserRequest;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户Proto转换工具类
 */
public class UserProtoConverter {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 将UserModel转换为Proto的User
     */
    public static User toProto(UserModel model) {
        if (model == null) {
            return User.getDefaultInstance();
        }
        
        User.Builder builder = User.newBuilder()
                .setId(model.getId())
                .setName(model.getName())
                .setBirthday(model.getBirthday().format(DATE_FORMATTER));
        
        // 处理创建和更新时间
        if (model.getCreatedAt() != null) {
            Instant instant = model.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant();
            builder.setCreatedAt(Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build());
        }
        
        if (model.getUpdatedAt() != null) {
            Instant instant = model.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant();
            builder.setUpdatedAt(Timestamp.newBuilder()
                    .setSeconds(instant.getEpochSecond())
                    .setNanos(instant.getNano())
                    .build());
        }
        
        return builder.build();
    }
    
    /**
     * 将CreateUserRequest转换为UserModel
     */
    public static UserModel toModel(CreateUserRequest request) {
        if (request == null) {
            return null;
        }
        
        UserModel model = new UserModel();
        model.setName(request.getName());
        model.setBirthday(LocalDate.parse(request.getBirthday(), DATE_FORMATTER));
        return model;
    }
    
    /**
     * 将UpdateUserRequest转换为UserModel
     */
    public static UserModel toModel(UpdateUserRequest request) {
        if (request == null) {
            return null;
        }
        
        UserModel model = new UserModel();
        model.setId(request.getId());
        model.setName(request.getName());
        model.setBirthday(LocalDate.parse(request.getBirthday(), DATE_FORMATTER));
        return model;
    }
    
    /**
     * 将UserModel列表转换为Proto的User列表
     */
    public static List<User> toProtoList(List<UserModel> models) {
        if (models == null) {
            return List.of();
        }
        
        return models.stream()
                .map(UserProtoConverter::toProto)
                .collect(Collectors.toList());
    }
}
```

然后，实现gRPC服务接口。在`demo-yoda-grpc`模块中创建`src/main/java/demo/yoda/grpc/handler/UserServiceHandler.java`文件：

```java
package demo.yoda.grpc.handler;

import com.google.protobuf.Empty;
import demo.yoda.business.exception.UserNotFoundException;
import demo.yoda.business.model.UserModel;
import demo.yoda.business.service.UserService;
import demo.yoda.grpc.util.UserProtoConverter;
import demo.yoda.proto.user.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * 用户gRPC服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceHandler extends UserServiceGrpc.UserServiceImplBase {
    
    private final UserService userService;
    
    @Override
    public void createUser(CreateUserRequest request, StreamObserver<User> responseObserver) {
        try {
            log.info("Creating user: {}", request.getName());
            
            UserModel userModel = UserProtoConverter.toModel(request);
            boolean created = userService.createUser(userModel);
            
            if (created) {
                User response = UserProtoConverter.toProto(userModel);
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new StatusRuntimeException(Status.INTERNAL
                        .withDescription("Failed to create user")));
            }
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format: {}", request.getBirthday());
            responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT
                    .withDescription("Invalid date format. Expected yyyy-MM-dd but got: " + request.getBirthday())));
        } catch (Exception e) {
            log.error("Error creating user", e);
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())));
        }
    }
    
    @Override
    public void getUser(GetUserRequest request, StreamObserver<User> responseObserver) {
        try {
            long userId = request.getId();
            log.info("Getting user by ID: {}", userId);
            
            UserModel userModel = userService.getUserById(userId);
            User response = UserProtoConverter.toProto(userModel);
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (UserNotFoundException e) {
            log.warn("User not found: {}", request.getId());
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND
                    .withDescription(e.getMessage())));
        } catch (Exception e) {
            log.error("Error getting user", e);
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())));
        }
    }
    
    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<User> responseObserver) {
        try {
            long userId = request.getId();
            log.info("Updating user: {}", userId);
            
            // 确保用户存在
            userService.getUserById(userId);
            
            UserModel userModel = UserProtoConverter.toModel(request);
            boolean updated = userService.updateUser(userModel);
            
            if (updated) {
                User response = UserProtoConverter.toProto(userModel);
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(new StatusRuntimeException(Status.INTERNAL
                        .withDescription("Failed to update user")));
            }
        } catch (UserNotFoundException e) {
            log.warn("User not found: {}", request.getId());
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND
                    .withDescription(e.getMessage())));
        } catch (DateTimeParseException e) {
            log.warn("Invalid date format: {}", request.getBirthday());
            responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT
                    .withDescription("Invalid date format. Expected yyyy-MM-dd but got: " + request.getBirthday())));
        } catch (Exception e) {
            log.error("Error updating user", e);
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())));
        }
    }
    
    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<DeleteUserResponse> responseObserver) {
        try {
            long userId = request.getId();
            log.info("Deleting user: {}", userId);
            
            // 确保用户存在
            userService.getUserById(userId);
            
            boolean deleted = userService.deleteUser(userId);
            DeleteUserResponse response = DeleteUserResponse.newBuilder()
                    .setSuccess(deleted)
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (UserNotFoundException e) {
            log.warn("User not found: {}", request.getId());
            responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND
                    .withDescription(e.getMessage())));
        } catch (Exception e) {
            log.error("Error deleting user", e);
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())));
        }
    }
    
    @Override
    public void listUsers(Empty request, StreamObserver<ListUsersResponse> responseObserver) {
        try {
            log.info("Listing all users");
            
            List<UserModel> userModels = userService.getAllUsers();
            ListUsersResponse response = ListUsersResponse.newBuilder()
                    .addAllUsers(UserProtoConverter.toProtoList(userModels))
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error listing users", e);
            responseObserver.onError(new StatusRuntimeException(Status.INTERNAL
                    .withDescription("Internal error: " + e.getMessage())));
        }
    }
}
```

## 配置gRPC服务器

接下来，我们需要配置gRPC服务器。在FUST框架中，我们使用`fust-boot-grpc`提供的`GrpcServerBuilder`来配置和启动gRPC服务器。

在`demo-yoda-grpc`模块中创建`src/main/java/demo/yoda/grpc/GrpcServer.java`文件：

```java
package demo.yoda.grpc;

import com.zhihu.fust.armeria.grpc.server.GrpcServerBuilder;
import demo.yoda.grpc.handler.UserServiceHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GrpcServer {
    
    private final UserServiceHandler userServiceHandler;
    
    @Value("${grpc.server.port:9090}")
    private int grpcPort;
    
    /**
     * 启动gRPC服务
     */
    public void start() {
        GrpcServerBuilder builder = GrpcServerBuilder.builder(grpcPort);
        // 启用HTTP JSON转码，支持通过HTTP调用gRPC服务
        builder.enableHttpJsonTranscoding(true)
                .addService(userServiceHandler)
                .build()
                .start();
        
        log.info("gRPC server started on port: {}", grpcPort);
    }
}
```

## 创建启动类

最后，我们需要创建一个启动类来启动gRPC服务。在`demo-yoda-grpc`模块中创建`src/main/java/demo/yoda/grpc/GrpcMain.java`文件：

```java
package demo.yoda.grpc;

import com.zhihu.fust.telemetry.sdk.TelemetryInitializer;
import demo.yoda.business.BusinessConfiguration;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(BusinessConfiguration.class)
public class GrpcMain {
    
    public static void main(String[] args) {
        // 初始化遥测SDK
        TelemetryInitializer.init();
        
        // 创建Spring应用
        SpringApplication application = new SpringApplication(GrpcMain.class);
        // 关闭banner显示
        application.setBannerMode(Banner.Mode.OFF);
        // 设置为非Web应用
        application.setWebApplicationType(WebApplicationType.NONE);
        
        // 启动Spring上下文
        ConfigurableApplicationContext context = application.run(args);
        
        // 获取GrpcServer并启动
        GrpcServer server = context.getBean(GrpcServer.class);
        server.start();
    }
}
```

## 配置应用属性

在`demo-yoda-grpc`模块的`src/main/resources/application.properties`文件中配置应用属性：

```properties
# 应用名称
spring.application.name=demo-yoda-grpc

# gRPC服务器端口
grpc.server.port=9090

# 环境配置
env=dev
```

## 测试gRPC服务

在FUST框架中，我们提供了多种方式来测试gRPC服务，包括内置的文档和调试工具、命令行工具以及HTTP API调用。

### 使用内置文档和调试工具

FUST框架的gRPC服务默认集成了API文档和调试工具，你可以通过浏览器访问以下URL：

```
# gRPC服务文档页面
http://localhost:9090/_docs/

# 用户服务具体方法调试页面
http://localhost:9090/_docs/#/methods/demo.yoda.user.UserService/CreateUser/POST?debug_form_is_open=true
http://localhost:9090/_docs/#/methods/demo.yoda.user.UserService/GetUser/POST?debug_form_is_open=true
http://localhost:9090/_docs/#/methods/demo.yoda.user.UserService/UpdateUser/POST?debug_form_is_open=true
http://localhost:9090/_docs/#/methods/demo.yoda.user.UserService/DeleteUser/POST?debug_form_is_open=true
http://localhost:9090/_docs/#/methods/demo.yoda.user.UserService/ListUsers/POST?debug_form_is_open=true
```

在这些页面上，你可以：
1. 查看gRPC服务的所有方法和参数定义
2. 使用表单直接构建请求和发送请求
3. 查看请求和响应的详细信息
4. 查看服务器错误和异常信息

这是开发和测试gRPC服务最简便的方式，无需安装额外的工具。

### 使用命令行工具（grpcurl）

如果你喜欢命令行工具，可以使用grpcurl测试gRPC服务：

```bash
# 安装grpcurl（Mac用户）
brew install grpcurl

# 列出服务
grpcurl -plaintext localhost:9090 list

# 列出服务方法
grpcurl -plaintext localhost:9090 list demo.yoda.user.UserService

# 创建用户
grpcurl -plaintext -d '{"name": "张三", "birthday": "1990-01-01"}' \
  localhost:9090 demo.yoda.user.UserService/CreateUser

# 获取用户
grpcurl -plaintext -d '{"id": 1}' \
  localhost:9090 demo.yoda.user.UserService/GetUser

# 更新用户
grpcurl -plaintext -d '{"id": 1, "name": "张三(已更新)", "birthday": "1990-01-01"}' \
  localhost:9090 demo.yoda.user.UserService/UpdateUser

# 删除用户
grpcurl -plaintext -d '{"id": 1}' \
  localhost:9090 demo.yoda.user.UserService/DeleteUser

# 获取所有用户
grpcurl -plaintext localhost:9090 demo.yoda.user.UserService/ListUsers
```

### 使用HTTP API调用gRPC服务

由于我们启用了HTTP JSON转码功能，你可以使用普通的HTTP请求调用gRPC服务，这对于前端开发和测试非常方便：

#### 使用curl命令行工具

```bash
# 创建用户（REST风格API）
curl -X POST http://localhost:9090/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"name": "张三", "birthday": "1990-01-01"}'

# 获取用户（REST风格API）
curl http://localhost:9090/api/v1/users/1

# 更新用户（REST风格API）
curl -X PUT http://localhost:9090/api/v1/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name": "张三(已更新)", "birthday": "1990-01-01"}'

# 删除用户（REST风格API）
curl -X DELETE http://localhost:9090/api/v1/users/1

# 获取所有用户（REST风格API）
curl http://localhost:9090/api/v1/users
```

#### 直接调用gRPC方法

除了REST风格API外，你还可以直接通过HTTP调用gRPC方法：

```bash
# 创建用户（直接调用gRPC方法）
curl 'http://localhost:9090/demo.yoda.user.UserService/CreateUser' \
  -H 'content-type: application/json; charset=utf-8' \
  --data-raw '{"name":"张三","birthday":"1990-01-01"}'

# 获取用户（直接调用gRPC方法）
curl 'http://localhost:9090/demo.yoda.user.UserService/GetUser' \
  -H 'content-type: application/json; charset=utf-8' \
  --data-raw '{"id":1}'

# 更新用户（直接调用gRPC方法）
curl 'http://localhost:9090/demo.yoda.user.UserService/UpdateUser' \
  -H 'content-type: application/json; charset=utf-8' \
  --data-raw '{"id":1,"name":"张三(已更新)","birthday":"1990-01-01"}'

# 删除用户（直接调用gRPC方法）
curl 'http://localhost:9090/demo.yoda.user.UserService/DeleteUser' \
  -H 'content-type: application/json; charset=utf-8' \
  --data-raw '{"id":1}'

# 获取所有用户（直接调用gRPC方法）
curl 'http://localhost:9090/demo.yoda.user.UserService/ListUsers' \
  -H 'content-type: application/json; charset=utf-8' \
  --data-raw '{}'
```

这种方式对于没有gRPC客户端的环境特别有用，例如在浏览器中使用AJAX/fetch调用gRPC服务。

## 实现gRPC客户端

我们也可以使用FUST框架实现gRPC客户端。在FUST框架中，我们提供了`fust-boot-grpc-client`模块来简化gRPC客户端的开发。

创建`demo-yoda-client`模块，并在`pom.xml`中添加依赖：

```xml
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-boot-grpc-client</artifactId>
</dependency>

<!-- 引入生成的Proto代码 -->
<dependency>
    <groupId>demo.yoda</groupId>
    <artifactId>demo-yoda-grpc</artifactId>
    <classifier>proto</classifier>
</dependency>
```

然后，创建gRPC客户端配置：

```java
@Configuration
public class GrpcClientConfig {
    
    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userServiceStub(
            @Value("${grpc.client.userService.host:localhost}") String host,
            @Value("${grpc.client.userService.port:9090}") int port) {
        
        return GrpcClientBuilder.builder(host, port)
                .build()
                .createBlockingStub(UserServiceGrpc::newBlockingStub);
    }
}
```

最后，我们可以使用这个客户端调用gRPC服务：

```java
@Service
@RequiredArgsConstructor
public class UserClient {
    
    private final UserServiceGrpc.UserServiceBlockingStub userServiceStub;
    
    public User createUser(String name, String birthday) {
        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setName(name)
                .setBirthday(birthday)
                .build();
        return userServiceStub.createUser(request);
    }
    
    public User getUser(long id) {
        GetUserRequest request = GetUserRequest.newBuilder()
                .setId(id)
                .build();
        return userServiceStub.getUser(request);
    }
    
    public User updateUser(long id, String name, String birthday) {
        UpdateUserRequest request = UpdateUserRequest.newBuilder()
                .setId(id)
                .setName(name)
                .setBirthday(birthday)
                .build();
        return userServiceStub.updateUser(request);
    }
    
    public boolean deleteUser(long id) {
        DeleteUserRequest request = DeleteUserRequest.newBuilder()
                .setId(id)
                .build();
        DeleteUserResponse response = userServiceStub.deleteUser(request);
        return response.getSuccess();
    }
    
    public List<User> listUsers() {
        ListUsersResponse response = userServiceStub.listUsers(Empty.getDefaultInstance());
        return response.getUsersList();
    }
}
```

## 总结

在本章中，我们学习了如何在FUST框架中开发gRPC服务：

1. 使用Buf工具管理Protocol Buffers
2. 定义用户服务的Proto文件
3. 使用Buf生成Java代码
4. 实现gRPC服务处理器
5. 配置和启动gRPC服务器
6. 使用多种方式测试gRPC服务
7. 实现gRPC客户端

通过FUST框架，我们可以更加方便地开发和部署gRPC服务，为微服务之间提供高效的通信方式。同时，通过启用HTTP JSON转码，我们还可以在不同场景下灵活选择通信协议，既可以使用高性能的gRPC，也可以使用普遍支持的HTTP API。 