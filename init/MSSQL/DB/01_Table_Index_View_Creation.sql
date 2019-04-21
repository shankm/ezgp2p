USE [IAG]
GO

-- DROP ALL
-- INDEXES
DROP INDEX IF EXISTS tbITAGRecords.IX_tbITAGRecords_TagAgencyID_TagNumber_INC
DROP INDEX IF EXISTS tbICLPRecords.IX_tbICLPRecords_LicensePlate_LicensePlateStateAbbrev_LicensePlateType_INC
DROP INDEX IF EXISTS tbITGURecords.IX_tbITAGRecords_TagAgencyID_TagNumber_INC
-- TABLES/VIEWS
IF OBJECT_ID('tbITAGRecords') IS NOT NULL 
	DROP TABLE tbITAGRecords
IF OBJECT_ID('tbICLPRecords') IS NOT NULL 
	DROP TABLE tbICLPRecords
IF OBJECT_ID('tbITGURecords') IS NOT NULL 
	DROP TABLE tbITGURecords
IF OBJECT_ID('vwIAGFileLogHistory') IS NOT NULL 
	DROP VIEW vwIAGFileLogHistory
IF OBJECT_ID('tbIAGFileLogHistory') IS NOT NULL 
	DROP TABLE tbIAGFileLogHistory
IF OBJECT_ID('tbIAGFileLog') IS NOT NULL 
	DROP TABLE tbIAGFileLog
IF OBJECT_ID('stbState') IS NOT NULL 
	DROP TABLE stbState
IF OBJECT_ID('stbTagStatus') IS NOT NULL
	DROP TABLE stbTagStatus
IF OBJECT_ID('stbIAGFileType') IS NOT NULL
	DROP TABLE stbIAGFileType
IF OBJECT_ID('stbIAGFileStatus') IS NOT NULL
	DROP TABLE stbIAGFileStatus
IF OBJECT_ID('stbAgency') IS NOT NULL
	DROP TABLE stbAgency


---------------------------------------------------------------------
/* stbAgency */
---------------------------------------------------------------------
CREATE TABLE stbAgency (
	AgencyID SMALLINT,
	AgencyShortName VARCHAR(10),
	AgencyName VARCHAR(100),
	IsITAGExpected BIT DEFAULT(1),
	IsICLPExpected BIT DEFAULT(1),
	IsITGUExpected BIT DEFAULT(1),
	
	CONSTRAINT PK_stbAgency PRIMARY KEY (AgencyID),
	CONSTRAINT UQ_stbAgency_AgencyShortName UNIQUE (AgencyShortName),
	CONSTRAINT UQ_stbAgency_AgencyName UNIQUE (AgencyName)
)
GO

INSERT INTO stbAgency (AgencyID, AgencyShortName, AgencyName)
SELECT number, 'AG' + CAST(number AS VARCHAR), 'Agency ' + CAST(number AS VARCHAR)
FROM master..spt_values
WHERE type = 'P'
	AND number BETWEEN 1 AND 50
	AND NOT EXISTS (SELECT 1 FROM stbAgency WHERE AgencyID = number)
GO
---------------------------------------------------------------------


/* stbIAGFileType */
---------------------------------------------------------------------
CREATE TABLE stbIAGFileType (
	IAGFileTypeID TINYINT IDENTITY(1,1),
	IAGFileType VARCHAR(10),
	IAGFileTypeDesc VARCHAR(500),
	
	CONSTRAINT PK_stbIAGFileType PRIMARY KEY (IAGFileTypeID)
)
GO

INSERT INTO stbIAGFileType (IAGFileType, IAGFileTypeDesc)
VALUES	('ITAG', 'Customer tag status file'),
		('ICLP', 'Customer license plate file'),
		('ITGU', 'Customer tag status update file')
GO
---------------------------------------------------------------------


/* stbIAGFileStatus */
---------------------------------------------------------------------
CREATE TABLE stbIAGFileStatus (
	IAGFileStatusID TINYINT IDENTITY(1,1),
	IAGFileStatus VARCHAR(50),
	
	CONSTRAINT PK_stbIAGFileStatus PRIMARY KEY (IAGFileStatusID)
)
GO

INSERT INTO stbIAGFileStatus (IAGFileStatus)
VALUES	('Received'),
		('Validated'),
		('Loaded'),
		('Validation Error'),
		('Load Error')
GO

CREATE NONCLUSTERED INDEX IX_stbIAGFileStatus_IAGFileStatus ON stbIAGFileStatus (IAGFileStatus)
GO
---------------------------------------------------------------------


/* stbIAGFileStatus */
---------------------------------------------------------------------
CREATE TABLE stbTagStatus (
	TagStatusID TINYINT,
	TagStatus VARCHAR(50),
	
	CONSTRAINT PK_stbTagStatus PRIMARY KEY (TagStatusID)
)
GO

INSERT INTO stbTagStatus (TagStatusID, TagStatus)
VALUES	('1', 'Valid'),
		('2', 'Low Balance'),
		('3', 'Invalid'),
		('4', 'Lost/Stolen'),
		('5', 'Valid (Non-Toll Enabled)'),
		('6', 'Low Balance (Non-Toll Enabled)'),
		('7', 'Invalid (Non-Toll Enabled)'),
		('8', 'Lost/Stolen (Non-Toll Enabled)')
GO
---------------------------------------------------------------------


/* stbState */
---------------------------------------------------------------------
CREATE TABLE stbState (
	StateAbbrev VARCHAR(2),
	StateName VARCHAR(30),

	CONSTRAINT PK_stbState PRIMARY KEY (StateAbbrev)
)
GO

INSERT INTO stbState (StateName, StateAbbrev)
VALUES	 ('Alabama', 'AL'),('Alaska', 'AK'),('Arizona', 'AZ'),('Arkansas', 'AR'),('California', 'CA'),('Colorado', 'CO'),('Connecticut', 'CT')
		,('Delaware', 'DE'),('Florida', 'FL'),('Georgia', 'GA'),('Hawaii', 'HI'),('Idaho', 'ID'),('Illinois', 'IL'),('Indiana', 'IN'),('Iowa', 'IA')
		,('Kansas', 'KS'),('Kentucky', 'KY'),('Louisiana', 'LA'),('Maine', 'ME'),('Maryland', 'MD'),('Massachusetts', 'MA'),('Michigan', 'MI')
		,('Minnesota', 'MN'),('Mississippi', 'MS'),('Missouri', 'MO'),('Montana', 'MT'),('Nebraska', 'NE'),('Nevada', 'NV'),('New Hampshire', 'NH')
		,('New Jersey', 'NJ'),('New Mexico', 'NM'),('New York', 'NY'),('North Carolina', 'NC'),('North Dakota', 'ND'),('Ohio', 'OH'),('Oklahoma', 'OK')
		,('Oregon', 'OR'),('Pennsylvania', 'PA'),('Rhode Island', 'RI'),('South Carolina', 'SC'),('South Dakota', 'SD'),('Tennessee', 'TN'),('Texas', 'TX')
		,('Utah', 'UT'),('Vermont', 'VT'),('Virginia', 'VA'),('Washington', 'WA'),('West Virginia', 'WV'),('Wisconsin', 'WI'),('Wyoming', 'WY')
GO
---------------------------------------------------------------------

/* tbIAGFileLog */
---------------------------------------------------------------------
BEGIN
CREATE TABLE tbIAGFileLog (
	IAGFileID INT IDENTITY(1,1),
	IAGFileTypeID TINYINT,
	IAGFileStatusID TINYINT,
	IAGFileName VARCHAR(250),
	FileDateTime DATETIME2(0),
	FromAgencyID SMALLINT,
	ToAgencyID SMALLINT,
	RecordCount INT,
	UpdateDateTime DATETIME2(3) DEFAULT(SYSDATETIME()),
	Comments VARCHAR(MAX) DEFAULT(''),
	
	CONSTRAINT PK_tbIAGFileLog PRIMARY KEY (IAGFileID),
	CONSTRAINT FK_tbIAGFileLog_IAGFileTypeID_stbIAGFileType FOREIGN KEY (IAGFileTypeID) REFERENCES stbIAGFileType (IAGFileTypeID),
	CONSTRAINT FK_tbIAGFileLog_IAGFileStatusID_stbIAGFileStatus FOREIGN KEY (IAGFileStatusID) REFERENCES stbIAGFileStatus (IAGFileStatusID),
	CONSTRAINT FK_tbIAGFileLog_FromAgencyID_stbAgency FOREIGN KEY (FromAgencyID) REFERENCES stbAgency (AgencyID),
	CONSTRAINT FK_tbIAGFileLog_ToAgencyID_stbAgency FOREIGN KEY (ToAgencyID) REFERENCES stbAgency (AgencyID),
	CONSTRAINT UQ_tbIAGFileLog_FromAgencyID_FileName UNIQUE (FromAgencyID, IAGFileName)
)
END
GO

CREATE NONCLUSTERED INDEX IX_tbIAGFileLog_FromAgencyID ON tbIAGFileLog (FromAgencyID)
GO
CREATE NONCLUSTERED INDEX IX_tbIAGFileLog_IAGFileTypeID ON tbIAGFileLog (IAGFileTypeID)
GO
---------------------------------------------------------------------


/* tbIAGFileLogHistory */
---------------------------------------------------------------------
CREATE TABLE tbIAGFileLogHistory (
	IAGFileID INT,
	IAGFileTypeID TINYINT,
	IAGFileStatusID TINYINT,
	IAGFileName VARCHAR(250),
	FileDateTime DATETIME2(0),
	FromAgencyID SMALLINT,
	ToAgencyID SMALLINT,
	RecordCount INT,
	UpdateDateTime DATETIME2(3),
	Comments VARCHAR(MAX),
	
	CONSTRAINT PK_tbIAGFileLogHistory PRIMARY KEY (IAGFileID, UpdateDateTime),
	CONSTRAINT FK_tbIAGFileLogHistory_IAGFileTypeID_stbIAGFileType FOREIGN KEY (IAGFileTypeID) REFERENCES stbIAGFileType (IAGFileTypeID),
	CONSTRAINT FK_tbIAGFileLogHistory_IAGFileStatusID_stbAIGFileStatus FOREIGN KEY (IAGFileStatusID) REFERENCES stbIAGFileStatus (IAGFileStatusID),
	CONSTRAINT FK_tbIAGFileLogHistory_FromAgencyID_stbAgency FOREIGN KEY (FromAgencyID) REFERENCES stbAgency (AgencyID),
	CONSTRAINT FK_tbIAGFileLogHistory_ToAgencyID_stbAgency FOREIGN KEY (ToAgencyID) REFERENCES stbAgency (AgencyID)
)
GO

CREATE TRIGGER TR_tbIAGFileLog_tbIAGFileLogHistory ON tbIAGFileLog 
AFTER UPDATE AS
BEGIN
	SET NOCOUNT ON;
	
	INSERT INTO tbIAGFileLogHistory (IAGFileID, IAGFileTypeID, IAGFileStatusID, IAGFileName, FileDateTime, FromAgencyID, ToAgencyID, RecordCount, UpdateDateTime, Comments)
	SELECT d.IAGFileID, d.IAGFileTypeID, d.IAGFileStatusID, d.IAGFileName, d.FileDateTime, d.FromAgencyID, d.ToAgencyID, d.RecordCount, d.UpdateDateTime, d.Comments
	FROM tbIAGFileLog fl
		INNER JOIN DELETED d
			ON fl.IAGFileID = d.IAGFileID 
END
GO

CREATE NONCLUSTERED INDEX IX_tbIAGFileLogHistory_FromAgencyID ON tbIAGFileLogHistory (FromAgencyID)
GO
CREATE NONCLUSTERED INDEX IX_tbIAGFileLogHistory_IAGFileTypeID ON tbIAGFileLogHistory (IAGFileTypeID)
GO
---------------------------------------------------------------------


/* vwIAGFileLogHistory */
---------------------------------------------------------------------
CREATE VIEW vwIAGFileLogHistory AS
	SELECT IAGFileID, IAGFileTypeID, IAGFileStatusID, IAGFileName, FileDateTime, FromAgencyID, ToAgencyID, RecordCount, UpdateDateTime, Comments
	FROM tbIAGFileLog
	UNION ALL
	SELECT IAGFileID, IAGFileTypeID, IAGFileStatusID, IAGFileName, FileDateTime, FromAgencyID, ToAgencyID, RecordCount, UpdateDateTime, Comments
	FROM tbIAGFileLogHistory;
GO
---------------------------------------------------------------------


/* tbITAGRecords */
---------------------------------------------------------------------
CREATE TABLE tbITAGRecords (
	IAGFileID INT,
	TagAgencyID SMALLINT,
	TagNumber INT,
	TagStatusID TINYINT,
	IsValidForNonTollParking BIT,
	IsValidForNonTollNonParking BIT,
	DiscountPlanID SMALLINT,

	CONSTRAINT PK_tbITAGRecords PRIMARY KEY (IAGFileID, TagAgencyID, TagNumber),
	CONSTRAINT FK_tbITAGRecords_TagAgencyID_stbAgency FOREIGN KEY (TagAgencyID) REFERENCES stbAgency (AgencyID),
	CONSTRAINT FK_tbITAGRecords_TagStatusID_stbTagStatusID FOREIGN KEY (TagStatusID) REFERENCES stbTagStatus (TagStatusID)
)
GO

CREATE NONCLUSTERED INDEX IX_tbITAGRecords_TagAgencyID_TagNumber_INC
ON tbITAGRecords (TagAgencyID, TagNumber) INCLUDE (TagStatusID, IsValidForNonTollParking, IsValidForNonTollNonParking, DiscountPlanID)
GO
---------------------------------------------------------------------


/* tbICLPREcords */
---------------------------------------------------------------------
CREATE TABLE tbICLPRecords (
	IAGFileID INT,
	TagAgencyID SMALLINT,
	TagNumber INT,
	LicensePlate VARCHAR(15),
	LicensePlateStateAbbrev VARCHAR(2),
	LicensePlateType VARCHAR(8),

	CONSTRAINT PK_tbICLPRecords PRIMARY KEY (IAGFileID, LicensePlate, LicensePlateStateAbbrev, LicensePlateType, TagAgencyID, TagNumber),
	CONSTRAINT FK_tbICLPRecords_TagAgencyID_stbAgency FOREIGN KEY (TagAgencyID) REFERENCES stbAgency (AgencyID),
	--CONSTRAINT FK_tbICLPREcords_LicensePlateStateAbbrev_stbState FOREIGN KEY (LicensePlateStateAbbrev) REFERENCES stbState (StateAbbrev)
)
GO

CREATE NONCLUSTERED INDEX IX_tbICLPRecords_LicensePlate_LicensePlateStateAbbrev_LicensePlateType_INC
ON tbICLPRecords (LicensePlate, LicensePlateStateAbbrev, LicensePlateType) INCLUDE (TagAgencyID, TagNumber)
GO
---------------------------------------------------------------------


/* tbITGURecords */
---------------------------------------------------------------------
CREATE TABLE tbITGURecords (
	IAGFileID INT,
	TagAgencyID SMALLINT,
	TagNumber INT,
	TagStatusID TINYINT,
	IsValidForNonTollParking BIT,
	IsValidForNonTollNonParking BIT,
	DiscountPlanID SMALLINT,

	CONSTRAINT PK_tbITGURecords PRIMARY KEY (IAGFileID, TagAgencyID, TagNumber),
	CONSTRAINT FK_tbITGURecords_AgencyID_stbAgency FOREIGN KEY (TagAgencyID) REFERENCES stbAgency (AgencyID),
	CONSTRAINT FK_tbITGURecords_TagStatusID_stbTagStatusID FOREIGN KEY (TagStatusID) REFERENCES stbTagStatus (TagStatusID)
)
GO

CREATE NONCLUSTERED INDEX IX_tbITGURecords_TagAgencyID_TagNumber_INC
ON tbITGURecords (TagAgencyID, TagNumber) INCLUDE (TagStatusID, IsValidForNonTollParking, IsValidForNonTollNonParking, DiscountPlanID)
GO
---------------------------------------------------------------------

