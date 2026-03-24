# Spring TDD 知乎项目 - 开发上下文

## 项目概述

这是一个使用 **TDD（测试驱动开发）** 方式构建的仿知乎论坛项目。项目基于 Spring Boot 3.0.6，采用 Java 17 作为开发语言，遵循约定大于配置的原则，已集成开发所需的核心依赖和公共组件。

**主要技术栈：**
- **框架**: Spring Boot 3.0.6
- **语言**: Java 17
- **ORM**: MyBatis + MyBatis Generator
- **数据库**: MySQL 8.0
- **连接池**: Druid 1.2.22
- **安全认证**: Spring Security + JWT (jjwt 0.11.5)
- **数据库版本控制**: Flyway
- **测试**: JUnit, Testcontainers, Spring Boot Test
- **消息队列**: Apache Kafka
- **缓存**: Redis

## 项目架构

项目采用典型的 Spring Boot MVC 架构，包含以下核心模块：

- **common**: 通用组件（如统一响应格式 CommonResult）
- **config**: 配置类
- **controller**: 控制器层，处理 HTTP 请求
- **event**: 事件相关
- **exception**: 异常处理
- **listener**: 监听器
- **mbg**: MyBatis Generator 生成的代码
- **model**: 数据传输对象
- **policy**: 业务策略
- **publisher**: 事件发布者
- **queue**: 队列相关
- **redis**: Redis 相关
- **security**: 安全认证组件
- **service**: 业务逻辑层
- **startup**: 启动相关
- **task**: 定时任务
- **util**: 工具类
- **validator**: 验证器

## 核心功能

### 1. 用户认证系统
- **JWT Token 认证**: 基于 JWT 的无状态认证机制
- **注册接口**: `POST /auth/register` - 用户注册（密码自动 BCrypt 加密）
- **登录接口**: `POST /auth/login` - 用户登录，返回 JWT Token
- **登出接口**: `GET /auth/logout` - 退出登录（前端需配合删除本地 Token）
- **密码加密**: 使用 BCrypt 进行密码加密存储（不可逆）

### 2. 内容管理系统
- **问题管理**: 发布、查看问题
- **答案管理**: 发布、查看答案
- **评论系统**: 对问题和答案进行评论
- **投票系统**: 对内容进行点赞/踩
- **分类系统**: 问题分类管理
- **通知系统**: 用户通知管理
- **订阅系统**: 问题订阅功能

### 3. 数据库管理
- **Flyway 版本控制**: 数据库结构版本化管理
- **多表设计**: 包含用户、问题、答案、评论、投票、分类等表
- **初始化数据**: 预置测试数据

### 4. 测试支持
- **Testcontainers**: 使用 Docker 容器进行集成测试
- **BaseContainerTest**: 集成测试基类，自动启动 MySQL、Kafka、Redis 容器

## 开发约定

### 代码结构
- Controller 层负责处理 HTTP 请求和响应
- Service 层负责业务逻辑处理
- Model 层定义数据传输对象
- MBG (MyBatis Generator) 生成的数据访问层
- Security 模块处理认证和授权

### 测试实践
- 单元测试位于 `src/test/java/com/nofirst/spring/tdd/zhihu/unit`
- 集成测试位于 `src/test/java/com/nofirst/spring/tdd/zhihu/integration`
- 使用 Testcontainers 进行真实环境的集成测试
- 遵循 TDD 实践，先写测试再实现功能

### 数据库迁移
- 使用 Flyway 进行数据库版本控制
- 迁移脚本位于 `src/main/resources/db/migration`
- 命名规范：`V版本号__描述.sql`

## 构建和运行

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Docker（用于运行测试）

### 构建命令
```bash
# 编译项目
mvn clean compile

# 运行测试
mvn test

# 打包项目
mvn clean package

# 运行应用
mvn spring-boot:run

# 运行 Flyway 迁移
mvn flyway:migrate

# 生成 MyBatis 代码
mvn mybatis-generator:generate
```

### 配置说明
- 数据库连接配置在 `application.yaml`
- JWT 密钥和过期时间可通过配置文件设置
- Kafka 和 Redis 配置也在 `application.yaml` 中

## API 规范

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

## 测试策略

### 测试框架
- **JUnit 5** (`org.junit.jupiter.api.*`)
- **AssertJ** (`org.assertj.core.api.Assertions`) - 流畅式断言
- **Mockito** (`org.mockito.*`) - 依赖模拟
- **Spring Test** (`org.springframework.test.*`)
- **Awaitility** - 异步测试等待

### 测试目录结构
```
src/test/java/com/nofirst/spring/tdd/zhihu/
├── integration/    # 集成测试
│   ├── CreateQuestionsTest.java
│   ├── PostAnswersTest.java
│   ├── QuestionUpVotesTest.java
│   └── ...
└── unit/           # 单元测试
    ├── policy/
    │   └── QuestionPolicyTest.java
    ├── service/
    │   └── QuestionServiceImplTest.java
    └── util/
        └── InviteUserUtilTest.java
```

### 测试类命名规范

| 类型 | 命名模式 | 示例 |
|------|----------|------|
| **集成测试** | `*Test` | `CreateQuestionsTest`, `PostAnswersTest` |
| **单元测试** | `*Test` | `QuestionPolicyTest`, `QuestionServiceImplTest` |
| **抽象测试基类** | `Base*` 或 `Abstract*` | `BaseContainerTest`, `AbstractVoteUpTest` |

### 测试方法命名规范

**格式**：`[条件]_[行为]_[预期结果]` 或 `[谁]_[可以/不可以]_[做什么]`

- 全部使用**小写字母 + 下划线分隔**
- 方法名本身是一个完整的测试场景描述

```java
// 示例
void guests_may_not_create_questions()
void an_authenticated_user_can_create_new_questions()
void title_is_required()
void signed_in_user_can_post_an_answer_to_a_published_question()
void can_not_post_an_answer_to_an_unpublished_question()
void a_notification_is_prepared_when_a_subscribed_question_receives_a_new_answer_by_other_people()
```

### 测试结构模式

**标准结构：given-when-then 三段式**

```java
@Test
void an_authenticated_user_can_create_new_questions() throws Exception {
    // given
    QuestionDto questionDto = QuestionFactory.createQuestionDto();
    QuestionExample example = new QuestionExample();
    example.createCriteria();
    long beforeCount = questionMapper.countByExample(example);

    // when
    this.mockMvc.perform(post("/questions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(questionDto)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()));

    // then
    long afterCount = questionMapper.countByExample(example);
    assertThat(afterCount - beforeCount).isEqualTo(1);
}
```

### 测试数据准备

**Factory 模式**：使用专门的工厂类创建测试数据

```java
// QuestionFactory.java
public class QuestionFactory {
    public static Question createPublishedQuestion() { ... }
    public static Question createUnpublishedQuestion() { ... }
    public static QuestionDto createQuestionDto() { ... }
}

// 使用示例
Question question = QuestionFactory.createPublishedQuestion();
Answer answer = AnswerFactory.createAnswer(question.getId());
```

**测试清理**：使用 `@BeforeEach` 清理数据

```java
@BeforeEach
public void setupTestData() {
    QuestionExample example = new QuestionExample();
    example.createCriteria();  // 空条件匹配所有
    questionMapper.deleteByExample(example);
}
```

### 断言方式

```java
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 基本断言
assertThat(afterCount - beforeCount).isEqualTo(1);
assertThat(questionOwner).isTrue();
assertThat(users).isEmpty();

// 异常断言
assertThatThrownBy(() -> {
    questionPolicy.canMarkAnswerAsBest(1, accountUser);
}).isInstanceOf(AnswerNotExistedException.class)
  .hasMessageContaining("answer not exist");

// 集合断言
assertThat(users).containsExactly("Jane", "Foo");
assertThat(votes).size().isEqualTo(1);
```

### 单元测试规范

```java
@ExtendWith(MockitoExtension.class)
class QuestionPolicyTest {

    @InjectMocks
    private QuestionPolicy questionPolicy;

    @Mock
    private AnswerMapper answerMapper;
    
    @Mock
    private QuestionMapper questionMapper;

    @Test
    void can_judge_user_is_question_owner_when_mark_best_answer() {
        // given
        Question publishedQuestion = QuestionFactory.createPublishedQuestion();
        given(questionMapper.selectByPrimaryKey(publishedQuestion.getId()))
            .willReturn(publishedQuestion);
        
        // when & then
        boolean result = questionPolicy.canMarkAnswerAsBest(publishedQuestion.getId(), accountUser);
        assertThat(result).isTrue();
    }
}
```

### 集成测试规范

```java
@SpringBootTest(classes = SpringTddZhihuApplication.class)
@AutoConfigureMockMvc
public abstract class BaseContainerTest {

    @Autowired
    protected MockMvc mockMvc;
}

// 子类继承
class CreateQuestionsTest extends BaseContainerTest {

    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void an_authenticated_user_can_create_new_questions() throws Exception {
        // 测试逻辑
    }
}
```

### Testcontainers 配置

```java
@SpringBootTest(classes = SpringTddZhihuApplication.class)
@AutoConfigureMockMvc
public abstract class BaseContainerTest {

    // 容器定义（静态复用）
    public static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("zhihu")
            .withUsername("root")
            .withPassword("root")
            .withReuse(true);

    public static final KafkaContainer kafkaContainer = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
    ).withReuse(true);

    public static final RedisContainer redisContainer =
            new RedisContainer(DockerImageName.parse("redis:7.2.3"))
                    .withExposedPorts(6379)
                    .withReuse(true);

    static {
        mysqlContainer.start();
        kafkaContainer.start();
        redisContainer.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
    }
}
```

### 异步测试处理 (Awaitility)

```java
@Test
@Tag("online")
@WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
void get_slug_when_create_a_question() throws Exception {
    // ...
    
    await()
        .pollInterval(Duration.ofSeconds(3))
        .atMost(10, SECONDS)
        .untilAsserted(() -> {
            long afterCount = questionMapper.countByExample(questionExample);
            assertThat(afterCount - beforeCount).isEqualTo(1);
        });
}
```

### 测试标签

```java
@Test
@Tag("online")  // 标记需要访问外部服务的测试
void can_translate_chinese_to_english() {
    // 百度翻译接口测试
}
```

### 代码复用模式

**抽象测试类**：定义通用测试逻辑，子类实现资源类型

```java
// 抽象基类
public abstract class AbstractVoteUpTest extends AbstractVoteTest {
    
    @Test
    void guest_can_not_vote_up() { ... }
    
    @Test
    void authenticated_user_can_vote_up() { ... }
    
    // 子类只需实现
    protected abstract String getResourceTypeName();
    protected abstract String getResourcePath();
}

// 具体实现
class QuestionUpVotesTest extends AbstractVoteUpTest {
    @Override
    protected String getResourceTypeName() {
        return Question.class.getSimpleName();
    }
    
    @Override
    protected String getResourcePath() {
        return "questions";
    }
}
```

### JSON 响应解析

```java
TypeReference<CommonResult<PageInfo<QuestionVo>>> typeRef = new TypeReference<>() {};
CommonResult<PageInfo<QuestionVo>> commonResult = objectMapper.readValue(json, typeRef);
```

## 安全措施

- 使用 Spring Security 进行安全控制
- JWT Token 实现无状态认证
- BCrypt 算法加密用户密码
- 防止常见的安全漏洞

## 开发指南

### 添加新功能
1. 先编写测试用例（TDD 原则）
2. 创建对应的数据库迁移脚本
3. 运行 MyBatis Generator 生成代码
4. 实现业务逻辑
5. 运行测试确保通过

### 数据库变更
1. 在 `src/main/resources/db/migration/` 下创建新的 Flyway 迁移脚本
2. 运行 `mvn flyway:migrate` 应用变更
3. 如需生成新的实体类，运行 `mvn mybatis-generator:generate`

### 编写测试
继承 `BaseContainerTest` 类即可使用 Testcontainers 提供的 MySQL、Kafka、Redis 容器：

```java
class YourServiceTest extends BaseContainerTest {
    @Test
    void testSomething() {
        // 测试逻辑
    }
}
```