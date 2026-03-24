CREATE TABLE `question`
(
    `id`           int unsigned NOT NULL AUTO_INCREMENT,
    `user_id`      int          NOT NULL comment '用户编号',
    `title`        varchar(100) NOT NULL comment '标题',
    `content`      text         NOT NULL comment '内容',
    `created_at`   timestamp    NOT NULL comment '创建时间',
    `updated_at`   timestamp    NOT NULL comment '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 comment '问题表';