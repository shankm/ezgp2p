package co.mattshank.iagp2p_middle.objects.dbconnection;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;

import com.microsoft.sqlserver.jdbc.SQLServerCallableStatement;
import com.microsoft.sqlserver.jdbc.SQLServerDataTable;

import co.mattshank.iagp2p_middle.objects.P2PFile;
import co.mattshank.iagp2p_middle.objects.P2PFileLogRecord;
import co.mattshank.iagp2p_middle.objects.filedata.P2PICLPData;
import co.mattshank.iagp2p_middle.objects.filedata.P2PITAGData;
import co.mattshank.iagp2p_middle.objects.filedata.P2PITGUData;

public class P2PMSSQLConnection implements P2PDBConnectionInterface {
	String serverName, databaseName, username, password, tlsVersion, connectionUrl;
	int serverPort, timeoutSeconds;
	Connection connection;
	
    public P2PMSSQLConnection(String serverName, int serverPort, String databaseName, String username, String password, int timeoutSeconds, String tlsVersion) {
    	this.serverName = serverName;
    	this.serverPort = serverPort;
    	this.databaseName = databaseName;
    	this.username = username;
    	this.password = password;
    	this.timeoutSeconds = timeoutSeconds;
    	this.tlsVersion = tlsVersion;
    	
    	setConnectionString();
    	
    	try {
    		open();
    	} catch (Exception e) {
    		System.out.println("FAIL");
    		e.printStackTrace();
    		System.exit(1);
    	}
    	
    	if(connection != null) {
    		System.out.println("SUCCESS");
    	}
    	else {
    		System.out.println("FAIL");
    		System.exit(1);
    	}
    }
    
    private void setConnectionString() {
    	connectionUrl =
                "jdbc:sqlserver://" + serverName + ":" + serverPort + ";"
                + "databaseName=" + databaseName + ";"
                + "user=" + username + ";"
                + "password=" + password + ";"
                + "sslProtocol=TLSv" + tlsVersion + ";"
                + "loginTimeout=" + timeoutSeconds + ";";
    }
    
    /**
     * Opens a connection to the database
     */
    public void open() {
		try {
			connection = DriverManager.getConnection(connectionUrl);
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
    }
    
    public int createUpdateFileRecord(P2PFileLogRecord loggedFile) throws SQLException {
    	int fileID;
    	
    	try (CallableStatement proc = connection.prepareCall("{ ? = call dbo.uspIAGFileLogInsUpd(?,?,?,?,?,?,?,?) }")) {
    		proc.registerOutParameter(1, Types.INTEGER);
    		proc.setInt(2, loggedFile.getFileTypeID());
    		proc.setString(3, loggedFile.getFileStatus());
    		proc.setString(4, loggedFile.getFileName());
    		proc.setTimestamp(5, java.sql.Timestamp.valueOf(loggedFile.getFileDate()));
    		proc.setInt(6, loggedFile.getFromAgencyID());
    		proc.setInt(7, loggedFile.getToAgencyID());
    		if(loggedFile.getRecordCount() > 0)
    			proc.setInt(8, loggedFile.getRecordCount());
    		else
    			proc.setObject(8, null);
    		proc.setString(9, loggedFile.getComments());
    		
    		proc.execute();
        	fileID = proc.getInt(1);
        	return fileID;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return -1;
    }
    
    public boolean fileAlreadyExists(P2PFileLogRecord loggedFile) throws SQLException {
    	try (CallableStatement proc = connection.prepareCall("{ ? = call dbo.uspIAGFileLogCheckAlreadyLoaded(?,?) }")) {
    		proc.registerOutParameter(1, Types.INTEGER);
    		proc.setString(2, loggedFile.getFileName());
    		proc.setInt(3, loggedFile.getFromAgencyID());
    		
    		proc.execute();
    		if(proc.getInt(1) == -1)
    			return false;
    		return true;
    	}
    }
    
    public void loadFileIntoDatabase(P2PFile file) throws SQLException {
    	switch (file.getFileLogRecord().getFileTypeName()) {
    		case "ITAG":	loadITAG(file);
    						break;
    		case "ICLP":	loadICLP(file);
    						break;
    		case "ITGU":	loadITGU(file);
    						break;
    	}
    }
    
    public void loadITAG(P2PFile file) throws SQLException {
    	SQLServerDataTable table = new SQLServerDataTable();
    	P2PITAGData data = (P2PITAGData) file.getBody();
    	
    	table.addColumnMetadata("IAGFileID", Types.INTEGER);
    	table.addColumnMetadata("TagAgencyID", Types.SMALLINT);
    	table.addColumnMetadata("TagNumber", Types.INTEGER);
    	table.addColumnMetadata("TagStatusID", Types.TINYINT);
    	table.addColumnMetadata("IsValidForNonTollParking", Types.BIT);
    	table.addColumnMetadata("IsValidForNonTollNonParking", Types.BIT);
    	table.addColumnMetadata("DiscountPlanID", Types.SMALLINT);
    	
    	for(int i = 0; i < file.getRecordCount(); i++) {
    		table.addRow(file.getFileLogRecord().getFileID(),
    					 data.tagAgencyID.get(i),
    					 data.tagNumber.get(i),
    					 data.tagStatusID.get(i),
    					 0, 0, 0
    				);
    	}
    	
    	try (CallableStatement stmt = connection.prepareCall("{ call dbo.uspITAGRecordsIns(?) }")) {
    		((SQLServerCallableStatement) stmt).setStructured(1, "dbo.udtITAGRecords", table);
    		stmt.execute();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public void loadICLP(P2PFile file) throws SQLException {
    	SQLServerDataTable table = new SQLServerDataTable();
    	P2PICLPData data = (P2PICLPData) file.getBody();
    	
    	table.addColumnMetadata("IAGFileID", Types.INTEGER);
    	table.addColumnMetadata("TagAgencyID", Types.SMALLINT);
    	table.addColumnMetadata("TagNumber", Types.INTEGER);
    	table.addColumnMetadata("LicensePlate", Types.VARCHAR);
    	table.addColumnMetadata("LicensePlateStateAbbrev", Types.VARCHAR);
    	table.addColumnMetadata("LicensePlateType", Types.VARCHAR);
    	
    	for(int i = 0; i < file.getRecordCount(); i++) {
    		table.addRow(file.getFileLogRecord().getFileID(),
    					 data.tagAgencyID.get(i),
    					 data.tagNumber.get(i),
    					 data.licensePlate.get(i),
    					 data.licensePlateState.get(i),
    					 data.licensePlateType.get(i)
    				);
    	}
    	
    	try (CallableStatement stmt = connection.prepareCall("{ call dbo.uspICLPRecordsIns(?) }")) {
    		((SQLServerCallableStatement) stmt).setStructured(1, "dbo.udtICLPRecords", table);
    		stmt.execute();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public void loadITGU(P2PFile file) throws SQLException {
    	SQLServerDataTable table = new SQLServerDataTable();
    	P2PITGUData data = (P2PITGUData) file.getBody();
    	
    	table.addColumnMetadata("IAGFileID", Types.INTEGER);
    	table.addColumnMetadata("TagAgencyID", Types.SMALLINT);
    	table.addColumnMetadata("TagNumber", Types.INTEGER);
    	table.addColumnMetadata("TagStatusID", Types.TINYINT);
    	table.addColumnMetadata("IsValidForNonTollParking", Types.BIT);
    	table.addColumnMetadata("IsValidForNonTollNonParking", Types.BIT);
    	table.addColumnMetadata("DiscountPlanID", Types.SMALLINT);
    	
    	for(int i = 0; i < file.getRecordCount(); i++) {
    		table.addRow(file.getFileLogRecord().getFileID(),
    					 data.tagAgencyID.get(i),
    					 data.tagNumber.get(i),
    					 data.tagStatusID.get(i),
    					 0, 0, 0
    				);
    	}
    	
    	try (CallableStatement stmt = connection.prepareCall("{ call dbo.uspITGURecordsIns(?) }")) {
    		((SQLServerCallableStatement) stmt).setStructured(1, "dbo.udtITGURecords", table);
    		stmt.execute();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    
    public void close() {
    	try {
    		connection.close();
    	} catch (Exception e) {
    		e.printStackTrace();
    		return;
    	}
    }

}
