package com.nofirst.spring.tdd.zhihu.service.impl;

import com.nofirst.spring.tdd.zhihu.mbg.mapper.VoteMapper;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.VoteMapperExt;
import com.nofirst.spring.tdd.zhihu.mbg.model.Vote;
import com.nofirst.spring.tdd.zhihu.mbg.model.VoteExample;
import com.nofirst.spring.tdd.zhihu.model.dto.VoteCountDto;
import com.nofirst.spring.tdd.zhihu.model.enums.VoteActionType;
import com.nofirst.spring.tdd.zhihu.model.vo.BaseVoteVo;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import com.nofirst.spring.tdd.zhihu.service.GenericVoteService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GenericVoteServiceImpl implements GenericVoteService {

    private VoteMapper voteMapper;
    private VoteMapperExt voteMapperExt;

    @Override
    public void voteUp(String resourceType, Integer resourceId, AccountUser accountUser) {
        store(resourceType, resourceId, accountUser, VoteActionType.VOTE_UP);
    }

    @Override
    public void cancelVoteUp(String resourceType, Integer resourceId, AccountUser accountUser) {
        destroy(resourceType, resourceId, accountUser, VoteActionType.VOTE_UP);
    }

    @Override
    public void voteDown(String resourceType, Integer resourceId, AccountUser accountUser) {
        store(resourceType, resourceId, accountUser, VoteActionType.VOTE_DOWN);
    }

    @Override
    public void cancelVoteDown(String resourceType, Integer resourceId, AccountUser accountUser) {
        destroy(resourceType, resourceId, accountUser, VoteActionType.VOTE_DOWN);
    }

    @Override
    public <T extends BaseVoteVo> void setVoteCounts(
            List<T> vos,
            Class<?> resourceClass,
            VoteActionType voteActionType,
            Function<T, Integer> idExtractor) {

        if (CollectionUtils.isEmpty(vos)) {
            return;
        }

        List<Integer> resourceIds = vos.stream().map(idExtractor).toList();
        List<VoteCountDto> voteCounts = voteMapperExt.countByResource(
                resourceClass.getSimpleName(),
                voteActionType.getCode(),
                resourceIds);

        if (CollectionUtils.isEmpty(voteCounts)) {
            vos.forEach(vo -> setVoteCountByType(vo, voteActionType, 0));
            return;
        }

        Map<Integer, Integer> voteCountMap = voteCounts.stream()
                .collect(Collectors.toMap(VoteCountDto::getResourceId, VoteCountDto::getVoteCount));

        vos.forEach(vo -> {
            Integer id = idExtractor.apply(vo);
            Integer count = voteCountMap.getOrDefault(id, 0);
            setVoteCountByType(vo, voteActionType, count);
        });
    }

    @Override
    public <T extends BaseVoteVo> void setUserVoteTypes(
            List<T> vos,
            Class<?> resourceClass,
            Integer userId,
            Function<T, Integer> idExtractor) {

        if (CollectionUtils.isEmpty(vos) || userId == null) {
            return;
        }

        List<Integer> resourceIds = vos.stream().map(idExtractor).toList();

        VoteExample voteExample = new VoteExample();
        voteExample.createCriteria()
                .andResourceIdIn(resourceIds)
                .andUserIdEqualTo(userId)
                .andResourceTypeEqualTo(resourceClass.getSimpleName());
        List<Vote> votes = voteMapper.selectByExample(voteExample);

        if (CollectionUtils.isEmpty(votes)) {
            vos.forEach(vo -> vo.setVoteType(VoteActionType.NOTHING.getCode()));
            return;
        }

        Map<Integer, Byte> voteTypeMap = votes.stream()
                .collect(Collectors.toMap(Vote::getResourceId, Vote::getActionType));

        vos.forEach(vo -> {
            Integer id = idExtractor.apply(vo);
            if (voteTypeMap.containsKey(id)) {
                vo.setVoteType(voteTypeMap.get(id));
            } else {
                vo.setVoteType(VoteActionType.NOTHING.getCode());
            }
        });
    }

    /**
     * 根据投票类型设置投票数量
     */
    private <T extends BaseVoteVo> void setVoteCountByType(T vo, VoteActionType voteActionType, int count) {
        switch (voteActionType) {
            case VOTE_UP -> vo.setVoteUpCount(count);
            case VOTE_DOWN -> vo.setVoteDownCount(count);
            default -> {}
        }
    }

    private void store(String resourceType, Integer resourceId, AccountUser accountUser, VoteActionType actionType) {
        VoteExample voteExample = new VoteExample();
        VoteExample.Criteria criteria = voteExample.createCriteria();
        criteria.andUserIdEqualTo(accountUser.getUserId());
        criteria.andResourceIdEqualTo(resourceId);
        criteria.andResourceTypeEqualTo(resourceType);
        long count = voteMapper.countByExample(voteExample);

        Date now = new Date();
        Vote vote = new Vote();
        if (count == 0) {
            vote.setUserId(accountUser.getUserId());
            vote.setResourceId(resourceId);
            vote.setResourceType(resourceType);
            vote.setActionType(actionType.getCode());

            vote.setCreatedAt(now);
            vote.setUpdatedAt(now);

            voteMapper.insert(vote);
        } else {
            vote.setActionType(actionType.getCode());
            vote.setUpdatedAt(now);
            voteMapper.updateByExampleSelective(vote, voteExample);
        }
    }

    private void destroy(String resourceType, Integer resourceId, AccountUser accountUser, VoteActionType actionType) {
        VoteExample voteExample = new VoteExample();
        VoteExample.Criteria criteria = voteExample.createCriteria();
        criteria.andResourceIdEqualTo(resourceId);
        criteria.andResourceTypeEqualTo(resourceType);
        criteria.andActionTypeEqualTo(actionType.getCode());
        criteria.andUserIdEqualTo(accountUser.getUserId());
        voteMapper.deleteByExample(voteExample);
    }
}