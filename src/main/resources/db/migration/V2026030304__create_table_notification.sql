create table `notification`
(
    `id` int unsigned auto_increment comment '主键'
        primary key,
    `type` varchar(255) not null comment '通知类型',
    `user_id` int  NOT NULL comment '接收用户的id',
    `data` text not null comment '通知内容',
    `read_at` timestamp NULL DEFAULT NULL,
    `created_at` timestamp NOT NULL,
    `updated_at` timestamp NOT NULL
) comment '通知表';