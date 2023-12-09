-- Valentina Studio --
-- MySQL dump --
-- ---------------------------------------------------------


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
-- ---------------------------------------------------------


-- CREATE DATABASE "serverdb" ------------------------------
CREATE DATABASE IF NOT EXISTS `serverdb` CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `serverdb`;
-- ---------------------------------------------------------


-- CREATE TABLE "ChatUser" -------------------------------------
CREATE TABLE `ChatUser`( 
	`id` BigInt( 255 ) AUTO_INCREMENT NOT NULL,
	`name` VarChar( 255 ) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
	PRIMARY KEY ( `id` ) )
CHARACTER SET = utf8
COLLATE = utf8_general_ci
ENGINE = InnoDB
AUTO_INCREMENT = 4;
-- -------------------------------------------------------------


-- CREATE TABLE "ChatUserDetails" ------------------------------
CREATE TABLE `ChatUserDetails`( 
	`id` BigInt( 255 ) AUTO_INCREMENT NOT NULL,
	`chatuserid` BigInt( 255 ) NOT NULL,
	`message` Text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
	PRIMARY KEY ( `id` ) )
CHARACTER SET = utf8
COLLATE = utf8_general_ci
ENGINE = InnoDB
AUTO_INCREMENT = 130;
-- -------------------------------------------------------------


-- CREATE TABLE "Job" ------------------------------------------
CREATE TABLE `Job`( 
	`id` BigInt( 20 ) AUTO_INCREMENT NOT NULL,
	`task_name` VarChar( 255 ) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
	`data` LongText CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
	`scheduled_time` BigInt( 20 ) NOT NULL,
	PRIMARY KEY ( `id` ) )
CHARACTER SET = utf8
COLLATE = utf8_general_ci
ENGINE = InnoDB
AUTO_INCREMENT = 68;
-- -------------------------------------------------------------


-- CREATE INDEX "chatuserdetails_fk1" --------------------------
CREATE INDEX `chatuserdetails_fk1` USING BTREE ON `ChatUserDetails`( `chatuserid` );
-- -------------------------------------------------------------


-- CREATE LINK "chatuserdetails_fk1" ---------------------------
ALTER TABLE `ChatUserDetails`
	ADD CONSTRAINT `chatuserdetails_fk1` FOREIGN KEY ( `chatuserid` )
	REFERENCES `ChatUser`( `id` )
	ON DELETE Cascade
	ON UPDATE Cascade;
-- -------------------------------------------------------------


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
-- ---------------------------------------------------------


