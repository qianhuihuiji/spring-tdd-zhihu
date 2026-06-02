package com.nofirst.spring.tdd.zhihu.controller;

import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
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

@Tag(name = "问题点赞")
@RestController
@AllArgsConstructor
public class QuestionUpVoteController {

    private GenericVoteService genericVoteService;

    @Operation(summary = "点赞问题")
    @PostMapping("/questions/{questionId}/up-votes")
    public CommonResult<String> store(@PathVariable Integer questionId, @AuthenticationPrincipal AccountUser accountUser) {
        genericVoteService.voteUp(Question.class.getSimpleName(), questionId, accountUser);
        return CommonResult.success("ok");
    }

    @Operation(summary = "取消点赞问题")
    @DeleteMapping("/questions/{questionId}/up-votes")
    public CommonResult<String> destroy(@PathVariable Integer questionId, @AuthenticationPrincipal AccountUser accountUser) {
        genericVoteService.cancelVoteUp(Question.class.getSimpleName(), questionId, accountUser);
        return CommonResult.success("ok");
    }
}