package com.nofirst.spring.tdd.zhihu.service;

import com.nofirst.spring.tdd.zhihu.model.enums.VoteActionType;
import com.nofirst.spring.tdd.zhihu.model.vo.BaseVoteVo;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;

import java.util.List;
import java.util.function.Function;

public interface GenericVoteService {

    void voteUp(String resourceType, Integer resourceId, AccountUser accountUser);

    void cancelVoteUp(String resourceType, Integer resourceId, AccountUser accountUser);

    void voteDown(String resourceType, Integer resourceId, AccountUser accountUser);

    void cancelVoteDown(String resourceType, Integer resourceId, AccountUser accountUser);

    /**
     * 为 VO 列表设置投票数量
     *
     * @param vos            VO 列表
     * @param resourceClass  资源类
     * @param voteActionType 投票动作类型
     * @param idExtractor    ID 提取函数
     */
    <T extends BaseVoteVo> void setVoteCounts(
            List<T> vos,
            Class<?> resourceClass,
            VoteActionType voteActionType,
            Function<T, Integer> idExtractor);

    /**
     * 为 VO 列表设置用户投票类型
     *
     * @param vos           VO 列表
     * @param resourceClass 资源类
     * @param userId        用户ID
     * @param idExtractor   ID 提取函数
     */
    <T extends BaseVoteVo> void setUserVoteTypes(
            List<T> vos,
            Class<?> resourceClass,
            Integer userId,
            Function<T, Integer> idExtractor);
}