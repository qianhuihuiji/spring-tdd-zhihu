package com.nofirst.spring.tdd.zhihu.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nofirst.spring.tdd.zhihu.exception.QuestionNotExistedException;
import com.nofirst.spring.tdd.zhihu.exception.QuestionNotPublishedException;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.AnswerMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.QuestionMapperExt;
import com.nofirst.spring.tdd.zhihu.mbg.model.Answer;
import com.nofirst.spring.tdd.zhihu.mbg.model.AnswerExample;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import com.nofirst.spring.tdd.zhihu.model.dto.AnswerDto;
import com.nofirst.spring.tdd.zhihu.model.enums.VoteActionType;
import com.nofirst.spring.tdd.zhihu.model.vo.AnswerVo;
import com.nofirst.spring.tdd.zhihu.publisher.CustomEventPublisher;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import com.nofirst.spring.tdd.zhihu.service.AnswerService;
import com.nofirst.spring.tdd.zhihu.service.GenericVoteService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final AnswerMapper answerMapper;
    private final QuestionMapper questionMapper;
    private final QuestionMapperExt questionMapperExt;
    private final GenericVoteService genericVoteService;

    private final CustomEventPublisher customEventPublisher;

    @Override
    public PageInfo<AnswerVo> answers(Integer questionId, int pageIndex, int pageSize, AccountUser accountUser) {
        PageHelper.startPage(pageIndex, pageSize);
        AnswerExample example = new AnswerExample();
        example.createCriteria().andQuestionIdEqualTo(questionId);
        List<Answer> answers = answerMapper.selectByExample(example);
        PageInfo<Answer> answerPageInfo = new PageInfo<>(answers);
        List<AnswerVo> result = new ArrayList<>();
        for (Answer answer : answers) {
            AnswerVo vo = new AnswerVo();
            vo.setId(answer.getId());
            vo.setQuestionId(answer.getQuestionId());
            vo.setUserId(answer.getUserId());
            vo.setCreatedAt(answer.getCreatedAt());
            vo.setUpdatedAt(answer.getUpdatedAt());
            vo.setContent(answer.getContent());
            result.add(vo);
        }
        appendVoteType(result, accountUser.getUserId());
        appendVoteCount(result);

        PageInfo<AnswerVo> pageResult = new PageInfo<>();
        pageResult.setTotal(answerPageInfo.getTotal());
        pageResult.setPageNum(answerPageInfo.getPageNum());
        pageResult.setPageSize(answerPageInfo.getPageSize());
        pageResult.setSize(answerPageInfo.getSize());
        pageResult.setList(result);

        return pageResult;
    }

    private void appendVoteCount(List<AnswerVo> result) {
        if (CollectionUtils.isEmpty(result)) {
            return;
        }

        genericVoteService.setVoteCounts(result, Answer.class, VoteActionType.VOTE_UP, AnswerVo::getId);
        genericVoteService.setVoteCounts(result, Answer.class, VoteActionType.VOTE_DOWN, AnswerVo::getId);
    }

    private void appendVoteType(List<AnswerVo> result, Integer userId) {
        genericVoteService.setUserVoteTypes(result, Answer.class, userId, AnswerVo::getId);
    }

    public void store(Integer questionId, AnswerDto answerDto, AccountUser accountUser) {
        Question question = questionMapper.selectByPrimaryKey(questionId);
        if (Objects.isNull(question)) {
            throw new QuestionNotExistedException();
        }
        if (Objects.isNull(question.getPublishedAt())) {
            throw new QuestionNotPublishedException();
        }
        Date now = new Date();
        Answer answer = new Answer();
        answer.setQuestionId(questionId);
        answer.setUserId(accountUser.getUserId());
        answer.setCreatedAt(now);
        answer.setUpdatedAt(now);
        answer.setContent(answerDto.getContent());
        answerMapper.insert(answer);

        Question updateQuestion = new Question();
        updateQuestion.setId(question.getId());
        updateQuestion.setAnswersCount(question.getAnswersCount() + 1);
        updateQuestion.setUpdatedAt(now);
        questionMapper.updateByPrimaryKeySelective(updateQuestion);

        customEventPublisher.firePostAnswerEvent(answer, accountUser.getUserId());
    }

    @Override
    public void markAsBest(Integer answerId) {
        Answer answer = answerMapper.selectByPrimaryKey(answerId);
        questionMapperExt.markAsBestAnswer(answer.getQuestionId(), answer.getId());
    }

    @Override
    public void destroy(Integer answerId) {
        Answer answer = answerMapper.selectByPrimaryKey(answerId);
        Question question = questionMapper.selectByPrimaryKey(answer.getQuestionId());

        Question updateQuestion = new Question();
        updateQuestion.setId(question.getId());
        updateQuestion.setAnswersCount(question.getAnswersCount() - 1);
        updateQuestion.setUpdatedAt(new Date());
        questionMapper.updateByPrimaryKeySelective(updateQuestion);

        answerMapper.deleteByPrimaryKey(answerId);
    }
}