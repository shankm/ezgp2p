package co.mattshank.iagp2p_middle.objects.dbconnection;

import java.util.Properties;

import co.mattshank.iagp2p_middle.objects.exceptions.UnsupportedDatabaseServerException;

public class P2PDBConnectionManager {
	Properties properties;
	String serverType, serverName, databaseName, username, password, tlsVersion, connectionUrl;
	int serverPort, timeoutSeconds;
	P2PDBConnectionInterface connection;

	public P2PDBConnectionManager(Properties properties) throws Exception {
		this.properties = properties;
		timeoutSeconds = Integer.parseInt(properties.getProperty("db_timeout_seconds"));
		serverType = properties.getProperty("iag_db_server_type");
		serverName = properties.getProperty("iag_db_server");
		serverPort = Integer.parseInt(properties.getProperty("iag_db_server_port"));
		databaseName = properties.getProperty("iag_db_database");
		username = properties.getProperty("iag_db_user");
		password = properties.getProperty("iag_db_password");
		tlsVersion = properties.getProperty("tls_version");
			
		System.out.print(serverType + "... ");
		System.out.flush();
		
		// initialize the connection
		switch (serverType) {
			case "MSSQL":		try {
									connection = new P2PMSSQLConnection(serverName, serverPort, databaseName, username, password, timeoutSeconds, tlsVersion);
								} catch (Exception e) {
									e.printStackTrace();
								}
								break;
			default:			throw new UnsupportedDatabaseServerException(serverType);
		}
	}
	
	public P2PDBConnectionInterface getConnection() throws Exception {
		return connection;
	}
}
