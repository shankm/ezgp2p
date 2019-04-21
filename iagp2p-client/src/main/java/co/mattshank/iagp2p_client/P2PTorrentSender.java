package co.mattshank.iagp2p_client;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Properties;

import co.mattshank.iagp2p_client.objects.P2PTorrent;

public class P2PTorrentSender extends Thread {
	File torrentFile;
	String serverIP;
	int serverPort;
	
	public P2PTorrentSender(Properties properties, P2PTorrent torrent) {
		this.torrentFile = torrent.getTorrentFile();
		this.serverIP = properties.getProperty("torrent_distributor_ip");
		this.serverPort = Integer.parseInt(properties.getProperty("torrent_distributor_port"));
	}
	
	public void run() {
		for(int i = 0; i < 10; i++) {
			try {
				SocketAddress sockaddr = new InetSocketAddress(serverIP, serverPort);
				Socket sock = new Socket();
				
				sock.connect(sockaddr, 5000);
				
				String response;
				
		        byte[] b = new byte[(int) torrentFile.length()];
		           
		        OutputStream out = sock.getOutputStream();  
		        DataOutputStream clientData = new DataOutputStream(out);
		        InputStream in = sock.getInputStream();  
		        DataInputStream serverData = new DataInputStream(sock.getInputStream());
		        
		        // Send the request PUT
		        clientData.writeUTF("PUT");
		        
		        //Sending file name and file size to the server      
		        clientData.writeUTF(torrentFile.getName());
		        clientData.writeLong(b.length); 
		        response = serverData.readUTF();
		        
		        // If the server doesn't already have the file
		        if(response.equals("OK")) {
		        	DataInputStream fileInput = new DataInputStream(new BufferedInputStream(new FileInputStream(torrentFile)));     
			        fileInput.readFully(b, 0, b.length);
		        	
			        clientData.write(b, 0, b.length);     
			        clientData.flush();
			           
			        //Sending file data to the server  
			        out.write(b, 0, b.length);  
			        out.flush();  
			        
			        fileInput.close();
		        }
		        
		        //Close the socket
		        in.close();
		        serverData.close();
		        out.close();
		        clientData.close();  
		        sock.close();
		       
			} catch (Exception e) {
				System.out.println("TorrentSender: Failed to establish a connection to the tracker. (" + torrentFile.getName() + " [#" + (i+1) + "])");
			}
		
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void send() {
		run();
	}
}
