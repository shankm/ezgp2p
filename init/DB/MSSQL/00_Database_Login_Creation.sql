USE [master]
GO

DECLARE @mdfName VARCHAR(500) = 'C:\DB\[IAG]\[IAG].mdf';
DECLARE @ldfName VARCHAR(500) = 'C:\DB\[IAG]\[IAG].ldf';

SELECT @mdfName = REPLACE(REPLACE(@mdfName, '[', ''), ']', ''),
	   @ldfName = REPLACE(REPLACE(@ldfName, '[', ''), ']', '')

DECLARE @sql NVARCHAR(MAX) = '
CREATE DATABASE [IAG]
ON (
	NAME = IAG_DATA,
	FILENAME = ''' + @mdfName + ''',
	SIZE = 10MB,
	FILEGROWTH = 5MB
)
LOG ON (
	NAME = IAG_LOG,
	FILENAME = ''' + @ldfName + ''',
	SIZE = 5MB,
	FILEGROWTH = 5MB
);
'
EXEC sp_executesql @sql
GO

IF NOT EXISTS (SELECT 1 FROM dbo.syslogins WHERE name = 'iag_user')
	CREATE LOGIN [iag_user] WITH PASSWORD = 'iagpass', CHECK_POLICY = OFF, CHECK_EXPIRATION = OFF, DEFAULT_DATABASE = [IAG];
GO

USE [IAG]
GO

CREATE USER [iag_user] FROM LOGIN [iag_user];
GO

ALTER ROLE [db_datareader] ADD MEMBER [iag_user];
GO

ALTER ROLE [db_datawriter] ADD MEMBER [iag_user];
GO

GRANT EXECUTE ON DATABASE::[IAG] TO [iag_user];

USE [master]
GO

