package com.nofirst.spring.tdd.zhihu.integration.users;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.nofirst.spring.tdd.zhihu.common.CommonResult;
import com.nofirst.spring.tdd.zhihu.common.ResultCode;
import com.nofirst.spring.tdd.zhihu.integration.BaseContainerTest;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.ActivityMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.Activity;
import com.nofirst.spring.tdd.zhihu.model.vo.ActivityVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ActivityIndexTest extends BaseContainerTest {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        cleanUpActivities();
    }

    @Test
    void unauthenticated_user_can_not_view_activities() throws Exception {
        mockMvc.perform(get("/activities")
                        .param("pageIndex", "1")
                        .param("pageSize", "10"))
                .andExpect(status().is(401));
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void authenticated_user_can_view_own_activities_with_pagination() throws Exception {
        // given - 为 John (id=2) 创建 3 条活动记录，按时间倒序
        for (int i = 0; i < 3; i++) {
            Activity activity = new Activity();
            activity.setUserId(2);
            activity.setType("published_question");
            activity.setSubjectId(i + 1);
            activity.setSubjectType("Question");
            activity.setCreatedAt(new Date(System.currentTimeMillis() - i * 1000));
            activity.setUpdatedAt(new Date());
            activityMapper.insert(activity);
        }

        // given - 为 Jane (id=1) 创建 1 条活动，不应出现在结果中
        Activity janeActivity = new Activity();
        janeActivity.setUserId(1);
        janeActivity.setType("created_answer");
        janeActivity.setSubjectId(1);
        janeActivity.setSubjectType("Answer");
        janeActivity.setCreatedAt(new Date());
        janeActivity.setUpdatedAt(new Date());
        activityMapper.insert(janeActivity);

        // when
        var mvcResult = mockMvc.perform(get("/activities")
                        .param("pageIndex", "1")
                        .param("pageSize", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andReturn();

        // then - 分页结果：总数 3（仅 John 的），当前页 2 条
        String json = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        TypeReference<CommonResult<PageInfo<ActivityVo>>> typeRef = new TypeReference<>() {};
        CommonResult<PageInfo<ActivityVo>> result = objectMapper.readValue(json, typeRef);

        assertThat(result.getData().getTotal()).isEqualTo(3);
        assertThat(result.getData().getList()).hasSize(2);
        assertThat(result.getData().getPageNum()).isEqualTo(1);
        assertThat(result.getData().getPageSize()).isEqualTo(2);

        // 验证每条记录都属于 John
        for (ActivityVo vo : result.getData().getList()) {
            assertThat(vo.getUserId()).isEqualTo(2);
        }
    }

    @Test
    @WithUserDetails(value = "John", userDetailsServiceBeanName = "customUserDetailsService")
    void authenticated_user_can_view_own_activities_in_descending_order() throws Exception {
        // given - 按时间倒序插入
        Activity oldest = new Activity();
        oldest.setUserId(2);
        oldest.setType("published_question");
        oldest.setSubjectId(1);
        oldest.setSubjectType("Question");
        oldest.setCreatedAt(new Date(System.currentTimeMillis() - 2000));
        oldest.setUpdatedAt(new Date());
        activityMapper.insert(oldest);

        Activity newest = new Activity();
        newest.setUserId(2);
        newest.setType("created_answer");
        newest.setSubjectId(2);
        newest.setSubjectType("Answer");
        newest.setCreatedAt(new Date());
        newest.setUpdatedAt(new Date());
        activityMapper.insert(newest);

        // when
        var mvcResult = mockMvc.perform(get("/activities")
                        .param("pageIndex", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andReturn();

        // then - 最新的在前
        String json = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        TypeReference<CommonResult<PageInfo<ActivityVo>>> typeRef = new TypeReference<>() {};
        CommonResult<PageInfo<ActivityVo>> result = objectMapper.readValue(json, typeRef);

        assertThat(result.getData().getList()).hasSize(2);
        assertThat(result.getData().getList().get(0).getType()).isEqualTo("created_answer");
        assertThat(result.getData().getList().get(1).getType()).isEqualTo("published_question");
    }
}
