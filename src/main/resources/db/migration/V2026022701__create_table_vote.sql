CREATE TABLE `vote` (
                        `id` int unsigned NOT NULL AUTO_INCREMENT,
                        `user_id` int NOT NULL comment '用户编号',
                        `resource_id` int NOT NULL comment '所属资源的编号，例如 Question、Answer模型的id',
                        `resource_type` VARCHAR(10) NOT NULL comment '所属资源的类型，如Question、Answer',
                        `action_type` tinyint NOT NULL comment '投票类型，1：赞同；2：反对',
                        `created_at` timestamp NOT NULL  comment '创建时间',
                        `updated_at` timestamp NOT NULL  comment '更新时间',
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 comment '投票表';

ALTER TABLE `vote` add CONSTRAINT unq_user_id_vote UNIQUE (`user_id`, `resource_id`,`resource_type`);