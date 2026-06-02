package com.nofirst.spring.tdd.zhihu.controller;

import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import com.nofirst.spring.tdd.zhihu.service.AnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "最佳回答")
@RestController
@AllArgsConstructor
public class BestAnswerController {

    private final AnswerService answerService;

    @Operation(summary = "标记最佳回答")
    @PostMapping("/answers/{answerId}/best")
    @PreAuthorize("@questionPolicy.canMarkAnswerAsBest(#answerId, #accountUser)")
    public CommonResult<String> store(@PathVariable Integer answerId,
                                      @AuthenticationPrincipal AccountUser accountUser) {
        answerService.markAsBest(answerId);
        return CommonResult.success("ok");
    }
}
