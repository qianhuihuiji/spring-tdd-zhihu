# AI Agent Harness 规范

> 版本：1.0（具化自模板 1.9）
> 具化日期：2026-08-07
> 适用对象：AI 编程助手、开发者、代码审查者
> 规范级别：`MUST` 必须；`MUST NOT` 禁止；`SHOULD` 应当

---

## 1. 上下文

**目的：** AI 知道"这个项目是什么、代码该写成什么样"。

### 项目简介
使用 TDD（测试驱动开发）方式构建的仿知乎论坛项目。

### 技术栈
语言：Java 17
框架：Spring Boot 3.0.6
构建工具：Maven 3.9.11（IntelliJ IDEA 内置）
ORM：MyBatis + MyBatis Generator
数据库：MySQL 8.0，连接池 Druid 1.2.22
安全认证：Spring Security + JWT (jjwt 0.11.5)
数据库迁移：Flyway (flyway-mysql)
消息队列：Kafka (spring-kafka)
缓存：Redis (spring-boot-starter-data-redis)
邮件：spring-boot-starter-mail
定时任务：Xxl-Job 3.4.0
API 文档：SpringDoc OpenAPI 2.3.0
分页：PageHelper 1.4.7
工具：Lombok, Apache Commons Lang3, DataFaker 2.4.2

### 入口文件
- CLAUDE.md：项目根目录，含构建命令、架构、测试规范
- README.md：项目根目录，含技术栈、快速开始、API 示例
- 架构文档：无独立架构文档，CLAUDE.md 的「项目架构」段已覆盖

### 模块划分
单模块 Maven 项目，包结构分层：

| 包 | 职责 |
|------|------|
| `controller/` | HTTP 层，处理请求/响应，调用 service |
| `service/` + `service/impl/` | 业务逻辑层 |
| `policy/` | 业务策略/权限判断（如能否标记最佳答案） |
| `mbg/mapper/` | MyBatis Generator 生成的数据访问层 |
| `mbg/model/` | 数据实体和 Example 查询类 |
| `model/dto/` | 请求 DTO（UserRegisterDto, QuestionDto 等） |
| `model/vo/` | 响应 VO（QuestionVo, AnswerVo 等） |
| `model/enums/` | 枚举（VoteActionType 等） |
| `security/` | JWT 过滤器、认证入口、UserDetailsService |
| `config/` | SecurityConfig, WebConfig, SpringDocConfig 等 |
| `event/` | Spring 事件类 |
| `listener/` | Spring 事件监听器 |
| `publisher/` | 事件发布工具 |
| `queue/consumer/` | Kafka 消费者 |
| `queue/producer/` | Kafka 生产者 |
| `common/` | CommonResult<T> 统一响应、ResultCode 状态码 |
| `exception/` | 全局异常处理 GlobalExceptionHandler |
| `redis/` | Redis 模板（JsonRedisTemplate） |
| `util/` | 工具类（EmailVerificationUtil, MD5, HttpGet 等） |
| `task/` | 定时任务（ActiveUserService） |
| `component/` | 组件（EmailSender） |

### 编码规范
- 包命名：`com.nofirst.spring.tdd.zhihu.<分层>`
- 类命名：PascalCase，测试类以 `Test` 结尾
- 异常处理：统一抛出 `ApiException`，由 `GlobalExceptionHandler` 全局处理
- 日志规范：MyBatis SQL 日志通过 `StdOutImpl` 输出
- 依赖注入：使用 Lombok `@AllArgsConstructor` 实现构造器注入
- 测试方法命名：小写下划线分隔的完整场景描述（如 `guests_may_not_create_questions`）
- 测试结构：given-when-then 三段式，使用 AssertJ 断言
- 统一响应：所有 API 返回 `CommonResult<T>`（code/message/data）

### 业务术语
| 缩写 | 全称 | 备注 |
|------|------|------|
| BS | 行为切片（Behavior Slice） | 验收标准拆解的最小可测试行为单元，编号 BS-xxx |
| TDD | 测试驱动开发（Test-Driven Development） | 本项目核心开发方法论 |
| mbg | MyBatis Generator | 代码生成模块 |
| slug | URL 友好标识符 | 问题标题的英文翻译，用于 SEO 友好 URL |

### 测试规范
- 框架：JUnit 5 + Mockito + AssertJ + MockMvc + Testcontainers + Awaitility
- 测试目录：`src/test/java/com/nofirst/spring/tdd/zhihu/`
  - `integration/`：集成测试，继承 `BaseContainerTest`
  - `unit/`：单元测试（service/policy/util）
  - `factory/`：测试数据工厂类
  - `matcher/`：自定义断言匹配器
  - `listener/`：监听器测试
- 集成测试基类：`BaseContainerTest`（路径 `src/test/java/.../integration/BaseContainerTest.java`）
  - 容器组件：MySQL 8.0 + Kafka (confluentinc/cp-kafka:7.6.1) + Redis 7.2.3 + GreenMail (SMTP)
  - 认证模拟：`@WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")`
  - 提供 `cleanUp*()` 方法用于 `@BeforeEach` 中清理测试数据
- 单元测试基类：无（使用 `@ExtendWith(MockitoExtension.class)`，`@Mock` + `@InjectMocks`）
- 测试数据工厂：`UserFactory`, `QuestionFactory`, `AnswerFactory`, `CommentFactory`, `SubscriptionFactory`, `EntityFactory`
- 测试数据播种器：`Seeder`（封装 Mapper 快速插入测试数据）
- 自定义匹配器：`AnswerMatcher`
- 应用主类：`com.nofirst.spring.tdd.zhihu.SpringTddZhihuApplication`
- 在线测试标签：`@Tag("online")`（标记需外部服务的测试，默认排除）

---

## 2. 工具

**目的：** AI 知道"用什么命令干活"。

### 构建工具路径
`D:\JetBrains\IntelliJ IDEA Ultimate\plugins\maven\lib\maven3\bin\mvn`

### 可用命令

> **Windows 环境：** 所有构建、测试、lint 命令以环境变量前缀执行，确保控制台输出 UTF-8 编码：
> ```
> JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8" mvn <goal>
> ```
> `JAVA_TOOL_OPTIONS` 对 Maven 自身 JVM 与 surefire 分叉 JVM 同时生效，无需修改项目文件。

| 操作 | 命令 | 说明 |
|------|------|------|
| 编译 | `mvn compile -DskipTests` | 仅编译，跳过测试 |
| 全部测试（排除 online） | `mvn test -DexcludedGroups=online` | 排除 @Tag("online") 的测试 |
| 全部测试（含 online） | `mvn test` | 不做分组排除 |
| 单个测试类 | `mvn test -Dtest=XxxTest -DexcludedGroups=online` | 运行指定测试类 |
| 单个测试方法 | `mvn test -Dtest=XxxTest#methodName -DexcludedGroups=online` | 运行指定测试方法 |
| Lint | UNKNOWN | 项目未配置 Checkstyle/Spotless/PMD 插件 |
| 覆盖率 | UNKNOWN | 项目未配置 JaCoCo 插件 |
| 本地启动 | `mvn spring-boot:run` | 启动 Spring Boot 应用 |
| Flyway 迁移 | `mvn flyway:migrate` | 执行数据库迁移脚本 |
| MyBatis 代码生成 | `mvn mybatis-generator:generate` | 基于 generatorConfig.xml 生成 Mapper/Model/XML |

### 外部依赖切换

| 依赖 | 本地开发 | 集成测试 |
|------|---------|---------|
| MySQL | `application.yaml` 中的连接配置 | Testcontainers 动态覆盖 |
| Kafka | 本地 Kafka（`localhost:9092`） | Testcontainers 动态覆盖 |
| Redis | 本地 Redis（`localhost:6379`） | Testcontainers 动态覆盖 |
| 邮件 | QQ 邮箱 SMTP | GreenMail 测试服务器 |
| 百度翻译 | `[需人工确认]` 外部 API，`@Tag("online")` 排除 | `@Tag("online")` 排除 |

---

## 3. 验证机制

**目的：** AI 每一步操作后都有自动化反馈，错了立刻知道。

### 验证流水线

```
AI 修改代码
  → 编译（失败则停止，不进入测试）
    → 相关单元测试（失败则停止，不进入回归）
      → 全量回归测试
        → 静态检查（如已配置）
          → 报告结果
```

| 阶段 | 命令 | 超时 | 说明 |
|------|------|------|------|
| 编译 | `mvn compile -DskipTests` | 60s | 修改代码后先编译 |
| 相关测试 | `mvn test -Dtest=XxxTest -DexcludedGroups=online` | 120s | 修改后立即运行相关测试 |
| 全量回归 | `mvn test -DexcludedGroups=online` | 300s | 行为切片完成后运行全量 |
| 静态检查 | UNKNOWN | — | 项目未配置 lint 工具 |
| 覆盖率 | UNKNOWN | — | 项目未配置 JaCoCo |

> 超时值为初始占位，首次执行验证流水线后按实测回写。

### AI 行为

- AI MUST 在每次修改代码后执行验证流水线
- 任一阶段失败，AI MUST 报告原因并停止，不得跳过失败阶段进入下一步
- AI MUST NOT 在验证未全部通过时宣称任务完成
- AI 执行验证流水线后，若输出存在乱码，MUST 报告"输出编码异常"并提请开发者修复

### 老项目全量测试豁免

本项目为持续演进项目。全量回归测试可能存在历史遗留失败：

- 首次具化 Harness 时，运行全量测试并记录结果。存在失败时，列出失败数量和大致原因分类，标记为 `known_failures`
- 需求开发时，AI 只保证本次新增和修改的测试通过。历史失败测试不阻塞 Red-Green-Refactor 流程
- 测试范围由开发者在 SDD 规格的「行为切片」中标注，AI 以此为准
- 如果历史失败测试与本次修改的代码位于同一模块，AI SHOULD 提醒开发者"存在 N 个历史失败测试"

---

## 4. 流程

**目的：** AI 知道"按什么步骤做事"。

### 准入条件
- [ ] SDD 规格已就绪（参见 `ai-rules/SDD-规格模板.md`）
- [ ] 行为切片已完成
- [ ] 测试方案已获开发者批准

### 执行顺序（Red-Green-Refactor）
1. **Red**：编写测试并确认有效失败（期望失败理由必须明确）
2. **Green**：最小实现使测试通过（不得提前重构、不得删断言、不得 Mock 核心逻辑）
3. **Refactor**：结构调整并回归（不改变外部行为）

### 审批节点
- 测试方案：MUST 开发者批准后进入 Red 阶段
- 修改公共接口/基类/pom.xml：MUST 开发者确认
- 测试基线提交（修改类需求）：MUST 开发者审核并手动提交

### 交付标准
- [ ] 本次新增/修改的测试全部通过
- [ ] 回归测试无新增失败（历史 `known_failures` 除外）
- [ ] 静态检查通过（如已配置 lint 工具）
- [ ] 覆盖率符合要求（如已配置覆盖率工具）
- [ ] 证据记录完整（见 §6）

### 修改类需求：测试审计

当需求类型为修改旧功能、重构、行为变更或缺陷修复时，AI MUST NOT 直接开始修改代码。必须先：
1. 检查目标代码是否已有测试用例
2. 检查既有测试是否符合《测试用例编写规范》
3. 输出审计结论和建议
4. 开发者确认并提交测试基线后，方可进入 Red-Green-Refactor

---

## 5. 权限边界

**目的：** AI 知道"什么能做、什么不能做、什么需要确认"。

### 允许（MAY）
- 读取项目所有源文件
- 修改 `src/main/` 和 `src/test/` 下的代码
- 运行构建、测试、lint 命令
- 创建测试数据工厂和夹具
- 创建特性分支（`feature/<功能名>`）
- 提交代码到特性分支

### 禁止（MUST NOT）
- `git push`、`git commit --amend`、`git rebase`
- 直接提交到共享分支（main/master）
- 修改 CI/CD 配置
- 操作生产或预发布环境
- 安装系统级软件包
- 删除或跳过既有测试（除非开发者明确批准）
- 修改 `.gitignore`、`generatorConfig.xml`
- 凭经验假定 `UNKNOWN` 参数
- 跳过审批节点自行决定
- 在验证未全部通过时宣称任务完成

### 需确认
- 修改 `pom.xml`（新增依赖、修改插件配置）
- 修改公共接口或基类（`BaseContainerTest`、`SecurityConfig` 等）
- 删除既有测试
- 连接非本地数据库
- 修改 Flyway 迁移脚本（`src/main/resources/db/migration/`）

---

## 6. 过程记录

**目的：** 每个需求的关键决策和操作可追溯，出问题能查。

### 存储位置
`docs/ai-records/YYYY-MM-DD-<功能名称>-ai-record.md`

一次需求一条记录，不得跨多个需求合并补记。

### 记录格式
- **完整记录**：使用《测试用例编写规范》§15 完整证据模板（默认）
- **精简记录**：开发者声明记录级别为精简时使用（behavior + red + green + regression）

### 每条记录必须包含
- 行为描述（BS 编号）
- 测试方案是否获批
- Red 结果和有效性（`red.valid`）
- Green 结果（`green.result`）
- 回归结果（`regression.result`）
- 未解决问题（如有）

> 记录中出现 `red.valid=false` 或 `green.result=failed` 时，AI MUST 明确说明原因。

---

## 附录 A：Harness 完成检查清单

- [x] **上下文**：项目简介、技术栈、入口文件、模块划分、编码规范、业务术语、测试规范 均已填充
- [x] **工具**：构建、测试（全量/单个类/单个方法）、本地启动、Flyway 迁移、MyBatis 代码生成命令可用；Lint 和覆盖率为 UNKNOWN
- [x] **验证**：编译 → 相关测试 → 全量回归 流水线定义完成；Windows 输出编码已配置
- [x] **流程**：准入条件、执行顺序、审批节点、交付标准 已定义
- [x] **权限**：允许清单、禁止清单、需确认清单 已明确
- [x] **记录**：存储位置、记录格式、记录时机 已确定
- [x] **基类**：`BaseContainerTest` 已存在（MySQL + Kafka + Redis + GreenMail），无需生成
- [ ] **AI 验证**：待执行——让 AI 读取本 Harness 规范，执行一条简单命令（如编译），确认 AI 能正确使用配置，并记录实测耗时回写验证流水线超时值
- [ ] **Lint**：UNKNOWN — 项目未配置静态检查工具
- [ ] **覆盖率**：UNKNOWN — 项目未配置 JaCoCo

---

## 附录 B：具化变更记录

| 文件 | 位置 | 变更类型 | 原内容 | 新内容 | 来源 |
|------|------|---------|--------|--------|------|
| Harness | §1 上下文 | 自动填充 | `语言：` | `Java 17` | pom.xml |
| Harness | §1 上下文 | 自动填充 | `框架：` | `Spring Boot 3.0.6` | pom.xml |
| Harness | §1 上下文 | 自动填充 | `构建工具：` | `Maven` | pom.xml |
| Harness | §1 上下文 | 自动填充 | `数据库：` | `MySQL 8.0 + Druid 1.2.22` | pom.xml |
| Harness | §1 上下文 | 自动填充 | `ORM：` | `MyBatis + MyBatis Generator` | pom.xml |
| Harness | §1 上下文 | 自动填充 | `安全认证：` | `Spring Security + JWT (jjwt 0.11.5)` | pom.xml |
| Harness | §1 上下文 | 自动填充 | `消息队列：` | `Kafka (spring-kafka)` | pom.xml |
| Harness | §1 上下文 | 自动填充 | `缓存：` | `Redis` | pom.xml |
| Harness | §1 上下文 | 自动填充 | `测试框架：` | `JUnit 5 + Mockito + AssertJ + Testcontainers` | pom.xml |
| Harness | §1 上下文 | 自动填充 | `测试目录：` | `src/test/java/com/nofirst/spring/tdd/zhihu/` | 目录树 |
| Harness | §1 上下文 | 自动填充 | `集成测试基类：` | `BaseContainerTest` | 源码搜索 |
| Harness | §1 上下文 | 自动填充 | `应用主类：` | `SpringTddZhihuApplication` | 源码搜索 |
| Harness | §1 上下文 | 自动填充 | `测试数据工厂：` | `UserFactory, QuestionFactory, AnswerFactory, CommentFactory, SubscriptionFactory, EntityFactory` | 源码搜索 |
| Harness | §1 上下文 | 自动填充 | `认证模拟：` | `@WithUserDetails(value="John", userDetailsServiceBeanName="customUserDetailsService")` | CLAUDE.md |
| Harness | §1 上下文 | 自动填充 | `架构概览：` | 16 个包的职责描述 | 目录树 + CLAUDE.md |
| Harness | §1 上下文 | 自动填充 | `编码规范：` | Lombok @AllArgsConstructor 构造器注入、测试命名约定等 | CLAUDE.md |
| Harness | §2 工具 | 自动填充 | `编译` | `mvn compile -DskipTests` | pom.xml |
| Harness | §2 工具 | 自动填充 | `全部测试` | `mvn test -DexcludedGroups=online` | CLAUDE.md |
| Harness | §2 工具 | 自动填充 | `单个测试类` | `mvn test -Dtest=XxxTest -DexcludedGroups=online` | CLAUDE.md |
| Harness | §2 工具 | 自动填充 | `单个测试方法` | `mvn test -Dtest=XxxTest#method -DexcludedGroups=online` | CLAUDE.md |
| Harness | §2 工具 | 自动填充 | `本地启动` | `mvn spring-boot:run` | pom.xml + README.md |
| Harness | §2 工具 | 自动填充 | `Flyway 迁移` | `mvn flyway:migrate` | pom.xml |
| Harness | §2 工具 | 自动填充 | `MyBatis 代码生成` | `mvn mybatis-generator:generate` | pom.xml |
| Harness | §2 工具 | UNKNOWN | `Lint` | 项目未配置 Checkstyle/Spotless/PMD 插件 | pom.xml 分析 |
| Harness | §2 工具 | UNKNOWN | `覆盖率` | 项目未配置 JaCoCo 插件 | pom.xml 分析 |
| Harness | §2 工具 | 自动填充 | `外部依赖切换` | Testcontainers 动态覆盖 + GreenMail | BaseContainerTest 源码 |
| Harness | §3 验证 | 自动填充 | `输出编码` | UTF-8（`JAVA_TOOL_OPTIONS` 前缀） | Windows 环境 |
| Harness | §4 流程 | 自动填充 | `执行顺序` | Red-Green-Refactor | CLAUDE.md |
| Harness | §4 流程 | 自动填充 | `审批节点` | 测试方案批准、修改确认 | 模板默认 + 启动指南 |
| Harness | §5 权限 | 自动填充 | `允许/禁止/需确认` | 基于模板 + 项目具体化 | 模板 + 项目文件分析 |
| Harness | §6 记录 | 自动填充 | `存储位置` | `docs/ai-records/` | 模板默认 |
| 集成测试基类 | — | 已存在，无需生成 | — | `BaseContainerTest`（MySQL+Kafka+Redis+GreenMail） | 源码搜索 |
| Testcontainers | — | 已采用（无需决策） | — | MySQL 8.0 + Kafka + Redis + GreenMail | pom.xml + BaseContainerTest |

---

## 附录 C：UNKNOWN / 待确认项

| 编号 | 位置 | 内容 | 状态 |
|------|------|------|------|
| 1 | §2 工具 → Lint | 项目未配置静态检查工具（Checkstyle/Spotless/PMD） | 待开发者决定是否添加 |
| 2 | §2 工具 → 覆盖率 | 项目未配置 JaCoCo | 待开发者决定是否添加 |
| 3 | §5 权限 → 外部依赖 | 百度翻译 API 为外部依赖，由 `@Tag("online")` 排除 | 待确认切换/隔离策略 |

---

> **下一步：** 按《AI-TDD工作流启动指南》第二步，适配《测试用例编写规范》。准备好后回复"继续第二步"。
