package com.nofirst.spring.tdd.zhihu.factory;

import com.nofirst.spring.tdd.zhihu.mbg.model.Comment;
import net.datafaker.Faker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommentFactory extends EntityFactory<Comment> {

    private static final Faker faker = new Faker();

    private Integer commentedId = 1;
    private String commentedType = "Question";
    private Integer userId = 1;

    public static CommentFactory of() {
        return new CommentFactory();
    }

    public CommentFactory forTarget(Integer commentedId, String commentedType) {
        this.commentedId = commentedId;
        this.commentedType = commentedType;
        return this;
    }

    public CommentFactory byUser(Integer userId) {
        this.userId = userId;
        return this;
    }

    @Override
    protected Comment build() {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setCommentedId(commentedId);
        comment.setCommentedType(commentedType);
        Date now = new Date();
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);
        comment.setContent(faker.lorem().sentence());

        return comment;
    }

    public static Comment create(Integer commentedId, String commentedType) {
        return CommentFactory.of().forTarget(commentedId, commentedType).make();
    }

    public static List<Comment> createBatch(Integer times, Integer commentedId, String commentedType) {
        return CommentFactory.of().forTarget(commentedId, commentedType).make(times);
    }
}
