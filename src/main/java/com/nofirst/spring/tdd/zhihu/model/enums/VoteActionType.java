package com.nofirst.spring.tdd.zhihu.model.enums;

public enum VoteActionType {

    NOTHING((byte) 0, "未进行"),

    VOTE_UP((byte) 1, "赞同"),

    VOTE_DOWN((byte) 2, "反对");

    private final Byte code;
    private final String description;

    VoteActionType(Byte code, String description) {
        this.code = code;
        this.description = description;
    }

    public Byte getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
