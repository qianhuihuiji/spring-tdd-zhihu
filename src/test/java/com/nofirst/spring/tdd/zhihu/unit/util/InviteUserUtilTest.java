package com.nofirst.spring.tdd.zhihu.unit.util;

import com.nofirst.spring.tdd.zhihu.util.InviteUserUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class InviteUserUtilTest {


    @Test
    void extract_invited_user_with_no_user() {
        // given
        String content = "This is a normal comment without mention";

        // when
        List<String> users = InviteUserUtil.extractInvitedUser(content);

        // then
        assertThat(users).isEmpty();
    }

    @Test
    void extract_invited_user_from_content() {
        // given
        String content = "@Jane @Foo look at this";

        // when
        List<String> users = InviteUserUtil.extractInvitedUser(content);

        // then
        assertThat(users).containsExactly("Jane", "Foo");
    }
}