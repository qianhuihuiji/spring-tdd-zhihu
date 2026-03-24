package com.nofirst.spring.tdd.zhihu.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.CommentMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Comment;
import com.nofirst.spring.tdd.zhihu.mbg.model.CommentExample;
import com.nofirst.spring.tdd.zhihu.model.dto.CommentDto;
import com.nofirst.spring.tdd.zhihu.model.enums.VoteActionType;
import com.nofirst.spring.tdd.zhihu.model.vo.CommentVo;
import com.nofirst.spring.tdd.zhihu.publisher.CustomEventPublisher;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import com.nofirst.spring.tdd.zhihu.service.CommentService;
import com.nofirst.spring.tdd.zhihu.service.GenericVoteService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final GenericVoteService genericVoteService;
    private final CustomEventPublisher customEventPublisher;


    @Override
    public void comment(Integer commentedId, String commentedType, CommentDto commentDto, AccountUser accountUser) {
        Comment comment = new Comment();
        comment.setUserId(accountUser.getUserId());
        comment.setContent(commentDto.getContent());
        comment.setCommentedId(commentedId);
        comment.setCommentedType(commentedType);
        Date date = new Date();
        comment.setCreatedAt(date);
        comment.setUpdatedAt(date);
        commentMapper.insert(comment);

        // 发布评论事件，触发通知等后续处理
        customEventPublisher.firePostCommentEvent(comment);
    }

    @Override
    public PageInfo<CommentVo> index(Integer commentedId, String commentedType, Integer pageIndex, Integer pageSize, AccountUser accountUser) {
        PageHelper.startPage(pageIndex, pageSize);
        CommentExample commentExample = new CommentExample();
        CommentExample.Criteria criteria = commentExample.createCriteria();
        criteria.andCommentedIdEqualTo(commentedId);
        criteria.andCommentedTypeEqualTo(commentedType);
        List<Comment> comments = commentMapper.selectByExample(commentExample);
        PageInfo<Comment> commentPageInfo = new PageInfo<>(comments);
        List<CommentVo> result = new ArrayList<>();
        for (Comment comment : comments) {
            CommentVo commentVo = new CommentVo();
            commentVo.setId(comment.getId());
            commentVo.setCommentedId(comment.getCommentedId());
            commentVo.setContent(comment.getContent());
            commentVo.setCreateTime(comment.getCreatedAt());

            result.add(commentVo);
        }

        appendVoteType(result, accountUser.getUserId());
        appendVoteCount(result);

        PageInfo<CommentVo> pageResult = new PageInfo<>();
        pageResult.setTotal(commentPageInfo.getTotal());
        pageResult.setPageNum(commentPageInfo.getPageNum());
        pageResult.setPageSize(commentPageInfo.getPageSize());
        pageResult.setList(result);
        return pageResult;
    }


    private void appendVoteCount(List<CommentVo> result) {
        if (CollectionUtils.isEmpty(result)) {
            return;
        }

        genericVoteService.setVoteCounts(result, Comment.class, VoteActionType.VOTE_UP, CommentVo::getId);
        genericVoteService.setVoteCounts(result, Comment.class, VoteActionType.VOTE_DOWN, CommentVo::getId);
    }

    private void appendVoteType(List<CommentVo> result, Integer userId) {
        genericVoteService.setUserVoteTypes(result, Comment.class, userId, CommentVo::getId);
    }
}
