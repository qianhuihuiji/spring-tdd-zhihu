package com.nofirst.spring.tdd.zhihu.controller;

import com.github.pagehelper.PageInfo;
import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.nofirst.spring.tdd.zhihu.model.dto.QuestionDto;
import com.nofirst.spring.tdd.zhihu.model.vo.QuestionVo;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import com.nofirst.spring.tdd.zhihu.service.QuestionService;
import com.nofirst.spring.tdd.zhihu.validator.ValidCategory;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "问题管理")
@RestController
@AllArgsConstructor
@Validated
public class QuestionController {

    private QuestionService questionService;

    @Operation(summary = "获取问题列表")
    @GetMapping("/questions")
    public CommonResult<PageInfo<QuestionVo>> index(@AuthenticationPrincipal AccountUser accountUser,
                                                    @RequestParam @NotNull Integer pageIndex,
                                                    @RequestParam @NotNull Integer pageSize,
                                                    @RequestParam(required = false) String slug,
                                                    @RequestParam(required = false) String by,
                                                    @RequestParam(required = false) Integer popularity,
                                                    @RequestParam(required = false) Integer unanswered) {
        PageInfo<QuestionVo> questionPage = questionService.index(accountUser, pageIndex, pageSize, slug, by, popularity, unanswered);
        return CommonResult.success(questionPage);
    }

    @Operation(summary = "创建问题")
    @PostMapping("/questions")
    public CommonResult<String> store(@RequestBody @ValidCategory @Validated QuestionDto dto, @AuthenticationPrincipal AccountUser accountUser) {
        questionService.store(dto, accountUser);
        return CommonResult.success("ok");
    }

    @Operation(summary = "查看问题详情")
    @GetMapping(value = {
            "/questions/{id}",
            "/questions/{id}/{slug:.*}"
    })
    public CommonResult<QuestionVo> show(@PathVariable Integer id,
                                         @PathVariable(required = false) String slug,
                                         @AuthenticationPrincipal AccountUser accountUser) {
        return CommonResult.success(questionService.show(id, accountUser));
    }
}