CREATE TABLE IF NOT EXISTS `gpay_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `created_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `birthday` DATE NOT NULL,
    `name` VARCHAR(64) NOT NULL,
    PRIMARY KEY (`id`)
); 