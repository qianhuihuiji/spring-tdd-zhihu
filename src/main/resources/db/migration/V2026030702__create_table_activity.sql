CREATE TABLE `activity`
(
    `id`           int unsigned NOT NULL AUTO_INCREMENT,
    `user_id`      int          NOT NULL comment '用户id',
    `type`         VARCHAR(50)  NOT NULL comment '活动的类型',
    `subject_id`   int          NOT NULL comment '活动主题的id，如问题模型的id，回答模型的id',
    `subject_type` VARCHAR(50)  NOT NULL comment '活动主题的模型类型，如Question、Answer',
    `created_at`   timestamp    NOT NULL comment '创建时间',
    `updated_at`   timestamp    NOT NULL comment '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1;
