# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 构建与运行命令

```bash
# 运行所有测试（排除 @Tag("online") 的测试）
mvn test -DexcludedGroups=online

# 运行指定测试类
mvn test -Dtest=UserRegisterTest -DexcludedGroups=online

# 运行指定测试方法
mvn test -Dtest=UserRegisterTest#guests_can_register_with_valid_credentials -DexcludedGroups=online

# 运行全部测试（含 online 分组）
mvn test

# 运行应用
mvn spring-boot:run

# Flyway 数据库迁移
mvn flyway:migrate

# MyBatis 代码生成（需先修改 generatorConfig.xml 中的表名）
mvn mybatis-generator:generate
```

测试脚本 `scripts/run-test.cmd`（Windows）和 `scripts/run-test.sh`（Linux/Mac）对 Maven 命令做了封装，支持按类名和方法名过滤。

## 技术栈

Spring Boot 3.0.6 / Java 17 / MyBatis + MyBatis Generator / MySQL 8.0 / Druid 连接池 / Flyway 数据库迁移 / Spring Security + JWT (jjwt 0.11.5) / Kafka / Redis / 邮件 (spring-boot-starter-mail)

## 项目架构

```
controller/     → HTTP 层，处理请求/响应，调用 service
service/        → 业务逻辑层
policy/         → 业务策略/权限判断（如能否标记最佳答案）
mbg/mapper/     → MyBatis Generator 生成的数据访问层
mbg/model/      → 数据实体和 Example 查询类
model/dto/      → 请求 DTO（UserRegisterDto, QuestionDto 等）
model/vo/       → 响应 VO（QuestionVo, AnswerVo 等）
security/       → JWT 过滤器、认证入口、UserDetailsService
config/         → SecurityConfig, WebConfig, SpringDocConfig 等
event/          → Spring 事件类
listener/       → Spring 事件监听器
publisher/      → 事件发布工具
queue/          → Kafka 消费者/生产者
common/         → CommonResult<T> 统一响应、ResultCode 状态码
exception/      → 全局异常处理 GlobalExceptionHandler
```

- **认证流程**: `UserController`（`/auth/register`, `/auth/login`, `/auth/logout`）→ Spring Security `AuthenticationManager` → `JwtAuthenticationFilter` 拦截所有请求解析 JWT → `CustomUserDetailsService` 加载用户
- **安全配置**: `SecurityConfig` 关闭 CSRF、无状态会话，除了 `/auth/**` 和 `GET /questions` 外全部需要认证
- **统一响应**: 所有 API 返回 `CommonResult<T>`（code/message/data）
- **依赖注入**: 使用 Lombok `@AllArgsConstructor` 实现构造器注入

## 数据库与 Flyway

迁移脚本位于 `src/main/resources/db/migration/`，命名格式 `VyyyyMMddNN__description.sql`。添加新表后运行 `mvn flyway:migrate`，然后修改 `generatorConfig.xml` 并运行 `mvn mybatis-generator:generate` 生成对应的 Mapper/Model/XML。

## 测试规范（TDD 流程）

### 测试目录结构

```
src/test/java/com/nofirst/spring/tdd/zhihu/
├── integration/        # 集成测试，继承 BaseContainerTest
├── unit/               # 单元测试（service/policy/util）
├── factory/            # 测试数据工厂类
├── matcher/            # 自定义断言匹配器
└── listener/           # 监听器测试
```

### BaseContainerTest

集成测试基类，自动启动 Testcontainers 容器（MySQL 8.0、Kafka、Redis），并用 `@DynamicPropertySource` 动态覆盖连接配置。提供 `cleanUp*()` 方法用于 `@BeforeEach` 中清理测试数据。测试类中通过 `@Autowired MockMvc mockMvc` 发起 HTTP 请求。

### 测试命名与方法结构

- **类名**: `*Test`（如 `CreateQuestionsTest`、`QuestionPolicyTest`）
- **方法名**: 小写下划线分隔的完整场景描述（如 `guests_may_not_create_questions`、`an_authenticated_user_can_create_new_questions`）
- **结构**: given-when-then 三段式，使用 AssertJ 断言
- **权限模拟**: 集成测试使用 `@WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")`

### 测试数据

使用 factory 包下的工厂类（`QuestionFactory`、`AnswerFactory`、`UserFactory` 等）创建测试实体和 DTO。

### 单元测试

使用 JUnit 5 + Mockito（`@ExtendWith(MockitoExtension.class)`），对 service 和 policy 层的依赖进行 mock（`@Mock`），被测对象用 `@InjectMocks`。

### 异步测试

使用 Awaitility 库等待断言满足，格式见 `QWEN.md` 中的示例。

## 重要约定

- `.gitignore` 中排除了 `src/main/resources/application.yaml`（含敏感信息），本地需自行维护数据库和邮件配置
- `uploads/avatars/` 目录不提交到 Git
- 邮件测试使用 GreenMail 轻量级测试服务器，集成测试中通过 `BaseContainerTest` 静态初始化
- `@Tag("online")` 标记需要访问外部服务的测试（如百度翻译），默认排除

## AI 开发规范

执行任何开发任务前，AI MUST 按顺序读取以下文件：

1. `ai-rules/AI-Agent-Harness-规范.md` — 本项目的上下文、工具、验证、流程、权限和记录规范
2. `ai-rules/测试用例编写规范.md` — 测试编写、断言、Mock 和数据准备规则

涉及新需求时，还需读取对应的 SDD 规格文档（通常在 `docs/sdd/` 下）。

以上文件中的 MUST / MUST NOT 指令对 AI 具有约束力。AI 不得跳过或自行解释。
