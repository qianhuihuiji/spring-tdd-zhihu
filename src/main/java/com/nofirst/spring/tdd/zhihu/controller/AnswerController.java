package com.nofirst.spring.tdd.zhihu.controller;

import com.github.pagehelper.PageInfo;
import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.model.dto.AnswerDto;
import com.nofirst.spring.tdd.zhihu.model.vo.AnswerVo;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import com.nofirst.spring.tdd.zhihu.service.AnswerService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AnswerController {

    private AnswerService answerService;

    @GetMapping("/questions/{questionId}/answers")
    public CommonResult<PageInfo<AnswerVo>> index(@PathVariable Integer questionId,
                                                  @RequestParam Integer pageIndex,
                                                  @RequestParam Integer pageSize,
                                                  @AuthenticationPrincipal AccountUser accountUser) {
        PageInfo<AnswerVo> answerPage = answerService.answers(questionId, pageIndex, pageSize, accountUser);
        return CommonResult.success(answerPage);
    }

    @PostMapping("/questions/{questionId}/answers")
    public CommonResult<String> store(@PathVariable Integer questionId,
                                      @RequestBody @Validated AnswerDto answerDto,
                                      @AuthenticationPrincipal AccountUser accountUser) {
        answerService.store(questionId, answerDto, accountUser);
        return CommonResult.success("success");
    }

    @DeleteMapping("/answers/{answerId}")
    @PreAuthorize("@answerPolicy.canDelete(#answerId, #accountUser)")
    public CommonResult<String> store(@PathVariable Integer answerId, @AuthenticationPrincipal AccountUser accountUser) {
        answerService.destroy(answerId);
        return CommonResult.success("ok");
    }
}