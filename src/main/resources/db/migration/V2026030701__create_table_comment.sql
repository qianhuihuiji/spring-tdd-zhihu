CREATE TABLE `comment` (
                           `id` int unsigned NOT NULL AUTO_INCREMENT,
                           `user_id` int NOT NULL comment '用户id',
                           `content` text  NOT NULL comment '评论内容',
                           `commented_id` int NOT NULL comment '资源的id，如问题模型的id，回答模型的id',
                           `commented_type` VARCHAR(10) NOT NULL comment '资源的类型，如Question、Answer',
                           `created_at` timestamp NULL DEFAULT NULL comment '创建时间',
                           `updated_at` timestamp NULL DEFAULT NULL comment '更新时间',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 ;