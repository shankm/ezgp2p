package co.mattshank.iagp2p_client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;

import co.mattshank.iagp2p_client.objects.P2PTorrent;

public class P2PTorrentBuilder extends Thread {
	private static final boolean runInfinitely = true;
	private static final boolean moveFiles = true;
	
	static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
	
	Properties properties;
	ArrayList<String> validHomeAgencyIDs;
	HashMap<String, RequestEvent> announcedFiles;
	ArrayList<P2PClient> clients;
	
	public P2PTorrentBuilder(Properties properties) {
		this.properties = properties;
		validHomeAgencyIDs = new ArrayList<String>();
		announcedFiles = new HashMap<String, RequestEvent>();
		clients = new ArrayList<P2PClient>();
		
		@SuppressWarnings("rawtypes")
		Enumeration en = this.properties.propertyNames();
		String key, property;
		
		while(en.hasMoreElements()) {
			key = (String) en.nextElement();
			if(key.equals("home_agency_id") || key.contains("child_agency")) {
				property = properties.getProperty(key).trim();
				if(property != null && !property.equals(""))
					validHomeAgencyIDs.add(String.format("%03d", Integer.parseInt(property)));
			}
		}
	}
	
	public void run() {
		do {
			buildTorrentFiles();
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} while(runInfinitely);
	}
	
	private void buildTorrentFiles() {
		ArrayList<P2PTorrent> torrents;
		HashMap<String, Exception> errors;
		File[] listOfFiles;
		HashMap<String, File[]> errorFiles;
		
		torrents = new ArrayList<P2PTorrent>();
		errors = new HashMap<String, Exception>();
		errorFiles = new HashMap<String, File[]>();
		
		listOfFiles = sortFilesChronologically(new File(
				properties.getProperty("home_file_directory")).listFiles(
						new FilenameFilter() {
							public boolean accept(File dir, String name) {
								boolean temp = false;
								if ((name.toLowerCase().endsWith(".itag") || name.toLowerCase().endsWith(".iclp") || name.toLowerCase().endsWith(".itgu")) &&
										validHomeAgencyIDs.contains(name.toLowerCase().substring(0, 3)))
									temp = true;
								return temp;
							}
						}));
		
		// Get lists of error files
		errorFiles.put("Invalid file type", new File(
					properties.getProperty("home_file_directory")).listFiles(
							new FilenameFilter() {
								public boolean accept(File dir, String name) {
									boolean temp = true;
									if ((name.toLowerCase().endsWith(".itag") || name.toLowerCase().endsWith(".iclp") || name.toLowerCase().endsWith(".itgu")) &&
											validHomeAgencyIDs.contains(name.toLowerCase().substring(0, 3)))
										temp = false;
									return temp;
								}
							}));
		errorFiles.put("Invalid home agency", new File(
				properties.getProperty("home_file_directory")).listFiles(
						new FilenameFilter() {
							public boolean accept(File dir, String name) {
								boolean temp = true;
								if ((name.toLowerCase().endsWith(".itag") || name.toLowerCase().endsWith(".iclp") || name.toLowerCase().endsWith(".itgu")) &&
										validHomeAgencyIDs.contains(name.toLowerCase().substring(0, 3)))
									temp = false;
								return temp;
							}
						}));
		
		String errorMsg;
		// Move invalid files to Error directory
		for(String key : errorFiles.keySet()) {
			for(File f : errorFiles.get(key)) {
				if(!f.isDirectory()) {
					switch (key) {
							case "Invalid file type":		errorMsg = key + ": " + f.getName().substring(f.getName().length() - 4);
															break;
							case "Invalid home agency":		errorMsg = key + ": " + f.getName().substring(0, 3);
															break;
							default:						errorMsg = "";
						}
					
					moveFileToError(f, errorMsg);
				}
			}
		}
		
		// Create torrents for valid files
		File[] temp;
		for(File f : listOfFiles) {
			temp = copyFileToSharedDataFiles(f);
			
			/* if(temp[1] == null)
					System.out.println("Bad copy"); */
			
			torrents.add(new P2PTorrent(properties, temp[0], temp[1], f));
			
			try {
				torrents.get(torrents.size()-1).createTorrent();
			} catch (Exception e) {
				errors.put(f.getPath(), e);
			}
		}
		
		if(torrents.size() > 0) {	
			System.out.println();
			System.out.println(dateFormatter.format(LocalDateTime.now()));
			System.out.println("TORRENT BUILDER SUMMARY");
			System.out.println("-----------------------------------------------------");
			for(P2PTorrent t : torrents) {
				System.out.print("- " + t.getDataFile().getName());
				
				
				// Print results;
				if(t.getTorrentFile() != null) {
					moveFileToArchive(t.getDataFile());
					System.out.println(" -> " + t.getTorrentFile().getName());
				}
				else {
					System.out.println(": ERROR: " + errors.get(t.getDataFile().getPath().getClass().getSimpleName()));
				}
			}
			System.out.println("-----------------------------------------------------");
		}
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
	
	private boolean moveFileToArchive(File file) {
		String destinationPath = properties.getProperty("home_file_archive_directory") + file.getName();
		Path temp;
		
		if(moveFiles) {
			if(destinationPath == null || destinationPath == "")
				return false;
			
			try {
				temp = Files.move(Paths.get(file.getPath()), Paths.get(destinationPath));
			
				if(temp != null)
					return true;
				else
					return false;
			} catch (Exception e) {
				return false;
			}
		}
		
		return false;
	}
	
	private File[] copyFileToSharedDataFiles(File file) {
		String destinationPath = properties.getProperty("torrent_sharing_directory") + file.getName();
		Path temp;
		File[] oldAndNewFile = new File[2];
		
		oldAndNewFile[0] = file;
		
		if(destinationPath == null || destinationPath == "")
			return null;
			
		oldAndNewFile[1] = new File(destinationPath);
		if(oldAndNewFile[1].exists())
			return oldAndNewFile;
		
		try {
			temp = Files.copy(Paths.get(file.getPath()), Paths.get(destinationPath), StandardCopyOption.REPLACE_EXISTING);
		
			if(temp != null) {
				oldAndNewFile[1] = new File(destinationPath);
				return oldAndNewFile;
			}
			else
				return null;
		} catch (Exception e) {
			return null;
		}
	}
	
	private boolean moveFileToError(File file, String error) {
		String destinationPath = properties.getProperty("home_file_error_directory") + file.getName();
		Path temp;
		BufferedWriter writer;
		boolean success = false;
		
		if(moveFiles) {
			try {
				temp = Files.move(Paths.get(file.getPath()), Paths.get(destinationPath));
				if(temp != null)
					success = true;
				else
					success = false;
			} catch (Exception e) {
				success = false;
			}
			
			if(error != null && !error.equals("")) {
				try {
					writer = new BufferedWriter(new FileWriter(destinationPath + ".ERROR"));
					writer.write(file.getName() + "\n");
					writer.write(error);
					writer.close();
				} catch (IOException e) {}
			}
		}
		
		return success;
	}
	
}
