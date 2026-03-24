package com.nofirst.spring.tdd.zhihu.controller;

import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.mbg.model.Answer;
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
public class AnswerDownVoteController {

    private GenericVoteService genericVoteService;

    @PostMapping("/answers/{answerId}/down-votes")
    public CommonResult<String> store(@PathVariable Integer answerId, @AuthenticationPrincipal AccountUser accountUser) {
        genericVoteService.voteDown(Answer.class.getSimpleName(), answerId, accountUser);
        return CommonResult.success("ok");
    }

    @DeleteMapping("/answers/{answerId}/down-votes")
    public CommonResult<String> destroy(@PathVariable Integer answerId, @AuthenticationPrincipal AccountUser accountUser) {
        genericVoteService.cancelVoteDown(Answer.class.getSimpleName(), answerId, accountUser);
        return CommonResult.success("ok");
    }
}