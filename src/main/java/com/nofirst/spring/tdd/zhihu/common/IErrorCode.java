package com.nofirst.spring.tdd.zhihu.common;

/**
 * API返回码接口
 */
public interface IErrorCode {
    /**
     * 返回码
     *
     * @return the code
     */
    long getCode();

    /**
     * 返回信息
     *
     * @return the message
     */
    String getMessage();

    /**
     * Sets message.
     *
     * @param message the message
     */
    void setMessage(String message);
}
