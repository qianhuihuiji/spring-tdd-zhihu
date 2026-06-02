package com.nofirst.spring.tdd.zhihu.unit.policy;

import com.nofirst.spring.tdd.zhihu.exception.UserNotExistedException;
import com.nofirst.spring.tdd.zhihu.factory.UserFactory;
import com.nofirst.spring.tdd.zhihu.mbg.mapper.UserMapper;
import com.nofirst.spring.tdd.zhihu.mbg.model.User;
import com.nofirst.spring.tdd.zhihu.policy.UserPolicy;
import com.nofirst.spring.tdd.zhihu.security.AccountUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserPolicyTest {

    @InjectMocks
    private UserPolicy userPolicy;

    @Mock
    private UserMapper userMapper;

    @Test
    void user_can_modify_own_avatar() {
        // given
        AccountUser accountUser = UserFactory.createAccountUser();
        User user = UserFactory.createUser();
        user.setId(accountUser.getUserId());
        given(userMapper.selectByPrimaryKey(accountUser.getUserId())).willReturn(user);

        // when
        boolean canModify = userPolicy.canModifyAvatar(accountUser.getUserId(), accountUser);

        // then
        assertThat(canModify).isTrue();
    }

    @Test
    void user_cannot_modify_others_avatar() {
        // given
        AccountUser accountUser = UserFactory.createAccountUser();
        User otherUser = UserFactory.createUser();
        otherUser.setId(2);
        given(userMapper.selectByPrimaryKey(2)).willReturn(otherUser);

        // when
        boolean canModify = userPolicy.canModifyAvatar(2, accountUser);

        // then
        assertThat(canModify).isFalse();
    }

    @Test
    void user_must_exist_when_checking_avatar_permission() {
        // given
        AccountUser accountUser = UserFactory.createAccountUser();
        given(userMapper.selectByPrimaryKey(anyInt())).willReturn(null);

        // then
        assertThatThrownBy(() -> {
            // when
            userPolicy.canModifyAvatar(1, accountUser);
        }).isInstanceOf(UserNotExistedException.class)
                .hasMessageContaining("user not exist");
    }
}
