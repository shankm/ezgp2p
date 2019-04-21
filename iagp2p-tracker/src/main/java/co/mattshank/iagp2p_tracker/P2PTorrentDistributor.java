package co.mattshank.iagp2p_tracker;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class P2PTorrentDistributor extends Thread {
	private static final boolean runInfinitely = true;
	
	Properties properties;
	int port;
	int newTorrents;
	String torrentDirectoryPath;
	ServerSocket serverSocket;
	Instant lastFetchedFiles;
	ArrayList<File> torrentCollection;
	final int torrentCollectionStaleSeconds;

	public P2PTorrentDistributor(Properties properties) {
		this.properties = properties;
		this.port = Integer.parseInt(properties.getProperty("torrent_distributor_port"));
		this.torrentDirectoryPath = properties.getProperty("torrent_directory");
		torrentCollectionStaleSeconds = Integer.parseInt(properties.getProperty("torrent_collection_stale_seconds"));
		newTorrents = 0;
		fetchTorrentFileCollection();
		
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public void run() {
    	do {  
            String torrentFileName = null;
    		Socket clientSocket = null;
    		int bytesRead;
    		byte[] buffer = null;
    		String requestType = null;
    		InputStream in = null;
    		DataInputStream clientData = null;
    		OutputStream out = null;
    		DataOutputStream serverData = null;
    		OutputStream fileOutput = null;
    		
            try {
            	deleteHalfCreatedFiles();
            	
            	clientSocket = serverSocket.accept();
               
	            in = clientSocket.getInputStream();
	            clientData = new DataInputStream(in);
	            out = clientSocket.getOutputStream();
	            serverData = new DataOutputStream(out);
	            
	            requestType = clientData.readUTF();
	            
	            // Client wishes to send a new torrent file
	            if(requestType.equals("PUT")) {	
	            	torrentFileName = clientData.readUTF();
		            long size = clientData.readLong();
		            
		            // If file does not already exist at the tracker
		            if(!(new File(torrentDirectoryPath + torrentFileName).exists())) {
		            	serverData.writeUTF("OK");
		            	serverData.flush();
		            	
		            	System.out.print("Receiving file " + torrentFileName + "... ");
		            	System.out.flush();
		            	
		            	buffer = new byte[1024];
		            	
		            	File newFileTemp = new File(torrentDirectoryPath + torrentFileName + ".part");
		            	File newFileFinal = new File(torrentDirectoryPath + torrentFileName);
		            	
		            	fileOutput = new FileOutputStream(newFileTemp);   
			            
			            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {     
			            	fileOutput.write(buffer, 0, bytesRead);     
			                size -= bytesRead;     
			            }
			            
		            	fileOutput.close();
		            	newFileTemp.renameTo(newFileFinal);
		            	newTorrents++;
		            	
			            System.out.println("SUCCESS");
		            }
		            else {
		            	serverData.writeUTF("NOK");
		            }
	            }
	            // If the client is checking for new files
	            else if(requestType.contentEquals("GET")) {
	            	ArrayList<String> filesRequested = new ArrayList<String>();
	            	ArrayList<String> agencyFileMarkersToExclude = new ArrayList<String>();
	            	
	            	// Get the requested number of minutes to look back for files received by the tracker
	            	int nHours = clientData.readInt();
	            	
	            	// If the collection is stale, fetch the collection of files again
	            	if(lastFetchedFiles.plusSeconds(torrentCollectionStaleSeconds).compareTo(Instant.now()) < 0)
	            		fetchTorrentFileCollection();
	            	
	            	// Load the agency file markers to exclude
	            	String temp;
	            	while (!(temp = clientData.readUTF()).equals("EOR")) {     
	            		agencyFileMarkersToExclude.add(temp);
		            }
	            	
	            	// Send the collection of files we have
	            	//		If client passed 0 for nHours, send everything
	            	//		Else send only files that were created in the last nHours
	            	for(File f : torrentCollection) {
	            		if(!agencyFileMarkersToExclude.contains(f.getName().substring(0,3))) {
	            			if(nHours == 0 ||
		            				Files.readAttributes(f.toPath(), BasicFileAttributes.class).creationTime().toInstant().compareTo(Instant.now().minusSeconds(nHours * 60 * 60)) > 0)
		            		{
		            				serverData.writeUTF(f.getName());
		            		}
	            		}
	            	}
	            	serverData.writeUTF("EOR");
	            	serverData.flush();
	            	
	            	// Get the list of files they want
	            	while (!(temp = clientData.readUTF()).equals("EOR")) {     
		            	filesRequested.add(temp);    
		            }
	            	
	            	// Send the files
	            	File f;
	            	int i = 0;
	            	DataInputStream fileData;
	            	if(filesRequested.size() > 0)
	            		System.out.println("Sending files to " + clientSocket.toString() + "...");
	            	for(String s : filesRequested) {
	            		System.out.println("\t" + s);
	            		f = new File(torrentDirectoryPath + s);
	            		byte[] b = new byte[(int) f.length()];  
	 		           
	            		fileData = new DataInputStream(new BufferedInputStream(new FileInputStream(f))); 
	            		fileData.readFully(b, 0, b.length);
	    		        
	    		        serverData.writeUTF(f.getName());
	    		        serverData.writeLong(b.length); 
    			        
	    		        serverData.write(b, 0, b.length);     
    			        serverData.flush();
	    			           
    			        //Sending file data to the server  
    			        out.write(b, 0, b.length);  
    			        out.flush();
    			        
    			        i++;
	            	}
	            	serverData.writeUTF("EOR");
	            	serverData.flush();
	            	System.out.println("SUCCESS: " + i + " files sent to " + clientSocket.toString());
	            }
	            
	            out.close();
	            serverData.close();
	            in.close();
	            clientData.close();
            } catch (IOException e) {
            	
            }
        } while(runInfinitely);
    }
    
    public int getNewTorrents() {
    	return newTorrents;
    }
    
    public void resetNewTorrents() {
    	newTorrents = 0;
    }
    
    private void fetchTorrentFileCollection() {
    	File[] listOfFiles = new File(
				properties.getProperty("torrent_directory")).listFiles(
						new FilenameFilter() {
							public boolean accept(File dir, String name) {
								boolean temp = false;
								if (name.toLowerCase().endsWith(".torrent"))
									temp = true;
								return temp;
							}
						});
    	
    	lastFetchedFiles = Instant.now();
    	torrentCollection = new ArrayList<File>(Arrays.asList(listOfFiles));
    }
    
    private void deleteHalfCreatedFiles() {
    	File[] listOfFiles = new File(
				properties.getProperty("torrent_directory")).listFiles(
						new FilenameFilter() {
							public boolean accept(File dir, String name) {
								boolean temp = false;
								if (name.toLowerCase().endsWith(".torrent.part"))
									temp = true;
								return temp;
							}
						});
    	for(File f : listOfFiles) {
    		f.delete();
    	}
    }
}
