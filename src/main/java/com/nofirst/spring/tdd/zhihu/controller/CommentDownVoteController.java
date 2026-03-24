package com.nofirst.spring.tdd.zhihu.controller;

import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.mbg.model.Comment;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import com.nofirst.spring.tdd.zhihu.service.GenericVoteService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class CommentDownVoteController {

    private GenericVoteService genericVoteService;

    @PostMapping("/comments/{commentId}/down-votes")
    public CommonResult<String> store(@PathVariable Integer commentId, @AuthenticationPrincipal AccountUser accountUser) {
        genericVoteService.voteDown(Comment.class.getSimpleName(), commentId, accountUser);
        return CommonResult.success("ok");
    }

    @DeleteMapping("/comments/{commentId}/down-votes")
    public CommonResult<String> destroy(@PathVariable Integer commentId, @AuthenticationPrincipal AccountUser accountUser) {
        genericVoteService.cancelVoteDown(Comment.class.getSimpleName(), commentId, accountUser);
        return CommonResult.success("ok");
    }
}