package co.mattshank.iagp2p_client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.protocol.TrackerMessage.AnnounceRequestMessage.RequestEvent;

import co.mattshank.iagp2p_client.objects.P2PTorrent;


public class P2PClient extends Thread {
	InetAddress listenInterface;
	P2PTorrent torrent;
	SharedTorrent sharedTorrent;
	Client client;
	
	public P2PClient (Properties properties, P2PTorrent torrent) throws UnknownHostException {
		this.torrent = torrent;
		
		if (properties.getProperty("listen_interface") == "localhost")
			listenInterface = InetAddress.getLocalHost();
		else
			/* TODO: Rework this */
			listenInterface = InetAddress.getLocalHost();
		
		try {
			sharedTorrent = SharedTorrent.fromFile(torrent.getTorrentFile(), torrent.getSharedDataFileDirectory());
			/*	for(java.util.List<URI> l : sharedTorrent.getAnnounceList())
					for(java.net.URI i : l)
						System.out.println(i.toString());
				System.exit(1); */
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			client = new Client(this.listenInterface, sharedTorrent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void shareFile() {
		client.share();
	}
	
	public void announceFile(RequestEvent event) {
		
	}
	
	public String getStatus() {
		return client.getState().toString();
	}
	
	public P2PTorrent getTorrent() {return torrent;}
}