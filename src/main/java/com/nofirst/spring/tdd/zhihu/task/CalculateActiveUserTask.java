package com.nofirst.spring.tdd.zhihu.task;

import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@AllArgsConstructor
public class CalculateActiveUserTask {

    private ActiveUserService activeUserService;

    @XxlJob("calculateActiveUserTask")
    public void run() {
        activeUserService.calculateAndCacheActiveUsers();
    }

}
