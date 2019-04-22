package co.mattshank.iagp2p_tracker;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;

public class P2PTracker extends Thread {
	
	Tracker tracker;
	Properties properties;
	int port;
	boolean run;
	File[] listOfFiles;
	
	public P2PTracker(Properties properties) {
		this.properties = properties;
		port = Integer.parseInt(properties.getProperty("tracker_port"));
		run = true;
	}
	
	public void run() {
		try {
			tracker = new Tracker(new InetSocketAddress(port));
			System.out.println("Announce URL: " + tracker.getAnnounceUrl().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Initialize torrent catalog
		listOfFiles = new File(
				properties.getProperty("torrent_directory")).listFiles(
						new FilenameFilter() {
							public boolean accept(File dir, String name) {
								boolean temp = false;
								if (name.toLowerCase().endsWith(".torrent"))
									temp = true;
								return temp;
							}
						});
		
		for(File f : listOfFiles) {
			try {
				tracker.announce(TrackedTorrent.load(f));
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		do {
//			try {
//				tracker.wait();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while(run);
	}
	
	public void terminate() {
		run = false;
	}
}
