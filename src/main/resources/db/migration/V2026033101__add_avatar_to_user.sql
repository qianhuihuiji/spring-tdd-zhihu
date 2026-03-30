-- Add avatar column to user table
ALTER TABLE `user`
  ADD COLUMN `avatar` VARCHAR(255) NULL COMMENT '头像相对路径' AFTER `email_verified_at`;
