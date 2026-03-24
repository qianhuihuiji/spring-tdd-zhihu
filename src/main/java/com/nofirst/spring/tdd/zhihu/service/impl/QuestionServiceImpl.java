package com.nofirst.spring.tdd.zhihu.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nofirst.spring.tdd.zhihu.exception.QuestionNotExistedException;
import com.nofirst.spring.tdd.zhihu.exception.QuestionNotPublishedException;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.CategoryMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Category;
import com.nofirst.spring.tdd.zhihu.mbg.model.CategoryExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.mbg.model.QuestionExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import com.nofirst.spring.tdd.zhihu.mbg.model.UserExample;
import com.nofirst.spring.tdd.zhihu.model.dto.QuestionDto;
import com.nofirst.spring.tdd.zhihu.model.enums.VoteActionType;
import com.nofirst.spring.tdd.zhihu.model.vo.QuestionVo;
import com.nofirst.spring.tdd.zhihu.publisher.CustomEventPublisher;
import com.nofirst.spring.tdd.zhihu.queue.producer.CustomKafkaProducer;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import com.nofirst.spring.tdd.zhihu.service.AnswerService;
import com.nofirst.spring.tdd.zhihu.service.GenericVoteService;
import com.nofirst.spring.tdd.zhihu.service.QuestionService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final AnswerService answerService;
    private final CustomEventPublisher customEventPublisher;
    private final GenericVoteService genericVoteService;
    private final CustomKafkaProducer customKafkaProducer;

    @Override
    public PageInfo<QuestionVo> index(AccountUser accountUser, Integer pageIndex, Integer pageSize, String slug, String by, Integer popularity, Integer unanswered) {
        QuestionExample example = new QuestionExample();
        QuestionExample.Criteria criteria = example.createCriteria();
        criteria.andPublishedAtIsNotNull();
        if (StringUtils.isNotBlank(slug)) {
            slug(criteria, slug);
        }
        if (StringUtils.isNotBlank(by)) {
            by(criteria, by);
        }
        if (Objects.nonNull(popularity) && popularity.equals(1)) {
            popularity(example);
        }

        if (Objects.nonNull(unanswered) && unanswered.equals(1)) {
            unanswered(criteria);
        }

        PageHelper.startPage(pageIndex, pageSize);
        List<Question> questions = questionMapper.selectByExample(example);
        // 如果不使用 mapper 返回的对象直接构造分页对象，total会被错误赋值成当前页的数据的数量，而不是总数
        PageInfo<Question> questionPageInfo = new PageInfo<>(questions);
        List<QuestionVo> result = new ArrayList<>();
        for (Question question : questions) {
            QuestionVo questionVo = new QuestionVo();
            questionVo.setId(question.getId());
            questionVo.setUserId(question.getUserId());
            questionVo.setTitle(question.getTitle());
            questionVo.setContent(question.getContent());
            questionVo.setAnswersCount(question.getAnswersCount());
            result.add(questionVo);
        }

        if (Objects.nonNull(accountUser)) {
            appendVoteType(result, accountUser.getUserId());
        }
        appendVoteCount(result);

        PageInfo<QuestionVo> pageResult = new PageInfo<>();
        pageResult.setTotal(questionPageInfo.getTotal());
        pageResult.setPageNum(questionPageInfo.getPageNum());
        pageResult.setPageSize(questionPageInfo.getPageSize());
        pageResult.setList(result);
        return pageResult;
    }


    private void appendVoteCount(List<QuestionVo> result) {
        if (CollectionUtils.isEmpty(result)) {
            return;
        }

        genericVoteService.setVoteCounts(result, Question.class, VoteActionType.VOTE_UP, QuestionVo::getId);
        genericVoteService.setVoteCounts(result, Question.class, VoteActionType.VOTE_DOWN, QuestionVo::getId);
    }

    private void appendVoteType(List<QuestionVo> result, Integer userId) {
        genericVoteService.setUserVoteTypes(result, Question.class, userId, QuestionVo::getId);
    }

    private void unanswered(QuestionExample.Criteria criteria) {
        criteria.andAnswersCountEqualTo(0);
    }

    private void popularity(QuestionExample example) {
        example.setOrderByClause("answers_count desc");
    }

    private void by(QuestionExample.Criteria criteria, String username) {
        UserExample userExample = new UserExample();
        userExample.createCriteria().andNameEqualTo(username);
        List<User> users = userMapper.selectByExample(userExample);
        if (!CollectionUtils.isEmpty(users)) {
            User user = users.get(0);
            criteria.andUserIdEqualTo(user.getId());
        }
    }

    private void slug(QuestionExample.Criteria criteria, String slug) {
        CategoryExample categoryExample = new CategoryExample();
        categoryExample.createCriteria().andSlugEqualTo(slug);
        List<Category> categories = categoryMapper.selectByExample(categoryExample);
        if (!CollectionUtils.isEmpty(categories)) {
            Category category = categories.get(0);
            criteria.andCategoryIdEqualTo(category.getId());
        }
    }


    @Override
    public QuestionVo show(Integer id, AccountUser accountUser) {
        Question question = questionMapper.selectByPrimaryKey(id);
        if (Objects.isNull(question)) {
            throw new QuestionNotExistedException();
        }
        if (Objects.isNull(question.getPublishedAt())) {
            throw new QuestionNotPublishedException();
        }

        QuestionVo questionVo = new QuestionVo();
        questionVo.setId(question.getId());
        questionVo.setUserId(question.getUserId());
        questionVo.setTitle(question.getTitle());
        questionVo.setContent(question.getContent());
        questionVo.setAnswers(answerService.answers(question.getId(), 1, 20, accountUser)); // 此处表示，首次显示问题列表的第一页，每页20个

        return questionVo;
    }

    @Override
    public void store(QuestionDto dto, AccountUser accountUser) {
        Date now = new Date();
        Question question = new Question();
        question.setUserId(accountUser.getUserId());
        question.setTitle(dto.getTitle());
        question.setContent(dto.getContent());
        question.setCategoryId(dto.getCategoryId());
        question.setCreatedAt(now);
        question.setUpdatedAt(now);
        question.setAnswersCount(0);

        questionMapper.insert(question);
        customKafkaProducer.sendTranslateEvent(question);
    }

    @Override
    public void publish(Integer questionId) {
        Question question = new Question();
        Date now = new Date();
        question.setId(questionId);
        question.setUpdatedAt(now);
        question.setPublishedAt(now);
        questionMapper.updateByPrimaryKeySelective(question);

        Question publishedQuestion = questionMapper.selectByPrimaryKey(questionId);
        customEventPublisher.firePublishQuestionEvent(publishedQuestion);
    }
}