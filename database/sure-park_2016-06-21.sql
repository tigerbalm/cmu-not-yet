# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 127.0.0.1 (MySQL 5.7.12)
# Database: sure-park
# Generation Time: 2016-06-21 21:22:06 +0000
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
	(1,'1',1,1),
	(2,'2',1,1),
	(3,'3',2,1),
	(4,'4',3,1);

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
	(1,'Shadyside Parking Lot',3.50,60,60),
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
	(17,2),
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
  `expiration_ts` int(11) NOT NULL,
  `fee` decimal(11,2) NOT NULL,
  `fee_unit` int(11) NOT NULL,
  `activated` tinyint(1) unsigned NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  KEY `slot_id` (`slot_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `reservation_ibfk_1` FOREIGN KEY (`slot_id`) REFERENCES `slot` (`id`),
  CONSTRAINT `reservation_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `reservation` WRITE;
/*!40000 ALTER TABLE `reservation` DISABLE KEYS */;

INSERT INTO `reservation` (`id`, `user_id`, `slot_id`, `confirmation_no`, `reservation_ts`, `expiration_ts`, `fee`, `fee_unit`, `activated`)
VALUES
	(6,6,4,8161,1466543280,1466543290,3.50,60,0),
	(7,3,3,4276,1466543220,1466543230,3.50,60,0),
	(8,9,1,4585,1466543220,1466543230,3.50,60,0),
	(9,11,2,1838,1466539863,1466539873,3.50,60,0),
	(10,16,1,9504,1466544360,1466544370,3.50,60,0),
	(11,16,9,7983,1466541718,1466543518,12.25,3600,0),
	(12,16,1,9276,1466545020,1466545030,3.50,60,0),
	(13,11,9,1119,1466542260,1466544060,12.25,3600,0),
	(14,16,2,1057,1466545440,1466545450,3.50,60,0),
	(15,3,1,4551,1466543236,1466543246,3.50,60,0),
	(16,3,1,7475,1466543463,1466543473,3.50,60,0);

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
	(10,'e70d85aa-8228-4eff-9227-fc7623'),
	(11,'10c3adea-150f-4543-b425-d14d0c'),
	(12,'4a8be540-67ee-49c0-9f43-14b3e6'),
	(13,'9f595937-afc0-4fb9-9f1e-8150d8'),
	(14,'dd8fc17e-47a5-4c6e-b199-fa456b'),
	(15,'0435fdc4-e20a-4e68-863f-d715dc'),
	(16,'3b63e82e-5cf9-4d11-85b9-701541'),
	(17,'3b63e82e-5cf9-4d11-85b9-721141');

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
	(2,1,2,0,0),
	(3,1,3,0,0),
	(4,1,4,0,0),
	(5,2,1,0,0),
	(6,2,2,0,0),
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
  `end_ts` int(11) DEFAULT NULL,
  `revenue` decimal(11,2) DEFAULT NULL,
  `paid` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `reservation_id` (`reservation_id`),
  CONSTRAINT `transaction_ibfk_1` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `transaction` WRITE;
/*!40000 ALTER TABLE `transaction` DISABLE KEYS */;

INSERT INTO `transaction` (`id`, `reservation_id`, `begin_ts`, `end_ts`, `revenue`, `paid`)
VALUES
	(38,6,1466539708,1466539921,10.50,0),
	(39,7,1466539734,1466539933,10.50,0),
	(40,8,1466539775,1466539959,10.50,0),
	(41,9,1466539817,1466540077,14.00,0),
	(42,12,1466541720,1466541832,3.50,0),
	(43,14,1466542278,1466543046,42.00,0),
	(44,15,1466542970,1466543030,3.50,0);

/*!40000 ALTER TABLE `transaction` ENABLE KEYS */;
UNLOCK TABLES;


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
	(10,2,'reshout@naver.com','password','1122334455667788','03/12'),
	(11,2,'test1@test.com','test1','1122334455667788','12/34'),
	(12,2,'test2@test.com','test2','1122334455667781','11/22'),
	(13,2,'test3@test.com','test3','1122334455667788','11/13'),
	(14,2,'test4@test.com','test4','1122334455661122','11/23'),
	(15,2,'test5@test.com','test5','1122334455667788','11/22'),
	(16,2,'user@gmail.com','user','9999888877776666','01/78'),
	(17,1,'kevin@gmail.com','1234',NULL,NULL);

/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;



/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
