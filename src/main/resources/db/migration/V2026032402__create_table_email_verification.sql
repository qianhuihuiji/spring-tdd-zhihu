CREATE TABLE `email_verification` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增 ID',
    `user_id` INT UNSIGNED NOT NULL COMMENT '用户 ID',
    `code` VARCHAR(6) NOT NULL COMMENT '6 位验证码',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱地址',
    `verified_at` TIMESTAMP NULL COMMENT '验证时间',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮箱验证码表';
