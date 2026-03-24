alter table question
    add answers_count int not null default 0 comment '回答数量';