SET MODE MySQL;
CREATE TABLE if not exists `table_sku` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(32) not null ,
    `price` int not null ,
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);