# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 127.0.0.1 (MySQL 5.7.12)
# Database: sure-park
# Generation Time: 2016-06-19 22:02:40 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table controller
# ------------------------------------------------------------

DROP TABLE IF EXISTS `controller`;

CREATE TABLE `controller` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `physical_id` varchar(36) NOT NULL,
  `facility_id` int(11) NOT NULL,
  `available` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `physical_id` (`physical_id`),
  KEY `facility_id` (`facility_id`),
  CONSTRAINT `controller_ibfk_1` FOREIGN KEY (`facility_id`) REFERENCES `facility` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `controller` WRITE;
/*!40000 ALTER TABLE `controller` DISABLE KEYS */;

INSERT INTO `controller` (`id`, `physical_id`, `facility_id`, `available`)
VALUES
	(1,'arduino1',1,1),
	(2,'arduino2',1,1),
	(3,'arduino3',2,1),
	(4,'arduino4',3,1);

/*!40000 ALTER TABLE `controller` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table facility
# ------------------------------------------------------------

DROP TABLE IF EXISTS `facility`;

CREATE TABLE `facility` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(36) NOT NULL,
  `fee` decimal(11,2) unsigned NOT NULL DEFAULT '5.00',
  `fee_unit` int(11) unsigned DEFAULT '3600',
  `grace_period` int(11) unsigned NOT NULL DEFAULT '1800',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `facility` WRITE;
/*!40000 ALTER TABLE `facility` DISABLE KEYS */;

INSERT INTO `facility` (`id`, `name`, `fee`, `fee_unit`, `grace_period`)
VALUES
	(1,'Shadyside Parking Lot',5.75,3600,1800),
	(2,'LG Parking Lot',12.25,3600,1800),
	(3,'CMU Parking Lot',7.00,3600,1800);

/*!40000 ALTER TABLE `facility` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table manage
# ------------------------------------------------------------

DROP TABLE IF EXISTS `manage`;

CREATE TABLE `manage` (
  `user_id` int(11) NOT NULL,
  `facility_id` int(11) NOT NULL,
  PRIMARY KEY (`user_id`,`facility_id`),
  KEY `facility_id` (`facility_id`),
  CONSTRAINT `manage_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `manage_ibfk_2` FOREIGN KEY (`facility_id`) REFERENCES `facility` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `manage` WRITE;
/*!40000 ALTER TABLE `manage` DISABLE KEYS */;

INSERT INTO `manage` (`user_id`, `facility_id`)
VALUES
	(1,1),
	(2,1),
	(1,2),
	(1,3);

/*!40000 ALTER TABLE `manage` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table reservation
# ------------------------------------------------------------

DROP TABLE IF EXISTS `reservation`;

CREATE TABLE `reservation` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `slot_id` int(11) NOT NULL,
  `confirmation_no` int(11) unsigned NOT NULL,
  `reservation_ts` int(11) unsigned NOT NULL,
  `activated` tinyint(1) unsigned NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `slot_id` (`slot_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `reservation_ibfk_1` FOREIGN KEY (`slot_id`) REFERENCES `slot` (`id`),
  CONSTRAINT `reservation_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `reservation` WRITE;
/*!40000 ALTER TABLE `reservation` DISABLE KEYS */;

INSERT INTO `reservation` (`id`, `user_id`, `slot_id`, `confirmation_no`, `reservation_ts`, `activated`)
VALUES
	(31,10,5,4574,1466349,1),
	(32,3,6,6660,1466350,1);

/*!40000 ALTER TABLE `reservation` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table role
# ------------------------------------------------------------

DROP TABLE IF EXISTS `role`;

CREATE TABLE `role` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(36) NOT NULL DEFAULT '',
  `read_facility` tinyint(1) NOT NULL DEFAULT '0',
  `write_facility` tinyint(1) NOT NULL DEFAULT '0',
  `read_reservation` tinyint(1) NOT NULL DEFAULT '0',
  `write_reservation` tinyint(1) NOT NULL DEFAULT '0',
  `read_statistics` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;

INSERT INTO `role` (`id`, `name`, `read_facility`, `write_facility`, `read_reservation`, `write_reservation`, `read_statistics`)
VALUES
	(1,'administrator',1,1,1,1,1),
	(2,'owner',1,1,1,1,1),
	(3,'attendant',1,1,1,0,0),
	(4,'driver',1,0,1,1,0);

/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table session
# ------------------------------------------------------------

DROP TABLE IF EXISTS `session`;

CREATE TABLE `session` (
  `user_id` int(11) NOT NULL,
  `session_key` varchar(36) NOT NULL,
  PRIMARY KEY (`user_id`,`session_key`),
  CONSTRAINT `session_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `session` WRITE;
/*!40000 ALTER TABLE `session` DISABLE KEYS */;

INSERT INTO `session` (`user_id`, `session_key`)
VALUES
	(1,'session1'),
	(2,'session2'),
	(3,'session3'),
	(4,'f4e7229b-2ce5-4834-86c9-ae39f1'),
	(5,'963dc367-b4a9-4f23-966c-cadb5d'),
	(6,'80e726e7-50bb-4539-bda0-4f8dff'),
	(8,'54f8be34-2a55-4fd5-9c1c-412b28'),
	(9,'272a6a56-ebae-4af2-9ae4-61cdd8'),
	(10,'e70d85aa-8228-4eff-9227-fc7623');

/*!40000 ALTER TABLE `session` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table slot
# ------------------------------------------------------------

DROP TABLE IF EXISTS `slot`;

CREATE TABLE `slot` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `controller_id` int(11) NOT NULL,
  `number` int(11) NOT NULL,
  `parked` tinyint(1) NOT NULL DEFAULT '0',
  `reserved` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `controller_id` (`controller_id`),
  CONSTRAINT `slot_ibfk_1` FOREIGN KEY (`controller_id`) REFERENCES `controller` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `slot` WRITE;
/*!40000 ALTER TABLE `slot` DISABLE KEYS */;

INSERT INTO `slot` (`id`, `controller_id`, `number`, `parked`, `reserved`)
VALUES
	(1,1,1,0,0),
	(2,1,2,1,0),
	(3,1,3,0,0),
	(4,1,4,1,0),
	(5,2,1,0,1),
	(6,2,2,0,1),
	(7,2,3,0,0),
	(8,2,4,0,0),
	(9,3,1,0,0),
	(10,3,2,0,0),
	(11,4,1,0,0),
	(12,4,2,0,0);

/*!40000 ALTER TABLE `slot` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table transaction
# ------------------------------------------------------------

DROP TABLE IF EXISTS `transaction`;

CREATE TABLE `transaction` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `reservation_id` int(11) NOT NULL,
  `begin_ts` int(11) NOT NULL,
  `end_ts` int(11) DEFAULT '-1',
  `revenue` int(11) DEFAULT '-1',
  PRIMARY KEY (`id`),
  KEY `reservation_id` (`reservation_id`),
  CONSTRAINT `transaction_ibfk_1` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table user
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` tinyint(1) NOT NULL,
  `email` varchar(36) NOT NULL,
  `password` varchar(64) NOT NULL DEFAULT '',
  `card_number` varchar(36) DEFAULT NULL,
  `card_expiration` varchar(36) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;

INSERT INTO `user` (`id`, `type`, `email`, `password`, `card_number`, `card_expiration`)
VALUES
	(1,0,'owner@gmail.com','password','',''),
	(2,1,'dave@gmail.com','password','',''),
	(3,2,'beney@gmail.com','password','0000-0000-0000-0000','00/00'),
	(4,2,'reshout@gmail.com','password','0000-1111-1111-1111','00/00'),
	(5,2,'test@gmail.com','testtest','1234-5678-9012-3467','11/22'),
	(6,2,'aaa@gmail.com','1234','1111111111111111','12/12'),
	(8,2,'test2@gamil.com','test2','123456789098','12/15'),
	(9,2,'daniel@cmu.com','1234','123456780987','22/22'),
	(10,2,'reshout@naver.com','password','1122334455667788','03/12');

/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;



/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
