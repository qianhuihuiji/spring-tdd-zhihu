package com.nofirst.spring.tdd.zhihu.controller;

import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.mbg.model.Comment;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import com.nofirst.spring.tdd.zhihu.service.GenericVoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "评论点踩")
@RestController
@AllArgsConstructor
public class CommentDownVoteController {

    private GenericVoteService genericVoteService;

    @Operation(summary = "点踩评论")
    @PostMapping("/comments/{commentId}/down-votes")
    public CommonResult<String> store(@PathVariable Integer commentId, @AuthenticationPrincipal AccountUser accountUser) {
        genericVoteService.voteDown(Comment.class.getSimpleName(), commentId, accountUser);
        return CommonResult.success("ok");
    }

    @Operation(summary = "取消点踩评论")
    @DeleteMapping("/comments/{commentId}/down-votes")
    public CommonResult<String> destroy(@PathVariable Integer commentId, @AuthenticationPrincipal AccountUser accountUser) {
        genericVoteService.cancelVoteDown(Comment.class.getSimpleName(), commentId, accountUser);
        return CommonResult.success("ok");
    }
}
