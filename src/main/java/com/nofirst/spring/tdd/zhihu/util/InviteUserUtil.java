package com.nofirst.spring.tdd.zhihu.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InviteUserUtil {

    public static List<String> extractInvitedUser(String content) {
        List<String> invitedUser = new ArrayList<>();
        Pattern p = Pattern.compile("(?<=@)\\S+");
        Matcher m = p.matcher(content);
        while (m.find()) {
            String username = m.group();
            invitedUser.add(username);
        }
        return invitedUser;
    }
}
