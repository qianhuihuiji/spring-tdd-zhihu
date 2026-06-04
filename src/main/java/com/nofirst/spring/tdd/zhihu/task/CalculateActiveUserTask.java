package com.nofirst.spring.tdd.zhihu.task;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CalculateActiveUserTask {

    private ActiveUserService activeUserService;

    @XxlJob("calculateActiveUser")
    public void run() {
        activeUserService.calculateAndCacheActiveUsers();
    }

}
