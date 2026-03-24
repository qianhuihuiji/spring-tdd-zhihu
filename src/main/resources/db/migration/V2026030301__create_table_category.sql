create table `category`
(
    `id`   int auto_increment comment '主键' primary key,
    `name` varchar(10) not null comment '名称',
    `slug` varchar(50) null comment '标签'
) comment '分类表';