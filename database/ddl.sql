SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `facility`;
DROP TABLE IF EXISTS `controller`;
DROP TABLE IF EXISTS `slot`;
DROP TABLE IF EXISTS `reservation`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `transaction`;
DROP TABLE IF EXISTS `session`;
DROP TABLE IF EXISTS `manage`;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `facility` (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(36) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `controller` (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `physical_id` VARCHAR(36) NOT NULL,
    `facility_id` INTEGER NOT NULL,
    `available` TINYINT(1) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE (`physical_id`)
);

CREATE TABLE `slot` (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `controller_id` INTEGER NOT NULL,
    `number` INTEGER NOT NULL,
    `occupied` TINYINT(1) NOT NULL,
    `reserved` TINYINT(1) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `reservation` (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `user_id` INTEGER NOT NULL,
    `slot_id` INTEGER NOT NULL,
    `reservation_ts` INTEGER NOT NULL,
    `reservation_number` INTEGER NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `user` (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `type` TINYINT(1) NOT NULL,
    `email` VARCHAR(36) NOT NULL,
    `password` VARCHAR(36) NOT NULL,
    `card_number` VARCHAR(36) NOT NULL,
    `card_expiration` VARCHAR(36) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE (`email`)
);

CREATE TABLE `transaction` (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `reservation_id` INTEGER NOT NULL,
    `begin_ts` INTEGER NOT NULL,
    `end_ts` INTEGER NOT NULL,
    `revenue` INTEGER NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `session` (
    `user_id` INTEGER NOT NULL,
    `session_key` VARCHAR(36) NOT NULL,
    PRIMARY KEY (`user_id`, `session_key`)
);

CREATE TABLE `manage` (
    `user_id` INTEGER NOT NULL,
    `facility_id` INTEGER NOT NULL,
    PRIMARY KEY (`user_id`, `facility_id`)
);

ALTER TABLE `controller` ADD FOREIGN KEY (`facility_id`) REFERENCES `facility`(`id`);
ALTER TABLE `slot` ADD FOREIGN KEY (`controller_id`) REFERENCES `controller`(`id`);
ALTER TABLE `reservation` ADD FOREIGN KEY (`slot_id`) REFERENCES `slot`(`id`);
ALTER TABLE `reservation` ADD FOREIGN KEY (`user_id`) REFERENCES `user`(`id`);
ALTER TABLE `transaction` ADD FOREIGN KEY (`reservation_id`) REFERENCES `reservation`(`id`);
ALTER TABLE `session` ADD FOREIGN KEY (`user_id`) REFERENCES `user`(`id`);
ALTER TABLE `manage` ADD FOREIGN KEY (`user_id`) REFERENCES `user`(`id`);
ALTER TABLE `manage` ADD FOREIGN KEY (`facility_id`) REFERENCES `facility`(`id`);
