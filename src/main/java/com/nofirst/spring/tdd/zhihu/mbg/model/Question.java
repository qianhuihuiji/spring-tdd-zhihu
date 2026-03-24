package com.nofirst.spring.tdd.zhihu.mbg.model;

import java.io.Serializable;
import java.util.Date;

public class Question implements Serializable {
    private Integer id;

    /**
     * 用户编号
     *
     * @mbg.generated
     */
    private Integer userId;

    /**
     * 标题
     *
     * @mbg.generated
     */
    private String title;

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
     * 发布时间
     *
     * @mbg.generated
     */
    private Date publishedAt;

    /**
     * 最佳答案
     *
     * @mbg.generated
     */
    private Integer bestAnswerId;

    /**
     * 分类编号
     *
     * @mbg.generated
     */
    private Integer categoryId;

    /**
     * 回答数量
     *
     * @mbg.generated
     */
    private Integer answersCount;

    private String slug;

    /**
     * 内容
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Date getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Date publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Integer getBestAnswerId() {
        return bestAnswerId;
    }

    public void setBestAnswerId(Integer bestAnswerId) {
        this.bestAnswerId = bestAnswerId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getAnswersCount() {
        return answersCount;
    }

    public void setAnswersCount(Integer answersCount) {
        this.answersCount = answersCount;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
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
        sb.append(", title=").append(title);
        sb.append(", createdAt=").append(createdAt);
        sb.append(", updatedAt=").append(updatedAt);
        sb.append(", publishedAt=").append(publishedAt);
        sb.append(", bestAnswerId=").append(bestAnswerId);
        sb.append(", categoryId=").append(categoryId);
        sb.append(", answersCount=").append(answersCount);
        sb.append(", slug=").append(slug);
        sb.append(", content=").append(content);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}