package co.mattshank.iagp2p_middle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import co.mattshank.iagp2p_middle.objects.P2PFile;
import co.mattshank.iagp2p_middle.objects.dbconnection.P2PDBConnectionInterface;
import co.mattshank.iagp2p_middle.objects.dbconnection.P2PDBConnectionManager;
import co.mattshank.iagp2p_middle.objects.exceptions.UnsupportedFileTypeException;

public class P2PFileManager {
	private static final boolean performDBFileLogUpdates = true;
	private static final boolean performDBFileLoad = true;
	
	static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
	
	P2PFile file;
	Properties properties;
	String serverType, serverName, databaseName, username, password, connectionUrl;
	int serverPort;
	P2PDBConnectionManager connectionManager;
	P2PDBConnectionInterface connection;
	
	public P2PFileManager (Properties properties, File file) {
		System.out.println();
		System.out.println(dateFormatter.format(LocalDateTime.now()));
		System.out.println("Loading " + file.getPath());
		
		this.properties = properties;
		
		try {
			this.file = new P2PFile(this.properties, file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedFileTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.print("Initializing DB connection... ");
		System.out.flush();
		try {
			connectionManager = new P2PDBConnectionManager(properties);
			connection = connectionManager.getConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			loadFile();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void loadFile() throws Exception {
		file.readHeader();
		
		if(connection.fileAlreadyExists(file.getFileLogRecord())) {
			System.out.println("File has already been loaded");
			if(!moveFileToArchive()) {
				moveFileToError();
			}
			return;
		}
		
		file.getFileLogRecord().setFileStatus("Received");
		if (performDBFileLogUpdates) {
			file.getFileLogRecord().setFileID(connection.createUpdateFileRecord(file.getFileLogRecord()));
		}
		
		file.readBody();
		
		file.getFileLogRecord().setFileStatus("Validated");
		if (performDBFileLogUpdates) {
			file.getFileLogRecord().setFileID(connection.createUpdateFileRecord(file.getFileLogRecord()));
		}
		
		file.loadedIntoApp = true;
		
		if(loadFileIntoDatabase()) {
			file.loadedIntoDB = true;
			moveFileToArchive();
		}
	}
	
	public boolean loadFileIntoDatabase() throws Exception {
		System.out.print("Loading data into database... ");
		System.out.flush();
		
		if (performDBFileLoad) {
			try {
				connection.loadFileIntoDatabase(file);
			} catch (Exception e) {
				System.out.println("FAIL");
				return false;
			}
		}
		
		file.getFileLogRecord().setFileStatus("Loaded");
		
		if (performDBFileLogUpdates) {
			file.getFileLogRecord().setFileID(connection.createUpdateFileRecord(file.getFileLogRecord()));
		}
		
		connection.close();
		System.out.println("SUCCESS");
		return true;
	}
	
	private boolean moveFileToArchive() throws IOException {
		System.out.print("Archiving file... ");
		System.out.flush();
		
		File finalFile;
		String destinationPath = properties.getProperty("archive_directory") + file.getFileName();
		Path temp = Files.move(Paths.get(file.getFilePath()), Paths.get(destinationPath));
		finalFile = new File(destinationPath);
		if(temp != null) {
			System.out.println("SUCCESS");
			file.setFile(finalFile);
		}
		else {
			System.err.println("Problem moving file to load directory");
			return false;
		}
		
		return true;
	}
	
	private void moveFileToError() throws IOException {
		File finalFile;
		String destinationPath = properties.getProperty("error_directory") + file.getFileName();
		Path temp = Files.move(Paths.get(file.getFilePath()), Paths.get(destinationPath));
		finalFile = new File(destinationPath);
		if(temp != null) {
			System.out.println("SUCCESS");
			file.setFile(finalFile);
		}
	}
	
	public P2PFile getFileData() {
		return file;
	}
}
