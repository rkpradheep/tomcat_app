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
CREATE DATABASE IF NOT EXISTS `tomcatserver` CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci;
USE `tomcatserver`;
-- ---------------------------------------------------------


-- CREATE TABLE "ChatUser" -------------------------------------
CREATE TABLE `ChatUser`( 
	`id` BigInt( 255 ) AUTO_INCREMENT NOT NULL,
	`name` VarChar( 255 ) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	PRIMARY KEY ( `id` ) )
CHARACTER SET = utf8mb3
COLLATE = utf8mb3_general_ci
ENGINE = InnoDB
AUTO_INCREMENT = 1000000000004;
-- -------------------------------------------------------------


-- CREATE TABLE "Job" ------------------------------------------
CREATE TABLE `Job`( 
	`id` BigInt( 20 ) AUTO_INCREMENT NOT NULL,
	`task_name` VarChar( 255 ) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	`data` LongText CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	`scheduled_time` BigInt( 20 ) NOT NULL,
	`is_recurring` TinyInt( 1 ) NOT NULL DEFAULT 0,
	`day_interval` Int( 255 ) NOT NULL DEFAULT -1,
	PRIMARY KEY ( `id` ) )
CHARACTER SET = utf8mb3
COLLATE = utf8mb3_general_ci
ENGINE = InnoDB
AUTO_INCREMENT = 1000000000106;
-- -------------------------------------------------------------


-- CREATE TABLE "Users" ----------------------------------------
CREATE TABLE `Users`( 
	`id` BigInt( 255 ) AUTO_INCREMENT NOT NULL,
	`name` VarChar( 255 ) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	`password` VarChar( 255 ) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	`role_type` Int( 11 ) NOT NULL,
	PRIMARY KEY ( `id` ) )
CHARACTER SET = utf8mb3
COLLATE = utf8mb3_general_ci
ENGINE = InnoDB
AUTO_INCREMENT = 1000000000003;
-- -------------------------------------------------------------


-- CREATE TABLE "ChatUserDetails" ------------------------------
CREATE TABLE `ChatUserDetails`( 
	`id` BigInt( 255 ) AUTO_INCREMENT NOT NULL,
	`chatuserid` BigInt( 255 ) NOT NULL,
	`message` Text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	PRIMARY KEY ( `id` ) )
CHARACTER SET = utf8mb3
COLLATE = utf8mb3_general_ci
ENGINE = InnoDB
AUTO_INCREMENT = 1000000000016;
-- -------------------------------------------------------------


-- CREATE TABLE "SessionManagement" ----------------------------
CREATE TABLE `SessionManagement`( 
	`id` VarChar( 255 ) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	`user_id` BigInt( 255 ) NOT NULL,
	`expiry_time` BigInt( 255 ) NOT NULL,
	PRIMARY KEY ( `id` ) )
CHARACTER SET = utf8mb3
COLLATE = utf8mb3_general_ci
ENGINE = InnoDB;
-- -------------------------------------------------------------


-- CREATE TABLE "AuthToken" ------------------------------------
CREATE TABLE `AuthToken`( 
	`token` Text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	`user_id` BigInt( 255 ) NOT NULL )
CHARACTER SET = utf8mb3
COLLATE = utf8mb3_general_ci
ENGINE = InnoDB;
-- -------------------------------------------------------------


-- CREATE INDEX "chatuserdetails_fk1" --------------------------
CREATE INDEX `chatuserdetails_fk1` USING BTREE ON `ChatUserDetails`( `chatuserid` );
-- -------------------------------------------------------------


-- CREATE INDEX "lnk_Users_SessionManagement" ------------------
CREATE INDEX `lnk_Users_SessionManagement` USING BTREE ON `SessionManagement`( `user_id` );
-- -------------------------------------------------------------


-- CREATE INDEX "lnk_Users_AuthToken" --------------------------
CREATE INDEX `lnk_Users_AuthToken` USING BTREE ON `AuthToken`( `user_id` );
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


-- CREATE LINK "lnk_Users_AuthToken" ---------------------------
ALTER TABLE `AuthToken`
	ADD CONSTRAINT `lnk_Users_AuthToken` FOREIGN KEY ( `user_id` )
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

