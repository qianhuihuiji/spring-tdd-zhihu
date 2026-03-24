package com.nofirst.spring.tdd.zhihu.controller;

import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import com.nofirst.spring.tdd.zhihu.service.AnswerService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class BestAnswerController {

    private final AnswerService answerService;

    @PostMapping("/answers/{answerId}/best")
    @PreAuthorize("@questionPolicy.canMarkAnswerAsBest(#answerId, #accountUser)") // 注意此处
    public CommonResult<String> store(@PathVariable Integer answerId,
                                      @AuthenticationPrincipal AccountUser accountUser) {
        answerService.markAsBest(answerId);
        return CommonResult.success("ok");
    }
}