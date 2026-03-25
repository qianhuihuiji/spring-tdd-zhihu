ALTER TABLE `user`
ADD COLUMN `email_verified_at` TIMESTAMP NULL COMMENT '邮箱验证时间（为空表示未验证，不为空表示已验证）';
