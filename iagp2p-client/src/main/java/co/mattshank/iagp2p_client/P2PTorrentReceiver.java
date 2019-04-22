package co.mattshank.iagp2p_client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

public class P2PTorrentReceiver extends Thread {
	private static final boolean runInfinitely = true;
	
	ArrayList<String> torrentFilesInPossession;
	ArrayList<String> homeAgencyFileMarkers;
	String incomingTorrentDirectoryPath;
	String serverIP;
	int serverPort;
	int secondsBetweenInquiries;
	Properties properties;
	
	public P2PTorrentReceiver(Properties properties) {
		this.properties = properties;
		this.serverIP = properties.getProperty("torrent_distributor_ip");
		this.serverPort = Integer.parseInt(properties.getProperty("torrent_distributor_port"));
		incomingTorrentDirectoryPath = properties.getProperty("torrent_incoming_directory");
		
		secondsBetweenInquiries = Integer.parseInt(properties.getProperty("torrent_receiver_sec_between_inquiries", Integer.toString(15 /*minutes*/ * 60 /*seconds*/)));
		
		fetchHomeAgencyFileIDs();
		fetchFilesInPossession();
	}

	public void run() {
		do {
			for(int i = 0; i < 12; i++) {
				try {
					FileOutputStream fileOutput = null;
					
					SocketAddress sockaddr = new InetSocketAddress(serverIP, serverPort);
					Socket sock = new Socket();
					
					sock.connect(sockaddr, 5000);
			           
			        OutputStream out = sock.getOutputStream();  
			        DataOutputStream clientData = new DataOutputStream(out);
			        InputStream in = sock.getInputStream();  
			        DataInputStream serverData = new DataInputStream(sock.getInputStream());
			        
			        ArrayList<String> filesAtTracker = new ArrayList<String>();
			        ArrayList<String> filesToRequest = new ArrayList<String>();
			        
			        // Send the request GET
			        clientData.writeUTF("GET");
			        
			        // If we only want any file added to the tracker in the last N minutes, send that number N
			        //		0 means we get everything
			        clientData.writeInt(0);
			        
			        // Send the home and children agencies
			        //	We don't need these files, because we already have them
			        for(String s : homeAgencyFileMarkers) {
			        	clientData.writeUTF(s);
			        }
			        clientData.writeUTF("EOR");
			        clientData.flush();
			        
			        // Get the list of files available
			        String temp;
	            	while (!(temp = serverData.readUTF()).equals("EOR")) {     
	            		filesAtTracker.add(temp);    
		            }
	            	
	            	// Check which files we need to download
	            	for(String s : filesAtTracker) {
	            		if(!torrentFilesInPossession.contains(s))
	            			filesToRequest.add(s);
	            	}
	
	            	// Send the files to request
	            	for(String s : filesToRequest) {
	            		clientData.writeUTF(s);
	            	}
	            	clientData.writeUTF("EOR");
	            	clientData.flush();
	            	
	            	// Download the torrent files
		            while(!(temp = serverData.readUTF()).equals("EOR")) {
	            		long size;
	            		byte[] buffer = new byte[1024];
	            		int bytesRead;
	            		
		            	File newFileTemp = new File(incomingTorrentDirectoryPath + temp + ".part");
		            	File newFileFinal = new File(incomingTorrentDirectoryPath + temp);
		            	
		            	// Read size
		            	size = serverData.readLong();
		            	
		            	fileOutput = new FileOutputStream(newFileTemp);   
			            
			            while (size > 0 && (bytesRead = serverData.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {     
			            	fileOutput.write(buffer, 0, bytesRead);     
			                size -= bytesRead;     
			            }
			            
		            	fileOutput.close();
		            	newFileTemp.renameTo(newFileFinal);
		            }
			        
			        //Close the socket
			        in.close();
			        serverData.close();
			        out.close();
			        clientData.close();  
			        sock.close();
			       
				} catch (Exception e) {
					System.out.println("TorrentReceiver: Failed to establish a connection to the tracker. (#" + (i+1) + ")");
				}
			
				try {
					Thread.sleep(10 /*seconds*/ * 1000 /*milliseconds*/);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			try {
				Thread.sleep(secondsBetweenInquiries /*seconds*/ * 1000 /*milliseconds*/);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while(runInfinitely);
	}
	
	public void send() {
		run();
	}
	
	@SuppressWarnings("rawtypes")
	private void fetchHomeAgencyFileIDs() {
		ArrayList<String> homeAgencyFileMarkers = new ArrayList<String>();
		Enumeration en = properties.propertyNames();
		String key, property;
		
		while(en.hasMoreElements()) {
			key = (String) en.nextElement();
			if(key.equals("home_agency_id") || key.contains("child_agency")) {
				property = properties.getProperty(key).trim();
				if(property != null && !property.equals(""))
					homeAgencyFileMarkers.add(String.format("%03d", Integer.parseInt(property)));
			}
		}
		
		this.homeAgencyFileMarkers = homeAgencyFileMarkers;
	}
	
	private void fetchFilesInPossession() {
		File[] listOfFiles;
		ArrayList<File> awayAgencyFiles;
		ArrayList<String> torrentFilesInPossession = new ArrayList<String>();
		
		// Downloaded away files
		listOfFiles = new File(
				properties.getProperty("torrent_incoming_directory")).listFiles(
						new FilenameFilter() {
							public boolean accept(File dir, String name) {
								boolean temp = false;
								if (name.toLowerCase().endsWith(".torrent"))
									temp = true;
								return temp;
							}
						});
		awayAgencyFiles = new ArrayList<File>(Arrays.asList(listOfFiles));
		
		for(File f : awayAgencyFiles) torrentFilesInPossession.add(f.getName());
		
		this.torrentFilesInPossession = torrentFilesInPossession;
	}
}
