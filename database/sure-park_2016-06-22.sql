# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 127.0.0.1 (MySQL 5.7.12)
# Database: sure-park
# Generation Time: 2016-06-22 20:54:38 +0000
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
	(1,'CMU West Parking Lot',900.00,10,60),
	(2,'CMU East Parking Lot',12.25,3600,1800),
	(3,'SEI Parking Lot',9.00,3600,1800),
	(4,'',5.00,3600,1800);

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
	(2,1),
	(4,1),
	(2,2),
	(5,2),
	(3,3);

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
	(62,35,1,4194,1466628240,1466628300,10.75,60,0),
	(63,37,2,6161,1466627896,1466627956,10.75,60,0),
	(64,39,3,8470,1466628243,1466628303,10.75,60,0),
	(65,38,4,5969,1466628246,1466628306,10.75,60,0),
	(66,37,5,2047,1466628104,1466628164,10.75,60,0),
	(67,35,1,5200,1466628876,1466628936,10.75,60,0),
	(68,37,2,8980,1466628540,1466628600,10.75,60,0),
	(69,39,4,9652,1466628780,1466628840,10.75,60,0),
	(70,38,4,7647,1466628840,1466628900,10.75,60,0),
	(71,38,3,4701,1466628743,1466628803,900.00,10,0);

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
	(1,'administrator',1,1,1,1,0),
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
  `issue_ts` int(11) NOT NULL,
  PRIMARY KEY (`user_id`,`session_key`),
  UNIQUE KEY `session_key` (`session_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `session` WRITE;
/*!40000 ALTER TABLE `session` DISABLE KEYS */;

INSERT INTO `session` (`user_id`, `session_key`, `issue_ts`)
VALUES
	(4,'ea5e7eb9-c90c-41f2-b6b8-fa7d26',1466628511),
	(35,'7fa227c9-2426-4ed9-9348-f01128',1466628018),
	(36,'5b5eeb17-710c-4fc1-b4ea-62067a',1466625379),
	(37,'b7a21b70-eb0b-4805-a04d-055f7e',1466628004),
	(38,'5545e505-7749-498a-ae7a-f57e77',1466627812),
	(39,'c3767482-e3a3-448e-9659-6049fb',1466626792);

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
  `payment_id` varchar(32) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `reservation_id` (`reservation_id`),
  CONSTRAINT `transaction_ibfk_1` FOREIGN KEY (`reservation_id`) REFERENCES `reservation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `transaction` WRITE;
/*!40000 ALTER TABLE `transaction` DISABLE KEYS */;

INSERT INTO `transaction` (`id`, `reservation_id`, `begin_ts`, `end_ts`, `revenue`, `payment_id`)
VALUES
	(51,63,1466627933,NULL,NULL,'0'),
	(52,62,1466628113,NULL,NULL,'0'),
	(53,68,1466628560,1466628792,43.00,'2016062214523'),
	(54,67,1466628574,1466628784,43.00,'2016062214523'),
	(55,69,1466628751,1466628808,10.75,'2016062214523'),
	(56,71,1466628769,1466628800,3600.00,'2016062214523');

/*!40000 ALTER TABLE `transaction` ENABLE KEYS */;
UNLOCK TABLES;


# Dump of table user
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `role_id` int(11) NOT NULL,
  `email` varchar(64) NOT NULL DEFAULT '',
  `password` varchar(256) NOT NULL DEFAULT '',
  `card_number` varchar(256) DEFAULT NULL,
  `card_expiration` varchar(256) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `user_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;

INSERT INTO `user` (`id`, `role_id`, `email`, `password`, `card_number`, `card_expiration`)
VALUES
	(1,1,'admin@surepark.com','*2470C0C06DEE42FD1618BB99005ADCA2EC9D1E19',NULL,NULL),
	(2,2,'lattanze@cs.cmu.edu','*2470C0C06DEE42FD1618BB99005ADCA2EC9D1E19',NULL,NULL),
	(3,2,'dplakosh@sei.cmu.edu','*2470C0C06DEE42FD1618BB99005ADCA2EC9D1E19',NULL,NULL),
	(4,3,'dave@gmail.com','*2470C0C06DEE42FD1618BB99005ADCA2EC9D1E19',NULL,NULL),
	(5,3,'kevin@gmail.com','*2470C0C06DEE42FD1618BB99005ADCA2EC9D1E19',NULL,NULL),
	(35,4,'beney.kim@gmail.com','*0FEA29D3495A20FAF6F606EA9DB40561AFB9DC8C','60EB0B112645C07A39CD677ACA521D48C65BFAFCD68D35CF43955297A8831EAD','48AE19FD582E6DA99660DDF2B7E7A70A'),
	(36,4,'reshout@gmail.com','*2470C0C06DEE42FD1618BB99005ADCA2EC9D1E19','D33E764D6937B8A2DB6FFB46BB8A35C8C65BFAFCD68D35CF43955297A8831EAD','6DF44C3AA3921A83E1944AB1F232F3D4'),
	(37,4,'test1@gmail.com','*06C0BF5B64ECE2F648B5F048A71903906BA08E5C','3C085703BFEEA992175CA77F2BB745CBC65BFAFCD68D35CF43955297A8831EAD','808EAAF8250744988CD9047C5A8CA0BB'),
	(38,4,'test2@gmail.com','*7CEB3FDE5F7A9C4CE5FBE610D7D8EDA62EBE5F4E','EDD849DFC4395F37978DC2AD58E3BDB1C65BFAFCD68D35CF43955297A8831EAD','884BEB6AC01BAB4ADE09F3164AC57DC3'),
	(39,4,'test4@gmail.com','*D159BBDA31273BE3F4F00715B4A439925C6A0F2D','D33E764D6937B8A2DB6FFB46BB8A35C8C65BFAFCD68D35CF43955297A8831EAD','6C9F8F63B25F1596E8570910B7847ECA');

/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;



/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
