CREATE TABLE `answer` (
                          `id` int unsigned NOT NULL AUTO_INCREMENT,
                          `question_id` int NOT NULL comment 'question表id',
                          `user_id` int NOT NULL comment 'user表id' ,
                          `content` text  NOT NULL comment '内容',
                          `created_at` timestamp NOT NULL comment '创建时间',
                          `updated_at` timestamp NOT NULL comment '更新时间',
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 comment '问题表';