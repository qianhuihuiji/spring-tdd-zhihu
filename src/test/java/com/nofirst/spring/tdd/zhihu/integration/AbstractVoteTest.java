package com.nofirst.spring.tdd.zhihu.integration;

public abstract class AbstractVoteTest extends BaseContainerTest {

    /**
     * 获取资源类型名称，例如 "Question", "Answer", "Comment"
     */
    protected abstract String getResourceTypeName();

    /**
     * 获取资源路径，例如 "questions", "answers", "comments"
     */
    protected abstract String getResourcePath();

    protected String getUpVoteUrl(int resourceId) {
        return "/" + getResourcePath() + "/" + resourceId + "/up-votes";
    }

    protected String getDownVoteUrl(int resourceId) {
        return "/" + getResourcePath() + "/" + resourceId + "/down-votes";
    }
}
