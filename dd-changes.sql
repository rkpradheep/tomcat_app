-- Valentina Studio --
-- MySQL dump --
-- ---------------------------------------------------------


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
-- ---------------------------------------------------------


-- CREATE DATABASE "tomcatserver" --------------------------
CREATE DATABASE IF NOT EXISTS `tomcatserver` CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `tomcatserver`;
-- ---------------------------------------------------------


-- CREATE TABLE "ChatUser" -------------------------------------
CREATE TABLE `ChatUser`( 
	`id` BigInt( 255 ) AUTO_INCREMENT NOT NULL,
	`name` VarChar( 255 ) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
	PRIMARY KEY ( `id` ) )
CHARACTER SET = utf8
COLLATE = utf8_general_ci
ENGINE = InnoDB
AUTO_INCREMENT = 1000000000002;
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
AUTO_INCREMENT = 1000000000006;
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
AUTO_INCREMENT = 1000000000000;
-- -------------------------------------------------------------


-- CREATE TABLE "Users" ----------------------------------------
CREATE TABLE `Users`( 
	`id` BigInt( 255 ) AUTO_INCREMENT NOT NULL,
	`name` VarChar( 255 ) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
	`password` VarChar( 255 ) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
	`role_type` int NOT NULL,
	PRIMARY KEY ( `id` ) )
CHARACTER SET = utf8
COLLATE = utf8_general_ci
ENGINE = InnoDB
AUTO_INCREMENT = 1000000000001;
-- -------------------------------------------------------------


-- CREATE TABLE "SessionManagement" ----------------------------
CREATE TABLE `SessionManagement`( 
	`id` VarChar( 255 ) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
	`user_id` BigInt( 255 ) NOT NULL,
	`expiry_time` BigInt( 255 ) NOT NULL,
	PRIMARY KEY ( `id` ) )
CHARACTER SET = utf8
COLLATE = utf8_general_ci
ENGINE = InnoDB;
-- -------------------------------------------------------------


-- CREATE INDEX "chatuserdetails_fk1" --------------------------
CREATE INDEX `chatuserdetails_fk1` USING BTREE ON `ChatUserDetails`( `chatuserid` );
-- -------------------------------------------------------------


-- CREATE INDEX "lnk_Users_SessionManagement" ------------------
CREATE INDEX `lnk_Users_SessionManagement` USING BTREE ON `SessionManagement`( `user_id` );
-- -------------------------------------------------------------


-- CREATE LINK "chatuserdetails_fk1" ---------------------------
ALTER TABLE `ChatUserDetails`
	ADD CONSTRAINT `chatuserdetails_fk1` FOREIGN KEY ( `chatuserid` )
	REFERENCES `ChatUser`( `id` )
	ON DELETE Cascade
	ON UPDATE Cascade;
-- -------------------------------------------------------------


-- CREATE LINK "lnk_Users_SessionManagement" -------------------
ALTER TABLE `SessionManagement`
	ADD CONSTRAINT `lnk_Users_SessionManagement` FOREIGN KEY ( `user_id` )
	REFERENCES `Users`( `id` )
	ON DELETE Cascade
	ON UPDATE Cascade;
-- -------------------------------------------------------------


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
-- ---------------------------------------------------------




INSERT INTO `tomcatserver`.`Users`
(
`name`,
`password`, `role_type`)
VALUES(
'admin',
'7676aaafb027c825bd9abab78b234070e702752f625b752e55e55b48e607e358', -1);

INSERT INTO `tomcatserver`.`Users`
(
`name`,
`password`, `role_type`)
VALUES(
'test',
'8622f0f69c91819119a8acf60a248d7b36fdb7ccf857ba8f85cf7f2767ff8265', 0);
