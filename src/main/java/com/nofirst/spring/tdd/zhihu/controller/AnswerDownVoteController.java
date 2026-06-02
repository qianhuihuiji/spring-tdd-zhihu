package com.nofirst.spring.tdd.zhihu.controller;

import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.mbg.model.Answer;
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

@Tag(name = "回答点踩")
@RestController
@AllArgsConstructor
public class AnswerDownVoteController {

    private GenericVoteService genericVoteService;

    @Operation(summary = "点踩回答")
    @PostMapping("/answers/{answerId}/down-votes")
    public CommonResult<String> store(@PathVariable Integer answerId, @AuthenticationPrincipal AccountUser accountUser) {
        genericVoteService.voteDown(Answer.class.getSimpleName(), answerId, accountUser);
        return CommonResult.success("ok");
    }

    @Operation(summary = "取消点踩回答")
    @DeleteMapping("/answers/{answerId}/down-votes")
    public CommonResult<String> destroy(@PathVariable Integer answerId, @AuthenticationPrincipal AccountUser accountUser) {
        genericVoteService.cancelVoteDown(Answer.class.getSimpleName(), answerId, accountUser);
        return CommonResult.success("ok");
    }
}
