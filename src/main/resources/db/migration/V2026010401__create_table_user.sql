
CREATE TABLE `user` (
                        `id` int unsigned NOT NULL AUTO_INCREMENT comment '自增用户编号',
                        `name` VARCHAR(100)  NOT NULL comment '用户名',
                        `phone` VARCHAR(100)  NOT NULL comment '电话号码',
                        `email` VARCHAR(100)  NOT NULL comment '邮箱 ',
                        `password` VARCHAR(100)  NOT NULL comment '密码（加密）',
                        `created_at` timestamp NOT NULL comment '创建时间',
                        `updated_at` timestamp NOT NULL comment '更新时间',
                        PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 comment '用户表';

