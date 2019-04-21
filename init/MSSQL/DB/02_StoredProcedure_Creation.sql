USE [IAG]
GO

/* uspIAGFileLogInsUpd */
---------------------------------------------------------------------
IF OBJECT_ID('uspIAGFileLogInsUpd') IS NOT NULL
	DROP PROCEDURE uspIAGFileLogInsUpd
GO
CREATE PROCEDURE uspIAGFileLogInsUpd
	@IAGFileTypeID INT,
	@IAGFileStatus VARCHAR(10),
	@IAGFileName VARCHAR(250),
	@FileDateTime DATETIME,
	@FromAgencyID SMALLINT,
	@ToAgencyID SMALLINT,
	@RecordCount INT = NULL,
	@Comments VARCHAR(MAX) = ''
AS
SET NOCOUNT ON;

DECLARE @IAGFileID INT;

BEGIN TRY
	SELECT @IAGFileID = IAGFileID FROM tbIAGFileLog WHERE FromAgencyID = @FromAgencyID AND IAGFileName = @IAGFileName
	
	IF @IAGFileID IS NOT NULL
	BEGIN
		UPDATE [dbo].[tbIAGFileLog]
		SET	[IAGFileTypeID] = @IAGFileTypeID,
			[IAGFileStatusID] = (SELECT IAGFileStatusID FROM stbIAGFileStatus WHERE IAGFileStatus = @IAGFileStatus),
			[FileDateTime] = @FileDateTime,
			[UpdateDateTime] = SYSDATETIME(),
			[RecordCount] = ISNULL(@RecordCount, RecordCount),
			[Comments] = ISNULL(@Comments, Comments)
		WHERE IAGFileID = @IAGFileID
	END
	ELSE
	BEGIN
		INSERT INTO [dbo].[tbIAGFileLog] ([IAGFileTypeID],[IAGFileStatusID],[IAGFileName],[FileDateTime],[FromAgencyID],[ToAgencyID],[RecordCount],[UpdateDateTime],[Comments])
		SELECT @IAGFileTypeID, (SELECT IAGFileStatusID FROM stbIAGFileStatus WHERE IAGFileStatus = @IAGFileStatus), @IAGFileName, @FileDateTime,
				@FromAgencyID, @ToAgencyID, NULL, SYSDATETIME(), @Comments

		SET @IAGFileID = SCOPE_IDENTITY()
	END

	RETURN @IAGFileID;
END TRY
BEGIN CATCH
	THROW;
END CATCH
GO
---------------------------------------------------------------------


/* uspIAGFileLogGet */
---------------------------------------------------------------------
CREATE PROCEDURE uspIAGFileLogCheckAlreadyLoaded
	@IAGFileName VARCHAR(250),
	@FromAgencyID SMALLINT
AS
DECLARE @IAGFileID INT

SELECT @IAGFileID = IAGFileID
FROM tbIAGFileLog
WHERE FromAgencyID = @FromAgencyID
	AND IAGFileName = @IAGFileName
	AND IAGFileStatusID = (SELECT IAGFileStatusID FROM stbIAGFileStatus WHERE IAGFileStatus = 'Loaded')

RETURN ISNULL(@IAGFileID, -1)
GO
---------------------------------------------------------------------


/* uspITAGRecordsIns */
---------------------------------------------------------------------
IF OBJECT_ID('uspITAGRecordsIns') IS NOT NULL
	DROP PROCEDURE uspITAGRecordsIns
GO
IF TYPE_ID('udtITAGRecords') IS NOT NULL
	DROP TYPE udtITAGRecords
CREATE TYPE udtITAGRecords AS TABLE (
	[IAGFileID] [int] NOT NULL,
	[TagAgencyID] [smallint] NOT NULL,
	[TagNumber] [int] NOT NULL,
	[TagStatusID] [tinyint] NULL,
	[IsValidForNonTollParking] [bit] NULL,
	[IsValidForNonTollNonParking] [bit] NULL,
	[DiscountPlanID] [smallint] NULL
)
GO
CREATE PROCEDURE uspITAGRecordsIns
	@t udtITAGRecords READONLY
AS
SET NOCOUNT ON;

INSERT INTO [dbo].[tbITAGRecords]
           ([IAGFileID],[TagAgencyID],[TagNumber],[TagStatusID],[IsValidForNonTollParking],[IsValidForNonTollNonParking],[DiscountPlanID])
SELECT DISTINCT [IAGFileID],[TagAgencyID],[TagNumber],[TagStatusID],[IsValidForNonTollParking],[IsValidForNonTollNonParking],[DiscountPlanID]
FROM @t
GO
---------------------------------------------------------------------


/* uspITGURecordsIns */
---------------------------------------------------------------------
IF OBJECT_ID('uspITGURecordsIns') IS NOT NULL
	DROP PROCEDURE uspITGURecordsIns
GO
IF TYPE_ID('udtITGURecords') IS NOT NULL
	DROP TYPE udtITGURecords
CREATE TYPE udtITGURecords AS TABLE (
	[IAGFileID] [int] NOT NULL,
	[TagAgencyID] [smallint] NOT NULL,
	[TagNumber] [int] NOT NULL,
	[TagStatusID] [tinyint] NULL,
	[IsValidForNonTollParking] [bit] NULL,
	[IsValidForNonTollNonParking] [bit] NULL,
	[DiscountPlanID] [smallint] NULL
)
GO
CREATE PROCEDURE uspITGURecordsIns
	@t udtITGURecords READONLY
AS
SET NOCOUNT ON;

INSERT INTO [dbo].[tbITGURecords] ([IAGFileID],[TagAgencyID],[TagNumber],[TagStatusID],[IsValidForNonTollParking],[IsValidForNonTollNonParking],[DiscountPlanID])
SELECT DISTINCT [IAGFileID],[TagAgencyID],[TagNumber],[TagStatusID],[IsValidForNonTollParking],[IsValidForNonTollNonParking],[DiscountPlanID]
FROM @t
GO
---------------------------------------------------------------------


/* uspICLPRecordsIns */
---------------------------------------------------------------------
IF OBJECT_ID('uspICLPRecordsIns') IS NOT NULL
	DROP PROCEDURE uspICLPRecordsIns
GO
IF TYPE_ID('udtICLPRecords') IS NOT NULL
	DROP TYPE udtICLPRecords
CREATE TYPE udtICLPRecords AS TABLE (
	[IAGFileID] [int] NOT NULL,
	[TagAgencyID] [smallint] NOT NULL,
	[TagNumber] [int] NOT NULL,
	[LicensePlate] [varchar](15) NOT NULL,
	[LicensePlateStateAbbrev] [varchar](2) NOT NULL,
	[LicensePlateType] [varchar](8) NOT NULL
)
GO
CREATE PROCEDURE uspICLPRecordsIns
	@t udtICLPRecords READONLY
AS
SET NOCOUNT ON;

INSERT INTO [dbo].[tbICLPRecords] ([IAGFileID],[TagAgencyID],[TagNumber],[LicensePlate],[LicensePlateStateAbbrev],[LicensePlateType])
SELECT DISTINCT [IAGFileID],[TagAgencyID],[TagNumber],[LicensePlate],[LicensePlateStateAbbrev],[LicensePlateType]
FROM @t
GO
---------------------------------------------------------------------



