# AI Agent Harness 模板

> 版本：1.9
> 适用对象：项目负责人、开发者
> 规范级别：`MUST` 必须；`MUST NOT` 禁止；`SHOULD` 应当

---

## 0. 什么是 Harness

Harness 是 AI Agent 在本项目的"工作环境 + 操作手册"。它回答一个问题：

> AI 进入这个项目后，应该读什么、用什么、怎么干、谁说了算？

一份合格的 Harness 规范 MUST 覆盖以下 6 个要素，缺一不可。

### 0.1 具化指南（给 AI 的指令）

当开发者要求"根据本项目环境具化 Harness 模板"时，AI MUST 按以下顺序读取项目文件并填充本模板。此过程称为"具化"。

| 信息 | 来源文件 | 提取方式 | 对应要素 |
|------|---------|---------|---------|
| 项目简介、架构概览、模块职责 | `README.md` 等项目说明文档 | **优先读取**；说明文档缺失时从 `pom.xml` 和目录树推断 | §1 上下文 |
| 语言、框架、Spring Boot 版本 | `pom.xml` / `build.gradle` | 读取 `<parent>` 或 `spring-boot` 依赖版本 | §1 上下文 |
| 数据库类型和版本 | `pom.xml` + `application.yml` | 读取 JDBC/MySQL/PostgreSQL 驱动依赖和连接配置 | §1 上下文、§2 工具 |
| 缓存、MQ、其他中间件 | `pom.xml` + `application.yml` | 读取 Redis/Kafka/RabbitMQ 依赖和连接配置 | §1 上下文、§2 工具 |
| 模块划分 | 目录树 `ls -R src/` 或项目根 `pom.xml` 的 `<modules>` | 识别 Maven 多模块或 Gradle 子项目 | §1 上下文 |
| 测试框架版本 | `pom.xml` | 读取 `junit`、`mockito`、`assertj` 依赖版本 | §1 上下文 |
| 测试目录路径 | 目录树 | 搜索 `src/test/java/` 确认测试根路径 | §1 上下文 |
| 测试基类 | 搜索源码 | 搜索 `*BaseContainerTest*`、`*AbstractTest*`、`*TestBase*`，仅集成测试基类；单元测试不需要基类 | §1 上下文、§2 工具 |
| 测试数据工厂 | 搜索源码 | 搜索 `*Factory*`、`*Fixture*`、`*Builder*`、`*Seeder*` | §1 上下文 |
| 编码规范 | `.editorconfig`、`checkstyle.xml`、`CLAUDE.md` | 读取配置，提取命名和格式约定 | §1 上下文 |
| 构建命令 | `pom.xml` 中的 `<plugins>` + 既有脚本（如 `Makefile`、`build.sh`） | 从 Maven/Gradle 插件和既有脚本推断构建、测试、lint、覆盖率命令 | §2 工具、§3 验证 |
| 测试命令 | `pom.xml` 中 `maven-surefire-plugin` 和 `maven-failsafe-plugin` 的配置 | 读取 `<includes>`、`<excludes>` 等配置，推断测试选择器模式 | §2 工具 |
| 代码检查命令 | `pom.xml` 中的 `checkstyle`/`spotless`/`pmd` 插件 | 读取插件配置，构造 lint 命令 | §2 工具 |
| 数据库连接信息 | `application.yml` / `application.properties` | 读取 `spring.datasource` 配置段 | §1 上下文 |
| 环境变量 | `.env` / `.env.example` / `docker-compose.yml` | 读取环境变量定义（只读变量名，不得读取值中的密钥） | §1 上下文、§5 权限 |

**提取规则：**

- README.md、架构说明等项目说明文档为**优先读取**来源：项目简介、架构概览、模块职责以说明文档为准；说明文档缺失时，从构建文件与目录树推断并标注来源
- 以上文件中能找到的信息 → 自动填充到对应要素
- 以上文件中找不到的信息 → MUST 标记为 `UNKNOWN`，不得猜测
- 配置文件中的密码、密钥、令牌 → MUST NOT 提取，标记为 `[需人工确认]`
- 规范前提不满足（如 Feign 客户端 url 硬编码、缺少外部依赖切换 profile、需要集成测试但测试基类缺失）→ 标记为 `[需人工确认]` 改造项，MUST NOT 自行修改生产代码，由开发者确认并改造后再落地
- 单模块项目 → 工具命令中去掉 `-pl module` 参数
- 多模块项目 → 工具命令中必须包含 `-pl <模块名>` 或等效参数
- 应用主类：搜索 `@SpringBootApplication` 标注的启动类，用于生成集成测试基类的 `@SpringBootTest(classes = ...)`
- 容器组件依赖：读取 testcontainers 系依赖（`org.testcontainers:mysql`/`:postgresql`/`:kafka`、`com.redis:testcontainers-redis`）与中间件 starter（`rocketmq-spring-boot-starter`、`spring-kafka`、`spring-boot-starter-amqp`），确定集成测试容器组件清单

**Testcontainers 决策点：** 具化时 MUST 与开发者确认是否采用 Testcontainers 运行集成测试：

- 采用 → 生成集成测试基类前，MUST 先检查项目是否已有测试基类（搜索 `*BaseContainerTest*`、`*AbstractTest*`、`*TestBase*`，即本表「测试基类」行探测结果）：
  - 已有 → 复用现有基类，MUST NOT 覆盖或重复生成；组件依赖或属性注册不一致时输出更新建议，经开发者确认后再更新
  - 没有 → 按《测试用例编写规范》§10.2「组件依赖 → 容器段映射表」与「生成步骤」，按组件依赖生成骨架并写入 `src/test/java/<包>/BaseContainerTest.java`，作为具化产物之一
  - 容器镜像无法跑通、版本不兼容等 → 列为 `[需人工确认]` 改造项
- 不采用 → 从《测试用例编写规范》§10.2 的替代方案（1/2/3 选一）中与开发者确认一项并写入规范，AI MUST NOT 自行选择

**构建工具路径探测规则：**

AI MUST 先自动搜索 Maven / Gradle 可执行文件路径：

- 搜索位置：环境变量（`MAVEN_HOME`、`GRADLE_HOME`、`PATH`）、项目根目录（`mvnw`、`gradlew`）、常见安装路径
- 将搜索到的结果列出，逐项请开发者确认，格式如下：

```text
🔍 搜索到以下构建工具路径：
1. ./mvnw（项目自带 Wrapper，推荐）
2. /usr/local/bin/mvn（系统安装，版本 3.9.5）
请确认使用哪个路径？（回复编号或输入完整路径）
```

- 即使只找到一个，也 MUST 请开发者确认后再使用——防止本机存在多个版本导致命令行为不一致
- 如果找不到任何结果 → 标记为 UNKNOWN，请开发者手动提供路径

后续所有构建、测试、lint、覆盖率命令 MUST 以确认后的路径为前缀。

**规范漂移检查：** 具化是一次性的，但项目会持续演进。每个需求开始时，AI MUST 核对 Harness 规范中记录的模块、构建/测试命令、路径与项目现状是否一致；发现漂移（新增模块、升级框架、命令或路径变化等）时，MUST 先输出变更建议，经开发者确认后更新 Harness 规范并追加变更记录（见附录 B），再开始需求开发。

**具化完成后 AI MUST 输出具化变更记录（见附录 B）。**

---

## 1. 上下文

**目的：** AI 知道"这个项目是什么、代码该写成什么样"。

| 配置项 | 内容 | 示例 |
|--------|------|------|
| 项目简介 | 一句话描述项目做什么 | "医药零售 POS 收银系统" |
| 技术栈 | 语言、框架、构建工具 | Java 8 / Spring Boot 2.7 / Maven |
| 入口文件 | AI 必读的项目文档 | CLAUDE.md、README.md、CONTRIBUTING.md |
| 架构概览 | 模块划分和职责 | `module-a`：订单核心 / `module-b`：支付网关 |
| 编码规范 | 命名、包结构、异常处理约定 | 包名：`com.xxx.模块.分层` / 异常：统一 `BusinessException` |
| 业务术语表 | 项目特有缩写和概念 | setl = settlement（结算）/ yb = 医保 |
| 测试规范 | 测试框架和约定 | JUnit 5 + Mockito + AssertJ + Testcontainers |

**AI 行为：**

- AI MUST 在执行任何任务前先读取上下文
- 上下文缺失时，AI MUST 标记为 `UNKNOWN` 并停止，不得凭经验假定

**填空模板：**

```markdown
## 项目上下文

### 项目简介
（一句话描述）

### 技术栈
语言：   框架：   构建工具：

### 入口文件
- CLAUDE.md：
- README.md：
- 架构文档：

### 模块划分
| 模块 | 职责 |
|------|------|
|      |      |

### 编码规范
- 包命名：
- 类命名：
- 异常处理：
- 日志规范：

### 业务术语
| 缩写 | 全称 | 备注 |
|------|------|------|
| BS | 行为切片（Behavior Slice） | 验收标准拆解的最小可测试行为单元，编号 BS-xxx |
|      |      |      |

### 测试规范
- 框架：
- 测试目录：
- 集成测试基类：
```

---

## 2. 工具

**目的：** AI 知道"用什么命令干活"。

| 配置项 | 内容 | 示例 |
|--------|------|------|
| 构建命令 | 编译、打包 | `mvn compile -DskipTests` |
| 测试命令 | 运行全部 / 单个类 / 单个方法 | `mvn test -pl module-a -Dtest=UserRegisterTest` |
| 代码检查 | lint、格式化、静态分析 | `mvn checkstyle:check` / `mvn spotless:check` |
| 覆盖率 | 生成并查看覆盖率报告 | `mvn verify -Pcoverage` |
| 本地启动 | 启动开发环境 | `mvn spring-boot:run -pl module-a` |

**AI 行为：**

- AI MUST 使用配置中指定的命令，不得自行构造
- 命令执行失败时，AI MUST 报告错误输出，不得自行修改命令重试
- AI MUST NOT 执行未在配置中列出的命令（如 `docker rm`、`git push`）

**填空模板：**

```markdown
## 可用工具

| 操作 | 命令 | 说明 |
|------|------|------|
| 编译 |      |      |
| 全部测试 |      |      |
| 单个测试类 |      |      |
| 单个测试方法 |      |      |
| Lint |      |      |
| 覆盖率 |      |      |
| 本地启动 |      |      |
```

---

## 3. 验证机制

**目的：** AI 每一步操作后都有自动化反馈，错了立刻知道。

| 配置项 | 内容 | 示例 |
|--------|------|------|
| 编译检查 | 修改代码后先编译 | `mvn compile` |
| 单元测试 | 修改后立即运行相关测试 | `mvn test -pl module-a -Dtest=XxxTest` |
| 回归测试 | 每个行为切片完成后运行全量 | `mvn test` |
| 静态检查 | 提交前运行 lint | `mvn checkstyle:check` |
| 覆盖率检查 | 确认新增代码被测试覆盖 | `mvn verify -Pcoverage` |

**标准验证流水线：**

```text
AI 修改代码
  → 编译（失败则停止，不进入测试）
    → 相关单元测试（失败则停止，不进入回归）
      → 全量回归测试
        → 静态检查
          → 报告结果
```

**AI 行为：**

- AI MUST 在每次修改代码后执行验证流水线
- 任一阶段失败，AI MUST 报告原因并停止，不得跳过失败阶段进入下一步
- AI MUST NOT 在验证未全部通过时宣称任务完成

**老项目全量测试豁免：**

如果项目是存量的老项目，全量回归测试可能存在历史遗留失败。AI MUST 按以下规则处理：

- 首次具化 Harness 时，运行全量测试并记录结果。存在失败时，列出失败数量和大致原因分类，标记为 `known_failures`，不得要求开发者在本次具化中全部修复
- 需求开发时，AI 只保证本次新增和修改的测试通过。历史失败测试不阻塞 Red-Green-Refactor 流程
- 测试范围由开发者在 SDD 规格的「行为切片」中标注，AI 以此为准
- 如果历史失败测试与本次修改的代码位于同一模块，AI SHOULD 提醒开发者"存在 N 个历史失败测试"，但不阻塞工作流

**输出编码约定（Windows 环境）：**

Windows 中文区域下，Maven / Java 默认以 GBK 编码输出控制台日志，与 UTF-8 终端不一致时中文显示为乱码，AI 无法可靠读取测试结果。

**执行期参数方案（推荐，零项目改动）：**

- 所有构建、测试、lint 命令以环境变量前缀执行（Java 18+ 的 stdout 编码独立，三个参数缺一不可）：

  `JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8" mvn test`

- `JAVA_TOOL_OPTIONS` 对 Maven 自身 JVM 与 surefire 分叉 JVM 同时生效（分叉 JVM 继承环境变量），无需修改 `pom.xml`、`.mvn/jvm.config` 或 logback 配置
- 该前缀 MUST 写入本项目 Harness 规范「工具」命令清单，作为标准命令执行；开发者在本地 IDE 运行不受影响（可另行设置，非本项目范围）
- 不使用命令行 `-DargLine` 传递编码参数（会覆盖 pom 中已配置的 argLine，如 Jacoco 的 `@{argLine}`）；环境变量前缀天然不与 pom 配置冲突

**局限与例外：**

- 项目 logback / log4j2 的 ConsoleAppender 显式声明了非 UTF-8 charset 时，执行期参数无法覆盖 → 标记为 `[需人工确认]` 改造项
- `pom.xml` 的 `<project.build.sourceEncoding>` 属于编译期源码编码约定，与运行日志乱码无关，不纳入本约定

**AI 行为：**

- AI 执行验证流水线后，若输出存在乱码，MUST 报告"输出编码异常"并提请开发者修复，MUST NOT 凭乱码内容判断测试成败
- 项目已具备执行期参数前缀时，AI MUST NOT 为绕开编码问题修改 `pom.xml` 或 `.mvn/` 配置（属 §5 权限边界需确认项）

**填空模板：**

```markdown
## 验证流水线

| 阶段 | 命令 | 超时 | 说明 |
|------|------|------|------|
| 编译 |      | 60s |      |
| 相关测试 |      | 120s |      |
| 全量回归 |      | 300s |      |
| 静态检查 |      | 60s |      |
| 覆盖率 |      | 120s |      |

（Windows 项目附加）输出编码：UTF-8（命令前缀 `JAVA_TOOL_OPTIONS` 已写入工具命令）

> 超时值为占位，首次执行验证流水线后按实测回写。
```

---

## 4. 流程

**目的：** AI 知道"按什么步骤做事"。

| 配置项 | 内容 | 示例 |
|--------|------|------|
| 开发流程 | Red-Green-Refactor 是强制执行顺序 | 不得跳过 Red 直接 Green |
| 任务准入 | 进入编码前必须满足的条件 | SDD 规格已就绪、测试方案已批准 |
| 审批节点 | 哪些步骤需要人工确认 | 测试方案必须获批后才能写代码 |
| 交付标准 | 任务完成的条件 | 回归全绿 + 静态检查通过 + 证据记录完整 |

**AI 行为：**

- AI MUST 严格按照本规范定义的流程执行
- AI MUST NOT 跳过审批节点自行决定
- 流程定义不清晰时，AI MUST 请求澄清，不得自行解释

**填空模板：**

```markdown
## 工作流程

### 准入条件
- [ ] SDD 规格已就绪
- [ ] 行为切片已完成
- [ ] 测试方案已获批准

### 执行顺序
1. Red：编写测试并确认有效失败
2. Green：最小实现使测试通过
3. Refactor：结构调整并回归

### 审批节点
- 测试方案：MUST 开发者批准
- 合并请求：MUST 开发者审查

### 交付标准
- [ ] 回归测试全绿
- [ ] 静态检查通过
- [ ] 覆盖率符合要求
- [ ] 证据记录完整
```

---

## 5. 权限边界

**目的：** AI 知道"什么能做、什么不能做、什么需要确认"。

| 类别 | 允许（MAY） | 禁止（MUST NOT） | 需确认 |
|------|-----------|-----------------|--------|
| 代码 | 新增测试和生产代码 | 删除或跳过既有测试 | 修改公共接口 |
| 文件 | 读取项目文件 | 修改 `.gitignore`、`pom.xml` | 新增依赖 |
| 命令 | 运行构建、测试、lint | `git push`、删除容器和卷 | `docker-compose` 启停 |
| 数据 | 使用测试夹具 | 使用真实密钥、令牌、用户数据 | 连接非本地数据库 |
| 环境 | 本地开发环境 | 生产环境、预发布环境 | 修改 CI/CD 配置 |

**AI 行为：**

- AI MUST 在每次操作前检查是否越界
- AI MUST NOT 以"我认为这应该是允许的"为由绕过权限
- 遇到需确认的操作，AI MUST 先询问并等待明确同意

**Git 操作约定：**

- AI MAY 创建特性分支（如 `feature/<功能名>`），MUST NOT 直接提交到共享分支
- 提交时机由开发者指示；测试基线提交（见《测试用例编写规范》§12.2）建议格式：`test: 为 <功能> 补充/重写测试用例`
- 提交前 MUST 先通过验证流水线（见 §3）
- `git push`、`git rebase`、`git commit --amend` 仍为 MUST NOT（见上表）

**填空模板：**

```markdown
## 权限边界

### 允许
- 读取项目所有源文件
- 修改 `src/main/` 和 `src/test/` 下的代码
- 运行构建、测试、lint 命令
- 创建测试数据工厂和夹具
- 创建特性分支（`feature/<功能名>`）

### 禁止
- `git push`、`git commit --amend`、`git rebase`
- 直接提交到共享分支
- 修改 CI/CD 配置
- 操作生产或预发布环境
- 安装系统级软件包

### 需确认
- 修改 `pom.xml` / `build.gradle`（新增依赖）
- 修改公共接口或基类
- 删除既有测试
- 连接非本地数据库
```

---

## 6. 过程记录

**目的：** 每个需求的关键决策和操作可追溯，出问题能查。

| 配置项 | 内容 | 示例 |
|--------|------|------|
| 记录格式 | YAML 格式的执行记录 | 见《测试用例编写规范》§15 AI 工作证据模板 |
| 存储位置 | 记录存放路径 | `docs/ai-records/YYYY-MM-DD-<功能名称>-ai-record.md`，一次需求一条 |
| 记录时机 | 每个需求完成后，一次需求一条 ai-records | 不得跨多个需求合并补记 |
| 最小内容 | 即使使用精简模板也必须包含 | behavior + red + green + regression |

**AI 行为：**

- AI MUST 在每个需求完成后记录一次（一次需求一条 ai-records，汇总该需求所有行为切片的证据）
- AI MUST NOT 跨多个需求合并补记
- 记录中出现 `red.valid=false` 或 `green.result=failed` 时，AI MUST 明确说明原因

**填空模板：**

```markdown
## 过程记录

### 存储位置
（路径）

### 记录格式
- 完整记录：使用《测试用例编写规范》§15 完整证据模板
- 精简记录：开发者声明记录级别为精简时使用（behavior + red + green + regression）

### 每条记录必须包含
- 行为描述
- 测试方案是否获批
- Red 结果和有效性
- Green 结果
- 回归结果
- 未解决问题（如有）
```

---

## 附录 A：Harness 完成检查清单

新项目配置 Harness 时，逐项确认：

- [ ] **上下文**：项目简介、技术栈、入口文件、模块划分、编码规范、业务术语、测试规范 均已知
- [ ] **工具**：构建、测试（全量/单个类/单个方法）、lint、覆盖率、本地启动 命令均可用；外部依赖切换（mock/real）命令已记录（如适用）
- [ ] **验证**：编译 → 相关测试 → 全量回归 → 静态检查 流水线可执行；Windows 环境下输出编码统一 UTF-8、测试日志无乱码
- [ ] **流程**：准入条件、执行顺序、审批节点、交付标准 已定义
- [ ] **权限**：允许清单、禁止清单、需确认清单 已明确
- [ ] **记录**：存储位置、记录格式、记录时机 已确定
- [ ] **基类**：采用 Testcontainers 时，集成测试基类已就绪——已检查项目是否已有测试基类（有则复用，无则按组件依赖生成）；组件段与动态属性注册完整，容器可启动
- [ ] **AI 验证**：让 AI 读取本 Harness 规范，执行一条简单命令（如编译），确认 AI 能正确使用配置，并记录实测耗时回写验证流水线超时值

全部就绪后，本 Harness 规范即为本项目的 AI 工作手册。后续每个新需求只需新增 SDD 规格文档 + 参考《测试用例编写规范》，即可启动 AI-TDD 开发工作流。

---

## 附录 B：具化变更记录

> AI 具化完成后 MUST 输出以下格式的变更摘要。开发者通过此表格确认 AI 的自动填充是否准确，以及哪些 UNKNOWN 项需要人工补充。具化后的 Harness 规范随项目演进持续更新，每次更新按本表格式追加一条变更记录。

| 文件 | 位置 | 变更类型 | 原内容 | 新内容 | 来源 |
|------|------|---------|--------|--------|------|
| Harness | §1 上下文 | 自动填充 | `语言：` | `Java 17` | pom.xml |
| Harness | §1 上下文 | 自动填充 | `框架：` | `Spring Boot 3.2` | pom.xml |
| Harness | §1 上下文 | 自动填充 | `构建工具：` | `Maven 3.9` | pom.xml |
| Harness | §1 上下文 | 自动填充 | `测试目录：` | `src/test/java/` | 目录树 |
| Harness | §1 上下文 | 自动填充 | `基类：` | `BaseContainerTest` | 源码搜索 |
| Harness | §2 工具 | 自动填充 | `编译` | `mvn compile -DskipTests` | pom.xml |
| Harness | §2 工具 | 自动填充 | `全部测试` | `mvn test` | pom.xml |
| Harness | §2 工具 | 自动填充 | `单个测试类` | `mvn test -Dtest=XxxTest` | pom.xml |
| Harness | §5 权限 | UNKNOWN | — | — | 无法从代码推断，需人工补充 |
| 测试规范 | §10 | 自动更新 | `JUnit 5` | `JUnit 4` | pom.xml |
| 集成测试基类 | `src/test/java/.../BaseContainerTest.java` | 自动生成 | 无（已检查，项目无基类） | 按 MySQL+Redis 组件生成 | pom.xml + 源码 |

- **自动填充**：AI 从项目文件中提取并填入，开发者确认即可
- **UNKNOWN**：AI 无法从代码中推断，MUST 由开发者手动补充
- **自动更新**：AI 检测到模板默认值与实际项目不一致，已自动修正
- **改造项**：规范前提不满足（如 Feign url 硬编码、切换 profile 缺失），MUST 由开发者人工改造，AI 不得自行修改生产代码
