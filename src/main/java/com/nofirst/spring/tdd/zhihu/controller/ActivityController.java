package com.nofirst.spring.tdd.zhihu.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.ActivityMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Activity;
import com.nofirst.spring.tdd.zhihu.mbg.model.ActivityExample;
import com.nofirst.spring.tdd.zhihu.model.vo.ActivityVo;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "用户动态")
@RestController
@Validated
@AllArgsConstructor
public class ActivityController {

    private final ActivityMapper activityMapper;

    @Operation(summary = "获取当前用户活动列表")
    @GetMapping("/activities")
    public CommonResult<PageInfo<ActivityVo>> index(@RequestParam @NotNull Integer pageIndex,
                                                     @RequestParam @NotNull Integer pageSize,
                                                     @AuthenticationPrincipal AccountUser accountUser) {
        PageHelper.startPage(pageIndex, pageSize);

        ActivityExample example = new ActivityExample();
        example.createCriteria().andUserIdEqualTo(accountUser.getUserId());
        example.setOrderByClause("created_at desc");

        List<Activity> activities = activityMapper.selectByExample(example);
        PageInfo<Activity> activityPageInfo = new PageInfo<>(activities);

        List<ActivityVo> result = new ArrayList<>();
        for (Activity activity : activities) {
            ActivityVo vo = new ActivityVo();
            vo.setId(activity.getId());
            vo.setUserId(activity.getUserId());
            vo.setType(activity.getType());
            vo.setSubjectId(activity.getSubjectId());
            vo.setSubjectType(activity.getSubjectType());
            vo.setCreatedAt(activity.getCreatedAt());
            result.add(vo);
        }

        PageInfo<ActivityVo> pageResult = new PageInfo<>();
        pageResult.setTotal(activityPageInfo.getTotal());
        pageResult.setPageNum(activityPageInfo.getPageNum());
        pageResult.setPageSize(activityPageInfo.getPageSize());
        pageResult.setList(result);
        return CommonResult.success(pageResult);
    }
}
