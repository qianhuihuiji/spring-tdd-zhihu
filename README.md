# Spring TDD 知乎 startup 项目

> 使用 TDD（测试驱动开发）方式构建的仿知乎论坛项目的起步工程

## 项目简介

本工程为使用 **TDD（Test-Driven Development）** 的方式构建一个仿知乎的论坛项目的起步工程。项目基于 Spring Boot 3.0.6，采用 Java 17 作为开发语言，遵循约定大于配置的原则，已集成开发所需的核心依赖和公共组件。

配套教程：[build-zhihu-with-springboot-and-tdd-manual](https://github.com/qianhuihuiji/build-zhihu-with-springboot-and-tdd-manual)

## 技术栈

| 技术 | 版本/说明 |
|------|-----------|
| **框架** | Spring Boot 3.0.6 |
| **语言** | Java 17 |
| **ORM** | MyBatis + MyBatis Generator |
| **数据库** | MySQL 8.0 |
| **连接池** | Druid 1.2.22 |
| **安全认证** | Spring Security + JWT (jjwt 0.11.5) |
| **数据库版本控制** | Flyway |
| **测试** | JUnit, Testcontainers, Spring Boot Test |
| **工具** | Lombok, PageHelper 分页插件 |

## 项目结构

```
spring-tdd-zhihu-startup/
├── src/main/
│   ├── java/com/nofirst/spring/tdd/zhihu/startup/
│   │   ├── common/              # 通用响应封装
│   │   │   ├── CommonResult.java
│   │   │   ├── IErrorCode.java
│   │   │   └── ResultCode.java
│   │   ├── config/              # 配置类
│   │   │   ├── SecurityConfig.java
│   │   │   └── MyBatisConfig.java
│   │   ├── controller/          # 控制器层
│   │   │   └── UserController.java
│   │   ├── exception/           # 异常处理
│   │   │   ├── ApiException.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── mbg/                 # MyBatis Generator 相关
│   │   │   ├── mapper/          # Mapper 接口
│   │   │   ├── model/           # 实体模型
│   │   │   ├── CommentGenerator.java
│   │   │   └── Generator.java
│   │   ├── model/dto/           # 数据传输对象
│   │   │   ├── UserLoginDto.java
│   │   │   └── UserRegisterDto.java
│   │   ├── security/            # 安全认证相关
│   │   │   ├── AccountUser.java
│   │   │   ├── CustomUserDetailsService.java
│   │   │   ├── JwtAccessDeniedHandler.java
│   │   │   ├── JwtAuthenticationEntryPoint.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── JwtUtil.java
│   │   └── SpringTddZhihuStartupApplication.java
│   └── resources/
│       ├── db/migration/        # Flyway 数据库迁移脚本
│       │   ├── V2026010401__create_table_user.sql
│       │   └── V2026010402__init_user_data.sql
│       ├── mapper/              # MyBatis XML 映射文件
│       ├── application.yaml     # 应用配置
│       ├── generator.properties # MyBatis Generator 配置
│       └── generatorConfig.xml  # MyBatis Generator 配置
└── src/test/
    └── java/com/nofirst/spring/tdd/zhihu/startup/
        ├── factory/             # 测试工厂
        │   └── UserFactory.java
        └── intergration/        # 集成测试
            └── BaseContainerTest.java
```

## 核心功能模块

### 1. 用户认证系统
- **JWT Token 认证**：基于 JWT 的无状态认证机制
- **注册接口**：`POST /auth/register` - 用户注册（密码自动 BCrypt 加密）
- **登录接口**：`POST /auth/login` - 用户登录，返回 JWT Token
- **登出接口**：`GET /auth/logout` - 退出登录（前端需配合删除本地 Token）
- **密码加密**：使用 BCrypt 进行密码加密存储（不可逆）

### 2. 安全配置
- Spring Security 配置
- JWT 过滤器
- 认证入口和访问拒绝处理器
- 接口权限控制

### 3. 数据库管理
- **Flyway 版本控制**：数据库结构版本化管理
- **初始表结构**：`user` 表（包含 id, name, phone, email, password, created_at, updated_at）
- **初始化数据**：预置 3 条测试用户数据（密码已 BCrypt 加密，默认密码为 `password`）

### 4. MyBatis 代码生成
- 配置了 MyBatis Generator
- 支持自动生成 Mapper、Model 和 XML 映射文件

### 5. 测试支持
- **Testcontainers**：使用 Docker 容器进行集成测试
- **BaseContainerTest**：集成测试基类，自动启动 MySQL 容器

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Docker（用于运行测试）

### 配置说明

#### 1. 数据库配置
修改 `src/main/resources/application.yaml` 中的数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zhihu?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

#### 2. Flyway 配置
Flyway 已配置在 `pom.xml` 的 Maven 插件中，默认连接信息：
- URL: `jdbc:mysql://127.0.0.1:3306/zhihu`
- 用户名：`root`
- 密码：`root`

### 运行步骤

#### 1. 初始化数据库
```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE zhihu CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 运行 Flyway 迁移
mvn flyway:migrate
```

#### 2. 生成 MyBatis 代码（可选）
如需生成新的表结构映射，修改 `generatorConfig.xml` 中的表名配置，然后运行：
```bash
mvn mybatis-generator:generate
```

#### 3. 运行应用
```bash
mvn spring-boot:run
```

#### 4. 运行测试
```bash
mvn test
```

### API 示例

#### 用户注册
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "newuser",
    "phone": "13800138000",
    "email": "newuser@qq.com",
    "password": "password123"
  }'
```

响应：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

#### 用户登录
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "Jane", "password": "password"}'
```

响应：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": "eyJhbGciOiJIUzI1NiJ9.xxx"
}
```

#### 用户登出
```bash
curl -X GET http://localhost:8080/auth/logout
```

响应：
```json
{
  "code": 200,
  "message": "退出登录成功，请前端删除本地存储的 Token",
  "data": null
}
```

**测试用户**：初始化数据包含 3 个测试用户（Jane、John、Foo），登录密码均为 `password`

## 开发指南

### 添加新表
1. 在 `src/main/resources/db/migration/` 下创建新的 Flyway 迁移脚本（命名格式：`V 版本号__描述.sql`）
2. 修改 `generatorConfig.xml`，添加新表名
3. 运行 `mvn mybatis-generator:generate` 生成代码

### 编写测试
继承 `BaseContainerTest` 类即可使用 Testcontainers 提供的 MySQL 容器：

```java
class YourServiceTest extends BaseContainerTest {
    @Test
    void testSomething() {
        // 测试逻辑
    }
}
```

## 统一响应格式

所有 API 返回统一使用 `CommonResult<T>` 封装：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

### 常用状态码
| 状态码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数验证失败 |
| 401 | 未登录/Token 无效 |
| 403 | 未授权 |
| 500 | 服务器错误 |

## License

MIT License
