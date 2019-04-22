USE master
GO
xp_readerrorlog 0, 1, N'Server is listening on' 
GO


SELECT COUNT(*)
FROM [IAG].dbo.tbITAGRecords a

SELECT *
FROM [IAG].dbo.tbIAGFileLog a

SELECT *
FROM [IAG].dbo.vwIAGFileLogHistory
ORDER BY IAGFileID, UpdateDateTime


TRUNCATE TABLE [IAG].dbo.tbITAGRecords
TRUNCATE TABLE [IAG].dbo.tbITGURecords
TRUNCATE TABLE [IAG].dbo.tbICLPRecords
TRUNCATE TABLE [IAG].dbo.tbIAGFileLog
TRUNCATE TABLE [IAG].dbo.tbIAGFileLogHistory
