package com.nofirst.spring.tdd.zhihu.mbg.model;

import java.io.Serializable;
import java.util.Date;

public class EmailVerification implements Serializable {
    /**
     * 自增 ID
     *
     * @mbg.generated
     */
    private Integer id;

    /**
     * 用户 ID
     *
     * @mbg.generated
     */
    private Integer userId;

    /**
     * 6 位验证码
     *
     * @mbg.generated
     */
    private String code;

    /**
     * 邮箱地址
     *
     * @mbg.generated
     */
    private String email;

    /**
     * 验证时间
     *
     * @mbg.generated
     */
    private Date verifiedAt;

    /**
     * 创建时间
     *
     * @mbg.generated
     */
    private Date createdAt;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(Date verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", code=").append(code);
        sb.append(", email=").append(email);
        sb.append(", verifiedAt=").append(verifiedAt);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}