package co.mattshank.iagp2p_middle;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import co.mattshank.iagp2p_middle.objects.P2PProperties;
import co.mattshank.iagp2p_middle.objects.exceptions.UnsupportedDatabaseServerException;

public class P2PMiddleManager 
{
	static boolean runInfinite = true;
	
    public static void main( String[] args ) throws UnsupportedDatabaseServerException, InterruptedException
    {
    	Properties properties;
		String configPath;
		ArrayList<P2PFileManager> fileManagers = new ArrayList<P2PFileManager>();
		
		System.out.println("---------------------------------------------------------------");
		System.out.println("\t   iagp2p-middle (v1.0)");
		System.out.println("---------------------------------------------------------------");
		System.out.println("\t  Developed by Matt Shank");
		System.out.println("\twww.github.com/shankm/iagp2p");
		System.out.println("\t GNU GENERAL PUBLIC LICENSE");
		System.out.println("\t  Version 3, 29 June 2007");
		System.out.println("---------------------------------------------------------------");
    	
		configPath = args.length > 0 ? args[0] : "src/main/resources/middle.properties";
		System.out.println("properties: " + configPath + "\n");
				
		// Gather properties to use for duration of execution
		List<String> requiredProperties = new ArrayList<String>();
		// Define list of required properties
		requiredProperties.add("home_agency_id");
		requiredProperties.add("files_to_load_directory");
		requiredProperties.add("load_failure_directory");
		requiredProperties.add("archive_directory");
		requiredProperties.add("iag_db_server_type");
		requiredProperties.add("iag_db_server");
		requiredProperties.add("iag_db_server_port");
		requiredProperties.add("iag_db_database");
		requiredProperties.add("iag_db_user");
		requiredProperties.add("iag_db_password");
		requiredProperties.add("db_timeout_seconds");
		
		// Configurable properties from config.properties
		properties = P2PProperties.loadProperties(configPath, requiredProperties);
		
		// If the required properties are not set, exit with error.
		if (properties == null) {
			System.err.println("All required properties must be defined in middle.properties:");
			for (String s : requiredProperties) {
				if (s != null)
					System.err.println("- " + s);
			}
			System.exit(1);
		}
		
		// Register applicable JDBC driver
		switch(properties.getProperty("iag_db_server_type")) {
			case "MSSQL":	try {
									DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());
								} catch (Exception e) {
									System.out.println("Failed to register Microsoft SQL Server JDBC driver. Aborting.");
									System.exit(1);
								}
							break;
			default:		throw new UnsupportedDatabaseServerException(properties.getProperty("iag_db_server_type"));
		}
		
		do {
			int success = 0, failure = 0;
			
			// Parse and load any new files that have come in
			fileManagers = parseLoadIncomingFiles(properties);
			
			if(fileManagers.size() > 0) {
				for(P2PFileManager m : fileManagers) {
					if(m.file.getLoadedIntoDB() == true)
						success++;
					else
						failure++;	
				}
				System.out.println(success + " of " + (success + failure) + " files loaded successfully.");
			}
			
			Thread.sleep(5000);
		} while(runInfinite);
    }
    
    private static File[] sortFilesChronologically (File[] files) {
    	File[] sortedFiles = files;
    	
    	Arrays.sort(sortedFiles, new Comparator<File>() {
    		public int compare(File f1, File f2) {
    			return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
    		}
    	});
    
    	return sortedFiles;
    }
    
    private static ArrayList<P2PFileManager> parseLoadIncomingFiles(Properties properties) {
    	ArrayList<P2PFileManager> fileParsers = new ArrayList<P2PFileManager>();
    	
    	File[] listOfFiles = sortFilesChronologically(new File(
    			properties.getProperty("files_to_load_directory")).listFiles(
    					new FilenameFilter() {
    						public boolean accept(File dir, String name) {
    							boolean temp = false;
    							if (name.toLowerCase().endsWith(".itag") || name.toLowerCase().endsWith(".iclp") ||	name.toLowerCase().endsWith(".itgu"))
    								temp = true;
    							return temp;
    						}
    					}));
    	
    	if (listOfFiles.length > 0) {
    		System.out.println("------------------------------------------");
	    	for (File f : listOfFiles) {
				fileParsers.add(new P2PFileManager(properties, f));
	    	}
	    	System.out.println("------------------------------------------");
	    	System.out.println();
    	}
    	
    	return fileParsers;
    }   
}
