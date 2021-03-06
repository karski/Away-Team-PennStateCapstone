-- MySQL Script generated by MySQL Workbench
-- Sat 02 Aug 2014 06:29:54 PM EDT
-- Model: New Model    Version: 1.0
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema awayteam
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `awayteam` ;
CREATE SCHEMA IF NOT EXISTS `awayteam` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
USE `awayteam` ;

-- -----------------------------------------------------
-- Table `awayteam`.`user`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `awayteam`.`user` ;

CREATE TABLE IF NOT EXISTS `awayteam`.`user` (
  `userId` INT NOT NULL AUTO_INCREMENT,
  `loginId` VARCHAR(45) NOT NULL,
  `password` VARCHAR(64) NOT NULL,
  `userIdentifier` VARCHAR(64) NOT NULL,
  `userSecret` VARCHAR(64) NOT NULL,
  `userSalt` VARCHAR(64) NOT NULL,
  `email` VARCHAR(255) NOT NULL,
  `firstName` VARCHAR(45) NOT NULL,
  `lastName` VARCHAR(45) NOT NULL,
  `cellPhone` VARCHAR(45) NOT NULL,
  `emergencyPhone` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`userId`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `awayteam`.`location`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `awayteam`.`location` ;

CREATE TABLE IF NOT EXISTS `awayteam`.`location` (
  `locId` INT NOT NULL AUTO_INCREMENT,
  `locDate` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  `locLatitude` VARCHAR(45) NOT NULL,
  `locLongitude` VARCHAR(45) NOT NULL,
  `locUserId` INT NULL,
  PRIMARY KEY (`locId`),
  INDEX `locUserId_idx` (`locUserId` ASC),
  CONSTRAINT `locUserId`
    FOREIGN KEY (`locUserId`)
    REFERENCES `awayteam`.`user` (`userId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `awayteam`.`team`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `awayteam`.`team` ;

CREATE TABLE IF NOT EXISTS `awayteam`.`team` (
  `teamId` INT NOT NULL AUTO_INCREMENT,
  `teamName` VARCHAR(45) NOT NULL,
  `teamLocationName` VARCHAR(45) NULL,
  `teamDescription` VARCHAR(255) NULL,
  `teamManaged` TINYINT(1) NULL,
  PRIMARY KEY (`teamId`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `awayteam`.`team_tasks`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `awayteam`.`team_tasks` ;

CREATE TABLE IF NOT EXISTS `awayteam`.`team_tasks` (
  `taskId` INT NOT NULL AUTO_INCREMENT,
  `taskTitle` VARCHAR(45) NOT NULL,
  `taskDescription` VARCHAR(255) NULL,
  `taskCompleted` TINYINT(1) NOT NULL DEFAULT 0,
  `taskTeamId` INT NULL,
  PRIMARY KEY (`taskId`),
  INDEX `teamId_idx` (`taskTeamId` ASC),
  CONSTRAINT `teamTasksTeamId`
    FOREIGN KEY (`taskTeamId`)
    REFERENCES `awayteam`.`team` (`teamId`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `awayteam`.`team_event`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `awayteam`.`team_event` ;

CREATE TABLE IF NOT EXISTS `awayteam`.`team_event` (
  `teamEventId` INT NOT NULL AUTO_INCREMENT,
  `teamEventName` VARCHAR(45) NOT NULL,
  `teamEventDescription` VARCHAR(255) NULL,
  `teamEventLocationString` VARCHAR(45) NULL,
  `teamEventStartTime` DATETIME NULL,
  `teamEventEndTime` DATETIME NULL,
  `teamEventTeamId` INT NULL,
  PRIMARY KEY (`teamEventId`),
  INDEX `teamEvent_teamId_idx` (`teamEventTeamId` ASC),
  CONSTRAINT `teamEvent_teamId`
    FOREIGN KEY (`teamEventTeamId`)
    REFERENCES `awayteam`.`team` (`teamId`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `awayteam`.`team_member`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `awayteam`.`team_member` ;

CREATE TABLE IF NOT EXISTS `awayteam`.`team_member` (
  `teamMemberId` INT NOT NULL AUTO_INCREMENT,
  `teamId` INT NULL,
  `userId` INT NULL,
  `manager` TINYINT(1) NULL,
  `pendingApproval` TINYINT(1) NULL,
  PRIMARY KEY (`teamMemberId`),
  INDEX `teamId_idx` (`teamId` ASC),
  INDEX `userId_idx` (`userId` ASC),
  CONSTRAINT `teamMembersTeamId`
    FOREIGN KEY (`teamId`)
    REFERENCES `awayteam`.`team` (`teamId`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `teamMembersUserId`
    FOREIGN KEY (`userId`)
    REFERENCES `awayteam`.`user` (`userId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `awayteam`.`expense`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `awayteam`.`expense` ;

CREATE TABLE IF NOT EXISTS `awayteam`.`expense` (
  `expenseId` INT NOT NULL AUTO_INCREMENT,
  `description` VARCHAR(255) NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `expDate` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `teamId` INT NOT NULL,
  `userId` INT NOT NULL,
  `receipt` MEDIUMBLOB NULL,
  `expType` ENUM('breakfast','lunch','dinner','snack','other') NOT NULL DEFAULT 'other',
  PRIMARY KEY (`expenseId`),
  INDEX `teamId_idx` (`teamId` ASC),
  INDEX `userId_idx` (`userId` ASC),
  CONSTRAINT `ExpenseTeamId`
    FOREIGN KEY (`teamId`)
    REFERENCES `awayteam`.`team` (`teamId`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `ExpenseUserId`
    FOREIGN KEY (`userId`)
    REFERENCES `awayteam`.`user` (`userId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
