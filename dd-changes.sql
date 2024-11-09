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


-- CREATE TABLE "BatchTable" -----------------------------------
CREATE TABLE `BatchTable`(
	`AccountId` BigInt( 255 ) NOT NULL,
	`BatchStart` BigInt( 255 ) NOT NULL )
CHARACTER SET = utf8mb3
COLLATE = utf8mb3_general_ci
ENGINE = InnoDB;
-- -------------------------------------------------------------


-- CREATE TABLE "Configuration" --------------------------------
CREATE TABLE `Configuration`(
	`Id` BigInt( 255 ) NOT NULL,
	`CKey` VarChar( 255 ) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	`CValue` LongText CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	PRIMARY KEY ( `Id` ) )
CHARACTER SET = utf8mb3
COLLATE = utf8mb3_general_ci
ENGINE = InnoDB;
-- -------------------------------------------------------------


-- CREATE TABLE "AuthToken" ------------------------------------
CREATE TABLE `AuthToken`(
	`Id` BigInt( 255 ) NOT NULL,
	`Token` LongText CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	`UserId` BigInt( 255 ) NOT NULL,
	PRIMARY KEY ( `Id` ) )
CHARACTER SET = utf8mb3
COLLATE = utf8mb3_general_ci
ENGINE = InnoDB;
-- -------------------------------------------------------------


-- CREATE TABLE "SessionManagement" ----------------------------
CREATE TABLE `SessionManagement`(
	`Id` BigInt( 255 ) NOT NULL,
	`UserId` BigInt( 255 ) NOT NULL,
	`ExpiryTime` BigInt( 255 ) NOT NULL,
	`SessionId` LongText CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	PRIMARY KEY ( `Id` ) )
CHARACTER SET = utf8mb3
COLLATE = utf8mb3_general_ci
ENGINE = InnoDB;
-- -------------------------------------------------------------


-- CREATE TABLE "ChatUserDetail" -------------------------------
CREATE TABLE `ChatUserDetail`(
	`Id` BigInt( 255 ) NOT NULL,
	`ChatUserId` BigInt( 255 ) NOT NULL,
	`Message` Text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	PRIMARY KEY ( `Id` ) )
CHARACTER SET = utf8mb3
COLLATE = utf8mb3_general_ci
ENGINE = InnoDB;
-- -------------------------------------------------------------


-- CREATE TABLE "ChatUser" -------------------------------------
CREATE TABLE `ChatUser`(
	`Id` BigInt( 255 ) NOT NULL,
	`Name` VarChar( 255 ) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	PRIMARY KEY ( `Id` ) )
CHARACTER SET = utf8mb3
COLLATE = utf8mb3_general_ci
ENGINE = InnoDB;
-- -------------------------------------------------------------


-- CREATE TABLE "Job" ------------------------------------------
CREATE TABLE `Job`(
	`Id` BigInt( 20 ) NOT NULL,
	`TaskName` VarChar( 255 ) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	`Data` LongText CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	`ScheduledTime` BigInt( 20 ) NOT NULL,
	`DayInterval` Int( 255 ) NOT NULL,
	`IsRecurring` TinyInt( 255 ) NOT NULL,
	PRIMARY KEY ( `Id` ) )
CHARACTER SET = utf8mb3
COLLATE = utf8mb3_general_ci
ENGINE = InnoDB;
-- -------------------------------------------------------------


-- CREATE TABLE "User" -----------------------------------------
CREATE TABLE `User`(
	`Id` BigInt( 255 ) NOT NULL,
	`Name` VarChar( 255 ) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	`Password` VarChar( 255 ) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	`RoleType` Int( 11 ) NOT NULL,
	PRIMARY KEY ( `Id` ) )
CHARACTER SET = utf8mb3
COLLATE = utf8mb3_general_ci
ENGINE = InnoDB;
-- -------------------------------------------------------------


-- CREATE TABLE "HttpLog" --------------------------------------
CREATE TABLE `HttpLog`(
	`Id` BigInt( 255 ) NOT NULL,
	`Url` VarChar( 255 ) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	`Method` VarChar( 255 ) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	`IP` VarChar( 255 ) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
	`Parameters` Text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
	`RequestHeaders` Text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
	`ResponseHeaders` Text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
	`RequestData` LongText CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
	`ResponseData` LongText CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
	`ThreadName` VarChar( 255 ) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
	`CreatedTime` BigInt( 255 ) NOT NULL,
	`EntityType` TinyInt( 255 ) NOT NULL,
	`StatusCode` Int,
	`IsOutgoing` TinyInt( 255 ) NOT NULL,
	PRIMARY KEY ( `Id` ) )
CHARACTER SET = utf8mb3
COLLATE = utf8mb3_general_ci
ENGINE = InnoDB;
-- -------------------------------------------------------------



-- CREATE INDEX "lnk_User_AuthToken" ---------------------------
CREATE INDEX `lnk_User_AuthToken` USING BTREE ON `AuthToken`( `UserId` );
-- -------------------------------------------------------------


-- CREATE INDEX "lnk_Users_SessionManagement" ------------------
CREATE INDEX `lnk_Users_SessionManagement` USING BTREE ON `SessionManagement`( `UserId` );
-- -------------------------------------------------------------


-- CREATE INDEX "chatuserdetails_fk1" --------------------------
CREATE INDEX `chatuserdetails_fk1` USING BTREE ON `ChatUserDetail`( `ChatUserId` );
-- -------------------------------------------------------------


-- CREATE LINK "lnk_User_AuthToken" ----------------------------
ALTER TABLE `AuthToken`
	ADD CONSTRAINT `lnk_User_AuthToken` FOREIGN KEY ( `UserId` )
	REFERENCES `User`( `Id` )
	ON DELETE Cascade
	ON UPDATE Cascade;
-- -------------------------------------------------------------


-- CREATE LINK "lnk_User_SessionManagement" --------------------
ALTER TABLE `SessionManagement`
	ADD CONSTRAINT `lnk_User_SessionManagement` FOREIGN KEY ( `UserId` )
	REFERENCES `User`( `Id` )
	ON DELETE Cascade
	ON UPDATE Cascade;
-- -------------------------------------------------------------


-- CREATE LINK "chatuserdetails_fk1" ---------------------------
ALTER TABLE `ChatUserDetail`
	ADD CONSTRAINT `chatuserdetails_fk1` FOREIGN KEY ( `ChatUserId` )
	REFERENCES `ChatUser`( `Id` )
	ON DELETE Cascade
	ON UPDATE Cascade;
-- -------------------------------------------------------------


-- CREATE LINK "lnk_ChatUser_ChatUserDetails" ------------------
ALTER TABLE `ChatUserDetail`
	ADD CONSTRAINT `lnk_ChatUser_ChatUserDetails` FOREIGN KEY ( `ChatUserId` )
	REFERENCES `ChatUser`( `Id` )
	ON DELETE Cascade
	ON UPDATE Cascade;
-- -------------------------------------------------------------


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
-- ---------------------------------------------------------



INSERT INTO `tomcatserver`.`User` VALUES(1000000000001,'admin','7676aaafb027c825bd9abab78b234070e702752f625b752e55e55b48e607e358', -1);

INSERT INTO `tomcatserver`.`User` VALUES(1000000000002, 'test', '8622f0f69c91819119a8acf60a248d7b36fdb7ccf857ba8f85cf7f2767ff8265', 0);

INSERT into AuthToken values(1000000000003, 'nwDP625SPuLFaTYtGxF9hXlimwVQX9Mi6mBnVvQVrbSeX5sKYs',1000000000002);
