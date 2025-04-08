# HTTP服务开发

本章将介绍如何在FUST框架中开发HTTP REST服务，将我们之前实现的用户管理功能通过REST API暴露给外部系统或前端应用。我们将学习如何定义DTO（数据传输对象）、实现控制器以及处理常见的Web开发问题。

## HTTP服务概述

HTTP服务是微服务架构中最常见的对外接口方式，FUST框架基于Spring MVC提供了强大的HTTP服务支持。在开发HTTP服务时，我们通常遵循以下步骤：

1. 定义DTO对象，用于API层的数据交换
2. 实现Controller，处理HTTP请求
3. 配置序列化/反序列化，确保数据格式正确
4. 实现统一的异常处理
5. 添加API文档支持

## 添加Web依赖

首先，在`demo-yoda-api`模块的`pom.xml`文件中添加FUST Web依赖：

```xml
<!-- FUST Web支持 -->
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-boot-web</artifactId>
</dependency>

<!-- Swagger API文档 -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.1.0</version>
</dependency>
```

`fust-boot-web`依赖会自动引入Spring Web MVC和相关组件，简化Web应用的开发。

## 定义DTO对象

DTO（数据传输对象）用于API层的数据交换，它与领域模型（Model）分离，有助于隐藏内部实现细节，并优化API传输的数据结构。

在`demo-yoda-api`模块中创建`src/main/java/demo/yoda/api/dto/UserDto.java`文件：

```java
package demo.yoda.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "用户DTO")
public class UserDto {
    
    @Schema(description = "用户ID", example = "1")
    private Long id;
    
    @Schema(description = "用户名称", example = "张三")
    private String name;
    
    @Schema(description = "用户生日", example = "1990-01-01")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    
    // 不包含createdAt和updatedAt字段，这些是内部字段，不对外暴露
}
```

这个DTO类使用了以下注解：

- `@Data`：Lombok注解，自动生成getter、setter等方法
- `@Schema`：Swagger注解，用于API文档生成
- `@JsonFormat`：Jackson注解，指定日期格式

## 添加转换工具类

为了简化Model和DTO之间的转换，我们创建一个转换工具类。

在`demo-yoda-api`模块中创建`src/main/java/demo/yoda/api/util/UserConverter.java`文件：

```java
package demo.yoda.api.util;

import demo.yoda.api.dto.UserDto;
import demo.yoda.business.model.UserModel;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户模型转换工具类
 */
public class UserConverter {
    
    /**
     * 将UserModel转换为UserDto
     */
    public static UserDto toDto(UserModel model) {
        if (model == null) {
            return null;
        }
        
        UserDto dto = new UserDto();
        BeanUtils.copyProperties(model, dto);
        return dto;
    }
    
    /**
     * 将UserDto转换为UserModel
     */
    public static UserModel toModel(UserDto dto) {
        if (dto == null) {
            return null;
        }
        
        UserModel model = new UserModel();
        BeanUtils.copyProperties(dto, model);
        return model;
    }
    
    /**
     * 将UserModel列表转换为UserDto列表
     */
    public static List<UserDto> toDtoList(List<UserModel> models) {
        if (models == null) {
            return List.of();
        }
        
        return models.stream()
                .map(UserConverter::toDto)
                .collect(Collectors.toList());
    }
}
```

## 配置Jackson日期转换

为了确保日期类型（如LocalDate、LocalDateTime）能够正确序列化和反序列化，我们需要配置Jackson。

在`demo-yoda-api`模块中创建`src/main/java/demo/yoda/api/config/WebConfig.java`文件：

```java
package demo.yoda.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
    
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        return converter;
    }
    
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(mappingJackson2HttpMessageConverter(objectMapper()));
    }
}
```

这个配置类完成了以下工作：

1. 配置ObjectMapper，注册JavaTimeModule以支持Java 8日期时间类型
2. 禁用将日期写为时间戳的默认行为
3. 创建和配置MappingJackson2HttpMessageConverter
4. 将自定义的消息转换器添加到Spring MVC配置中

## 定义统一响应结构

为了提供一致的API响应格式，我们定义统一的响应结构。

在`demo-yoda-api`模块中创建`src/main/java/demo/yoda/api/response/ApiResponse.java`文件：

```java
package demo.yoda.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "API响应结构")
public class ApiResponse<T> {
    
    @Schema(description = "响应码", example = "200")
    private int code;
    
    @Schema(description = "响应消息", example = "操作成功")
    private String message;
    
    @Schema(description = "响应数据")
    private T data;
    
    /**
     * 创建成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("操作成功");
        response.setData(data);
        return response;
    }
    
    /**
     * 创建成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }
    
    /**
     * 创建失败响应
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}
```

## 实现用户控制器

现在，我们实现用户控制器，提供用户CRUD操作的REST API。

在`demo-yoda-api`模块中创建`src/main/java/demo/yoda/api/controller/UserController.java`文件：

```java
package demo.yoda.api.controller;

import demo.yoda.api.dto.UserDto;
import demo.yoda.api.response.ApiResponse;
import demo.yoda.api.util.UserConverter;
import demo.yoda.business.exception.UserNotFoundException;
import demo.yoda.business.model.UserModel;
import demo.yoda.business.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "用户管理", description = "用户CRUD操作")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询用户")
    public ApiResponse<UserDto> getUserById(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        try {
            UserModel user = userService.getUserById(id);
            return ApiResponse.success(UserConverter.toDto(user));
        } catch (UserNotFoundException e) {
            log.warn("User not found: {}", id);
            return ApiResponse.error(404, e.getMessage());
        }
    }
    
    @GetMapping
    @Operation(summary = "查询所有用户")
    public ApiResponse<List<UserDto>> getAllUsers() {
        List<UserModel> users = userService.getAllUsers();
        return ApiResponse.success(UserConverter.toDtoList(users));
    }
    
    @GetMapping("/name/{name}")
    @Operation(summary = "根据名称查询用户")
    public ApiResponse<UserDto> getUserByName(
            @Parameter(description = "用户名称", required = true) @PathVariable String name) {
        Optional<UserModel> user = userService.getUserByName(name);
        return user.map(u -> ApiResponse.success(UserConverter.toDto(u)))
                .orElse(ApiResponse.error(404, "User not found with name: " + name));
    }
    
    @GetMapping("/birthday/{birthday}")
    @Operation(summary = "根据生日查询用户")
    public ApiResponse<List<UserDto>> getUsersByBirthday(
            @Parameter(description = "生日（格式：yyyy-MM-dd）", required = true) @PathVariable String birthday) {
        List<UserModel> users = userService.getUsersByBirthday(LocalDate.parse(birthday));
        return ApiResponse.success(UserConverter.toDtoList(users));
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "创建用户")
    public ApiResponse<UserDto> createUser(
            @Parameter(description = "用户信息", required = true) @RequestBody UserDto userDto) {
        UserModel userModel = UserConverter.toModel(userDto);
        boolean created = userService.createUser(userModel);
        if (created) {
            userDto.setId(userModel.getId());
            return ApiResponse.success(userDto);
        } else {
            return ApiResponse.error(500, "Failed to create user");
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public ApiResponse<UserDto> updateUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "用户信息", required = true) @RequestBody UserDto userDto) {
        userDto.setId(id);
        UserModel userModel = UserConverter.toModel(userDto);
        
        try {
            // 确保用户存在
            userService.getUserById(id);
            
            boolean updated = userService.updateUser(userModel);
            if (updated) {
                return ApiResponse.success(userDto);
            } else {
                return ApiResponse.error(500, "Failed to update user");
            }
        } catch (UserNotFoundException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
    
    @PatchMapping("/{id}")
    @Operation(summary = "部分更新用户")
    public ApiResponse<UserDto> patchUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id,
            @Parameter(description = "用户部分信息", required = true) @RequestBody UserDto userDto) {
        userDto.setId(id);
        UserModel userModel = UserConverter.toModel(userDto);
        
        try {
            // 确保用户存在
            UserModel existingUser = userService.getUserById(id);
            
            boolean patched = userService.patchUser(userModel);
            if (patched) {
                // 重新查询最新数据
                UserModel updatedUser = userService.getUserById(id);
                return ApiResponse.success(UserConverter.toDto(updatedUser));
            } else {
                return ApiResponse.error(500, "Failed to patch user");
            }
        } catch (UserNotFoundException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public ApiResponse<Void> deleteUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        try {
            // 确保用户存在
            userService.getUserById(id);
            
            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                return ApiResponse.success();
            } else {
                return ApiResponse.error(500, "Failed to delete user");
            }
        } catch (UserNotFoundException e) {
            return ApiResponse.error(404, e.getMessage());
        }
    }
    
    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "批量创建用户")
    public ApiResponse<List<UserDto>> batchCreateUsers(
            @Parameter(description = "用户列表", required = true) @RequestBody List<UserDto> userDtos) {
        List<UserModel> userModels = userDtos.stream()
                .map(UserConverter::toModel)
                .toList();
        
        boolean created = userService.batchCreateUsers(userModels);
        if (created) {
            return ApiResponse.success(UserConverter.toDtoList(userModels));
        } else {
            return ApiResponse.error(500, "Failed to batch create users");
        }
    }
    
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除用户")
    public ApiResponse<Integer> batchDeleteUsers(
            @Parameter(description = "用户ID列表", required = true) @RequestBody List<Long> ids) {
        int deleted = userService.batchDeleteUsers(ids);
        return ApiResponse.success(deleted);
    }
}
```

控制器实现了以下功能：

1. CRUD基本操作：查询、创建、更新、删除
2. 批量操作：批量创建、批量删除
3. 按条件查询：按名称查询、按生日查询

每个API端点都添加了Swagger注解，便于生成API文档。

## 实现全局异常处理

为了统一处理API层的异常，我们实现一个全局异常处理器。

在`demo-yoda-api`模块中创建`src/main/java/demo/yoda/api/exception/GlobalExceptionHandler.java`文件：

```java
package demo.yoda.api.exception;

import demo.yoda.api.response.ApiResponse;
import demo.yoda.business.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleUserNotFoundException(UserNotFoundException e) {
        log.warn("User not found: {}", e.getMessage());
        return ApiResponse.error(404, e.getMessage());
    }
    
    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        DateTimeParseException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBadRequestException(Exception e) {
        log.warn("Bad request: {}", e.getMessage());
        return ApiResponse.error(400, "Invalid request format: " + e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGlobalException(Exception e) {
        log.error("Unexpected error", e);
        return ApiResponse.error(500, "An unexpected error occurred: " + e.getMessage());
    }
}
```

## 配置OpenAPI文档

为了提供API文档，我们配置OpenAPI（Swagger）。

在`demo-yoda-api`模块中创建`src/main/java/demo/yoda/api/config/OpenApiConfig.java`文件：

```java
package demo.yoda.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI demoYodaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Demo Yoda API")
                        .description("Demo Yoda用户管理API")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("FUST Team")
                                .url("https://github.com/zhihu/fust")
                                .email("fust@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
```

## 配置CORS

为了支持前端跨域访问，我们配置CORS（跨源资源共享）。

修改`WebConfig`类，添加CORS配置：

```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .maxAge(3600);
}
```

## 服务层扩展 - getAllUsers 方法

在之前的章节中，我们没有实现`getAllUsers`方法，这里需要在`UserService`接口和实现类中添加该方法：

```java
// 在UserService接口中添加
List<UserModel> getAllUsers();

// 在UserServiceImpl类中实现
@Override
public List<UserModel> getAllUsers() {
    log.info("Getting all users");
    return userDao.findAll();
}
```

同时，需要在`UserDao`接口中添加`findAll`方法：

```java
@Select("SELECT * FROM " + TABLE_NAME)
@ResultMap("UserModel")
List<UserModel> findAll();
```

## 修改UserService.java增加findByCache方法

为了支持从缓存中查询用户，我们需要在`UserService`接口中添加`findByCache`方法，并在接口中声明该方法：

```java
/**
 * 从缓存中获取用户，如果不存在则从数据库加载并缓存
 * 
 * @param userId 用户ID
 * @return 用户信息
 */
Optional<UserModel> findByCache(long userId);
```

## 测试HTTP服务

最后，我们可以使用Postman或curl等工具测试我们的HTTP API。

以下是一些常见的API测试命令：

### 创建用户

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","birthday":"1990-01-01"}'
```

### 查询用户

```bash
curl -X GET http://localhost:8080/api/users/1
```

### 更新用户

```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"张三(已更新)","birthday":"1990-01-01"}'
```

### 删除用户

```bash
curl -X DELETE http://localhost:8080/api/users/1
```

## 总结

在本章中，我们学习了如何在FUST框架中开发HTTP REST服务：

1. 定义DTO对象，用于API层的数据交换
2. 配置Jackson日期转换，确保日期格式正确
3. 实现用户控制器，提供CRUD操作的REST API
4. 添加全局异常处理，统一处理API异常
5. 配置OpenAPI文档，提供API文档
6. 配置CORS，支持跨域访问

通过完成上述步骤，我们成功地将用户管理功能通过REST API暴露给外部应用。在下一章中，我们将学习如何开发gRPC服务，为系统间通信提供高性能的RPC接口。 