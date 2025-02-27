<p align="center">
    <strong>🛠️ An Enterprise-Level Microservices Development Framework Based on Spring Boot</strong>
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

## 📚Introduction

FUST (pronounced as "Fast") is a rapid development framework based on Spring Boot that provides:

* 🏗️ **Complete Microservices Architecture** - Built on Spring Boot 3.x, integrating core components like gRPC and Apollo Configuration Center
* 🔐 **Multi-Environment Management** - Supports deployment strategies for development, testing, staging, and production environments
* 🚀 **Canary Release** - Built-in canary release capabilities with support for percentage-based deployment and whitelist/blacklist
* 📊 **Observability** - Integrated with OpenTelemetry for comprehensive monitoring and tracing solutions
* 💾 **Data Access** - Supports Redis multi-instance and MySQL read-write separation features
* 📝 **Configuration Management** - Integrated with Apollo Configuration Center, supporting hot configuration updates
* 🎯 **Dynamic Logging** - Automated log configuration file generation with dynamic control through configuration center

## 🛠️Installation

### 📦 Maven

Add the following dependency to your project's pom.xml:

```xml
<dependency>
    <groupId>com.zhihu.fust</groupId>
    <artifactId>fust-boot-starter</artifactId>
    <version>${version}</version>
</dependency>
```

### 🐘 Gradle

```gradle
implementation 'com.zhihu.fust:fust-boot-starter:${version}'
```

## 📝Quick Start

1. Ensure your development environment meets the following requirements:
   - JDK 17 or higher
   - Gradle 8.x

2. Clone the project:

```bash
git clone https://github.com/zhihu/fust
```

3. Build the project:

```bash
./gradlew build
```

## 🎨Core Features

### 1. Environment Management

- Development Environment
- Integration Environment
- Testing Environment
- Staging Environment
- Production Environment

### 2. Configuration Management

- Apollo Configuration Center Integration
- Multi-Environment Configuration Support
- Hot Configuration Updates
- Local Cache Support

### 3. Service Communication

- gRPC Service Support
- HTTP JSON Transcoding
- Automatic API Documentation Generation
- Health Check

### 4. Data Access

- Redis Multi-Instance Management
- Database Connection Pool Configuration
- MyBatis Enhanced Features
- Read-Write Separation Support

### 5. Canary Release

- Percentage-based Canary Release
- Whitelist/Blacklist Support
- Dynamic Adjustment

### 6. Observability

- Log Management
- Distributed Tracing
- Metrics Collection
- Performance Monitoring

## 🧰Technology Stack

### Base Framework

- Spring Boot 3.4.1
- MyBatis 3.0.4
- Armeria 1.31.3 (RPC Framework)

### Storage

- Redis (Supports Lettuce/Jedis)
- MySQL

### Observability

- OpenTelemetry 1.46.0

## 📦Module Description

### fust-base (Base Module)

- fust-core: Core framework functionality with basic interface definitions and common implementations
- fust-provider: SPI provider interface, defining framework extension points
- fust-commons: Common utility library

### fust-components (Component Module)

- fust-config-apollo: Apollo configuration center integration
- fust-config-extension: Configuration extension features with hot update support
- fust-armeria-grpc: gRPC service framework integration
- fust-armeria-commons: Armeria common functionality library
- fust-logging-log4j2: Log4j2 logging framework integration
- fust-telemetry: OpenTelemetry observability integration

### fust-boot (Auto-Configuration Module)

- fust-boot-jdbc: Database connection pool auto-configuration with multi-datasource support
- fust-boot-lettuce: Redis Lettuce client auto-configuration
- fust-boot-jedis: Redis Jedis client auto-configuration
- fust-boot-grpc: gRPC service auto-configuration
- fust-boot-web: Web application auto-configuration
- fust-boot-log4j2: Logging auto-configuration
- fust-boot-config: Configuration center auto-configuration
- fust-boot-mybatis: MyBatis auto-configuration

### fust-spring (Spring Integration Module)

- fust-spring-web: Web development support with unified exception handling
- fust-spring-redis-common: Redis common functionality with multi-instance configuration
- fust-spring-mybatis: Enhanced MyBatis integration with dynamic SQL support
- fust-spring-jdbc: Enhanced JDBC-related functionality
- fust-spring-jedis: Jedis client integration
- fust-spring-lettuce: Lettuce client integration

### examples (Example Projects)

- fust-boot-example: Quick start examples
  - fust-boot-example-business: Business logic layer example
  - fust-boot-example-grpc: gRPC service example
  - fust-boot-example-api: Web API service example

## 🎯Maven Archetype Usage Guide

FUST provides a project scaffold to help you quickly create projects based on the FUST framework.

### Usage

#### 1. Command Line Creation

```bash
export ORIGIN_HOME=$JAVA_HOME && \
export JAVA_HOME="$(/usr/libexec/java_home -v 17)" && \
mvn archetype:generate -DarchetypeGroupId=com.zhihu.fust \
-DarchetypeArtifactId=fust-boot-archetype -DarchetypeVersion=1.0.0 \
-DinteractiveMode=false -DarchetypeCatalog=local -DgroupId=demo -DartifactId=demo-yoda && \
export JAVA_HOME=$ORIGIN_HOME && unset ORIGIN_HOME
```

#### 2. IDE Creation

In your IDE, select "Create New Project" -> "Maven" -> "Create from Archetype", then search for "fust-archetype".

### Project Structure

Projects created using the archetype will have the following structure:

```
demo-yoda
├── README.md
├── build.sh
├── demo-yoda-api
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── demo
│           │       └── api
│           │           ├── ApiMain.java
│           │           └── HelloController.java
│           └── resources
├── demo-yoda-business
│   ├── pom.xml
│   └── src
│       ├── main
│       │   └── java
│       │       └── demo
│       │           └── business
│       │               ├── ServiceConfiguration.java
│       │               ├── dao
│       │               ├── dto
│       │               ├── model
│       │               └── service
│       └── test
│           ├── java
│           │   └── test
│           │       └── service
│           │           ├── TestBeanConfig.java
│           │           ├── TestConfiguration.java
│           │           └── TestDao.java
│           └── resources
│               ├── application.properties
│               └── test.sql
├── demo-yoda-grpc
│   ├── pom.xml
│   └── src
│       └── main
│           ├── java
│           │   └── demo
│           │       └── grpc
│           │           ├── GrpcMain.java
│           │           ├── GrpcServer.java
│           │           └── HelloServiceHandler.java
│           └── resources
├── pom.xml
├── proto
│   ├── buf.yaml
│   └── hello.proto
└── run.sh
```

### 📐Development Guidelines

1. Follow modular development principles, maintaining reasonable layering between modules
2. Use proto files to define service interfaces
3. Make appropriate use of framework-provided functional components

## 🏗️Contributing

We welcome all forms of contributions, including:

- Submitting issues and feature requests
- Bug fixes
- Documentation improvements
- Feature enhancements

## 📄License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 