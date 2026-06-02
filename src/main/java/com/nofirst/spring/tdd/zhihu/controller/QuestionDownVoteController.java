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

@Tag(name = "问题点踩")
@RestController
@AllArgsConstructor
public class QuestionDownVoteController {

    private GenericVoteService genericVoteService;

    @Operation(summary = "点踩问题")
    @PostMapping("/questions/{questionId}/down-votes")
    public CommonResult<String> store(@PathVariable Integer questionId, @AuthenticationPrincipal AccountUser accountUser) {
        genericVoteService.voteDown(Question.class.getSimpleName(), questionId, accountUser);
        return CommonResult.success("ok");
    }

    @Operation(summary = "取消点踩问题")
    @DeleteMapping("/questions/{questionId}/down-votes")
    public CommonResult<String> destroy(@PathVariable Integer questionId, @AuthenticationPrincipal AccountUser accountUser) {
        genericVoteService.cancelVoteDown(Question.class.getSimpleName(), questionId, accountUser);
        return CommonResult.success("ok");
    }
}
