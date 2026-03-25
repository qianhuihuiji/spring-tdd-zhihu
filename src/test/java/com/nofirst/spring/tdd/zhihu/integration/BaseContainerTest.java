package com.nofirst.spring.tdd.zhihu.integration;

import com.nofirst.spring.tdd.zhihu.SpringTddZhihuApplication;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.ActivityMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.AnswerMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.CommentMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.NotificationMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.SubscriptionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.VoteMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.ActivityExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.AnswerExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.CommentExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.NotificationExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.QuestionExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.SubscriptionExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.UserExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.VoteExample;
import com.redis.testcontainers.RedisContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.TestcontainersConfiguration;

@SpringBootTest(classes = SpringTddZhihuApplication.class)
@AutoConfigureMockMvc
public abstract class BaseContainerTest {

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
        // 复用配置
        TestcontainersConfiguration.getInstance().updateUserConfig("testcontainers.reuse.enable", "true");
        // 启动容器，确保端口提前分配
        mysqlContainer.start();
        kafkaContainer.start();
        redisContainer.start();
    }

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    private QuestionMapper questionMapper;
    @Autowired
    private AnswerMapper answerMapper;
    @Autowired
    private VoteMapper voteMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private SubscriptionMapper subscriptionMapper;
    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private ActivityMapper activityMapper;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);

        // 将 Testcontainers 启动的 Kafka 容器的实际连接地址，动态覆盖到 Spring 上下文的 spring.kafka.bootstrap-servers 配置项中
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);

        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
    }

    protected void cleanUpQuestions() {
        QuestionExample example = new QuestionExample();
        example.createCriteria();
        questionMapper.deleteByExample(example);
    }

    protected void cleanUpAnswers() {
        AnswerExample example = new AnswerExample();
        example.createCriteria();
        answerMapper.deleteByExample(example);
    }

    protected void cleanUpVotes() {
        VoteExample voteExample = new VoteExample();
        voteExample.createCriteria();
        voteMapper.deleteByExample(voteExample);
    }

    protected void cleanUpComments() {
        CommentExample commentExample = new CommentExample();
        commentExample.createCriteria();
        commentMapper.deleteByExample(commentExample);
    }

    protected void cleanUpSubscriptions() {
        SubscriptionExample subscriptionExample = new SubscriptionExample();
        subscriptionExample.createCriteria();
        subscriptionMapper.deleteByExample(subscriptionExample);
    }

    protected void cleanUpNotifications() {
        NotificationExample notificationExample = new NotificationExample();
        notificationExample.createCriteria();
        notificationMapper.deleteByExample(notificationExample);
    }

    protected void cleanUpUsersExceptDefault() {
        // 只删除测试创建的用户（id > 3），保留初始化的 3 个用户（Jane、John、Foo）
        UserExample example = new UserExample();
        example.createCriteria().andIdGreaterThan(3);
        userMapper.deleteByExample(example);
    }

    protected void cleanUpActivities() {
        ActivityExample example = new ActivityExample();
        example.createCriteria();
        activityMapper.deleteByExample(example);
    }


}
