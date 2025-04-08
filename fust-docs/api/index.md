# FUST API 参考

本节提供了 FUST 框架的 API 参考文档，帮助开发者了解和使用框架提供的各种 API。

## API 模块

FUST 框架的 API 按模块组织：

- [核心 API](./core.md) - 核心接口和类，包括环境、配置和日志等基础功能
- [Boot API](./boot.md) - 自动配置相关 API，包括数据源、Redis 和 gRPC 等
- [组件 API](./components.md) - 各种组件的 API，如 Apollo 配置中心、gRPC 和日志等
- [Spring 集成 API](./spring.md) - Spring 相关 API，如 Web、Redis 和 MyBatis 等
- [可观测性 API](./telemetry.md) - 监控和追踪相关 API

## API 使用原则

在使用 FUST API 时，请遵循以下原则：

1. **优先使用高级 API**：FUST 提供了多层次的 API，通常应优先使用高级 API，除非有特殊需求
2. **避免直接依赖实现类**：尽量依赖接口而非实现类，以便于测试和扩展
3. **遵循 SPI 机制**：扩展 FUST 时，应使用 SPI（Service Provider Interface）机制

## 示例代码

每个 API 页面都包含示例代码，展示如何使用相应的 API。例如：

```java
// 使用 FUST 环境 API 获取当前环境
import com.zhihu.fust.core.env.Env;

public class EnvExample {
    public static void main(String[] args) {
        // 初始化环境
        Env.init();
        
        // 获取环境信息
        String envName = Env.getName();
        boolean isProd = Env.isProd();
        
        System.out.println("Current environment: " + envName);
        System.out.println("Is production: " + isProd);
    }
}
```

## API 版本

FUST 框架遵循语义化版本控制（Semantic Versioning），API 版本与框架版本保持一致。当前版本的 API 可能会在后续版本中发生变化，请关注版本更新日志。
