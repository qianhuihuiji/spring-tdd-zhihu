package com.nofirst.spring.tdd.zhihu.event;

import com.nofirst.spring.tdd.zhihu.mbg.model.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranslateSlugEvent {

    private Question question;
    private Date eventTime;
}
