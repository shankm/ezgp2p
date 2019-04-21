package co.mattshank.iagp2p_tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import co.mattshank.iagp2p_tracker.objects.P2PProperties;

public class P2PTrackerManager {
	private static final boolean runInfinitely = true;
	
	public static void main(String[] args) {
		Properties properties;
		String configPath;
		P2PTracker tracker;
		P2PTorrentDistributor torrentAcceptor;
		
		configPath = args.length > 0 ? args[0] : "src/main/resources/tracker.properties";
		
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
		System.out.print("Initializing torrent acceptor... ");
		System.out.flush();
		torrentAcceptor = new P2PTorrentDistributor(properties);
		torrentAcceptor.start();
		System.out.println("SUCCESS");
		
		// Check whether any new torrents have come in
		do {
			if(torrentAcceptor.getNewTorrents() > 0) {
				System.out.print("New torrent file(s) received. Terminating tracker... ");
				System.out.flush();
				tracker.terminate();
				while(tracker.getState() != Thread.State.TERMINATED) {};
				System.out.println("SUCCESS");
				
				torrentAcceptor.resetNewTorrents();
				
				System.out.print("Initializing new tracker... ");
				System.out.flush();
				tracker = new P2PTracker(properties);
				tracker.start();
				System.out.println("SUCCESS");
			}
			
			if(tracker.getState() == Thread.State.TERMINATED)
				tracker = new P2PTracker(properties);
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while(runInfinitely);
	}

}
