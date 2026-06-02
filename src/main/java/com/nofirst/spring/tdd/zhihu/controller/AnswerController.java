package com.nofirst.spring.tdd.zhihu.controller;

import com.github.pagehelper.PageInfo;
import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "回答管理")
@RestController
@AllArgsConstructor
public class AnswerController {

    private AnswerService answerService;

    @Operation(summary = "获取回答列表")
    @GetMapping("/questions/{questionId}/answers")
    public CommonResult<PageInfo<AnswerVo>> index(@PathVariable Integer questionId,
                                                  @RequestParam Integer pageIndex,
                                                  @RequestParam Integer pageSize,
                                                  @AuthenticationPrincipal AccountUser accountUser) {
        PageInfo<AnswerVo> answerPage = answerService.answers(questionId, pageIndex, pageSize, accountUser);
        return CommonResult.success(answerPage);
    }

    @Operation(summary = "创建回答")
    @PostMapping("/questions/{questionId}/answers")
    public CommonResult<String> store(@PathVariable Integer questionId,
                                      @RequestBody @Validated AnswerDto answerDto,
                                      @AuthenticationPrincipal AccountUser accountUser) {
        answerService.store(questionId, answerDto, accountUser);
        return CommonResult.success("success");
    }

    @Operation(summary = "删除回答")
    @DeleteMapping("/answers/{answerId}")
    @PreAuthorize("@answerPolicy.canDelete(#answerId, #accountUser)")
    public CommonResult<String> destroy(@PathVariable Integer answerId, @AuthenticationPrincipal AccountUser accountUser) {
        answerService.destroy(answerId);
        return CommonResult.success("ok");
    }
}