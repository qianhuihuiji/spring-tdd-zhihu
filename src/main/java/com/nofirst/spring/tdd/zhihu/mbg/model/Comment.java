package com.nofirst.spring.tdd.zhihu.mbg.model;

import java.io.Serializable;
import java.util.Date;

public class Comment implements Serializable {
    private Integer id;

    /**
     * 用户id
     *
     * @mbg.generated
     */
    private Integer userId;

    /**
     * 资源的id，如问题模型的id，回答模型的id
     *
     * @mbg.generated
     */
    private Integer commentedId;

    /**
     * 资源的类型，如Question、Answer
     *
     * @mbg.generated
     */
    private String commentedType;

    /**
     * 创建时间
     *
     * @mbg.generated
     */
    private Date createdAt;

    /**
     * 更新时间
     *
     * @mbg.generated
     */
    private Date updatedAt;

    /**
     * 评论内容
     *
     * @mbg.generated
     */
    private String content;

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

    public Integer getCommentedId() {
        return commentedId;
    }

    public void setCommentedId(Integer commentedId) {
        this.commentedId = commentedId;
    }

    public String getCommentedType() {
        return commentedType;
    }

    public void setCommentedType(String commentedType) {
        this.commentedType = commentedType;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", userId=").append(userId);
        sb.append(", commentedId=").append(commentedId);
        sb.append(", commentedType=").append(commentedType);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", content=").append(content);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}