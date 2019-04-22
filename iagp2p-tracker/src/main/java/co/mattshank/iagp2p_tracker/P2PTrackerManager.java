package co.mattshank.iagp2p_tracker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.LogManager;

import co.mattshank.iagp2p_tracker.objects.P2PProperties;

public class P2PTrackerManager {
	private static final boolean runInfinitely = true;
	
	public static void main(String[] args) {
		Properties properties;
		String configPath;
		P2PTracker tracker;
		P2PTorrentDistributor torrentDistributor = new P2PTorrentDistributor();
		boolean disableTrackerLogging = true;
		
		System.out.println("---------------------------------------------------------------");
		System.out.println("\t   iagp2p-tracker (v1.0)");
		System.out.println("---------------------------------------------------------------");
		System.out.println("\t  Developed by Matt Shank");
		System.out.println("\twww.github.com/shankm/iagp2p");
		System.out.println("\t GNU GENERAL PUBLIC LICENSE");
		System.out.println("\t  Version 3, 29 June 2007");
		System.out.println("---------------------------------------------------------------");
		
		configPath = args.length > 0 ? args[0] : "src/main/resources/tracker.properties";
		System.out.println("properties: " + configPath + "\n");
		
		// Check whether to disable logging from the torrent tracker
		if(args.length == 1)
			disableTrackerLogging = false;
		else if (args.length >= 2 && args[1].equals("-n"))
			disableTrackerLogging = true;
		if(disableTrackerLogging)
			LogManager.getLogManager().reset();
		
		// Gather properties to use for duration of execution
		List<String> requiredProperties = new ArrayList<String>();
		// Define list of required properties
		requiredProperties.add("tracker_port");
		
		// Configurable properties from config.properties
		properties = P2PProperties.loadProperties(configPath, requiredProperties);
		
		if(properties == null) {
			System.err.println("All required properties must be defined in client.properties:");
			for (String s : requiredProperties) {
				if (s != null)
					System.err.println("- " + s);
			}
			System.exit(1);
		}
		
		// Initialize new tracker
		System.out.print("Initializing tracker... ");
		System.out.flush();
		tracker = new P2PTracker(properties);
		tracker.start();
		System.out.println("SUCCESS");
		
		// Initialize new torrent distributor to receive new torrents and deliver them to the swarm
		System.out.print("Initializing torrent distributor... ");
		System.out.flush();
		try {
			torrentDistributor = new P2PTorrentDistributor(properties);
			torrentDistributor.start();
		} catch (IOException e1) {
			System.out.println("FAIL");
			e1.printStackTrace();
			System.exit(1);
		}
		System.out.println("SUCCESS");
		
		// Check whether any new torrents have come in
		do {
			if(torrentDistributor.getNewTorrents() > 0) {
				System.out.print("New torrent file(s) received. Terminating tracker... ");
				System.out.flush();
				tracker.terminate();
				while(tracker.getState() != Thread.State.TERMINATED) {};
				System.out.println("SUCCESS");
				
				torrentDistributor.resetNewTorrents();
				
				System.out.print("Initializing new tracker... ");
				System.out.flush();
				tracker = new P2PTracker(properties);
				tracker.start();
				System.out.println("SUCCESS");
			}
			
			if(tracker.getState() == Thread.State.TERMINATED) {
				System.out.println("Tracker terminated unexpectedly.");
				System.out.print("Re-initializing tracker...");
				System.out.flush();
				tracker = new P2PTracker(properties);
				tracker.start();
				System.out.println("SUCCESS");
			}
			
			if(torrentDistributor.getState() == Thread.State.TERMINATED) {
				System.out.println("Torrent distributor terminated unexpectedly.");
				System.out.print("Re-initializing torrent distributor...");
				System.out.flush();
				try {
					torrentDistributor = new P2PTorrentDistributor(properties);
					torrentDistributor.start();
				} catch (IOException e1) {
					System.out.println("FAIL");
					e1.printStackTrace();
					System.exit(1);
				}
				System.out.println("SUCCESS");
			}
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while(runInfinitely);
	}

}
