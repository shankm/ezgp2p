package co.mattshank.iagp2p_client.objects;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

/*
 * 
 * Torrent building code by Alexandre Jasmin (https://stackoverflow.com/questions/2032876/how-can-i-generate-a-torrent-in-java)
 *
 */
public class P2PTorrent {
	File sharedDataFile, torrentFile;

	Properties properties;
	
	/**
	 * Used in any scenario in which the torrent already exists
	 * @param properties
	 * @param torrentFile
	 * @param isOutgoing
	 */
	public P2PTorrent(Properties properties, File torrentFile, boolean isOutgoing) {
		this.properties = properties;
		this.torrentFile = torrentFile;
		
		String key = isOutgoing ? "home_file_archive_directory" : "torrent_seeding_incoming_directory";
		this.sharedDataFile = new File(properties.getProperty(key) + computeDataFileName(torrentFile.getName()));
	}
	
	/**
	 * Used if we're going to be creating a torrent (outgoing) or don't know the torrent name
	 * @param properties
	 * @param sharedDataFile
	 */
	public P2PTorrent(Properties properties, File sharedDataFile) {
		this.properties = properties;
		this.sharedDataFile = sharedDataFile;
		
		File temp1 = new File(properties.getProperty("home_file_archive_directory") + sharedDataFile.getName());
		File temp2 = new File(properties.getProperty("torrent_seeding_incoming_directory") + sharedDataFile.getName());
		
		if(sharedDataFile.getAbsolutePath().toString().equals(temp1.getAbsolutePath().toString()))
			torrentFile = new File(properties.getProperty("home_file_archive_directory") + buildTorrentName(sharedDataFile.getName()));
		else if (sharedDataFile.getAbsolutePath().toString().equals(temp2.getAbsolutePath().toString()))
			torrentFile = new File(properties.getProperty("torrent_seeding_incoming_directory") + buildTorrentName(sharedDataFile.getName()));
		else
			torrentFile = null;
		
		if(torrentFile == null || !torrentFile.exists())
			torrentFile = null;
	}
	
	public void createTorrent() throws IOException {
        File sharedFile = this.sharedDataFile;
    	File torrentFile = new File(properties.getProperty("torrent_outgoing_directory") + buildTorrentName(sharedFile.getName()));
        String trackerIP = properties.getProperty("tracker_ip");
        int trackerPort = Integer.parseInt(properties.getProperty("tracker_port"));
    	String trackerUrl = "http://" + trackerIP + ":" + trackerPort;
   
    	String announceUrl = trackerUrl + "/" + "announce";
    	
    	final int pieceLength = 512*1024;
        Map<String,Object> info = new HashMap<String,Object>();
        info.put("name", sharedFile.getName());
        info.put("length", sharedFile.length());
        info.put("piece length", pieceLength);
        info.put("pieces", hashPieces(sharedFile, pieceLength));
        Map<String,Object> metainfo = new HashMap<String,Object>();
        metainfo.put("announce", announceUrl);
        metainfo.put("info", info);
        OutputStream out = new FileOutputStream(torrentFile);
        encodeMap(metainfo, out);
        out.close();
        
        this.torrentFile = torrentFile;
    }
   
	public File getSharedDataFile() {
		return sharedDataFile;
	}

	public File getTorrentFile() {
		return torrentFile;
	}
	
	public File getSharedDataFileDirectory() {
		return sharedDataFile.getParentFile();
	}
	
	private static String computeDataFileName(String torrentFileName) {
		String temp = torrentFileName;
		StringBuilder b;
		
		temp = temp.replace(".torrent", "");
		b = new StringBuilder(temp).reverse();
		temp = b.toString().replaceFirst("_", ".");
		b = new StringBuilder(temp).reverse();
		
		return b.toString();
	}
	
	
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private static void encodeObject(Object o, OutputStream out) throws IOException {
        if (o instanceof String)
            encodeString((String)o, out);
        else if (o instanceof Map)
            encodeMap((Map)o, out);
        else if (o instanceof byte[])
            encodeBytes((byte[])o, out);
        else if (o instanceof Number)
            encodeLong(((Number) o).longValue(), out);
        else
            throw new Error("Unencodable type");
    }
    private static void encodeLong(long value, OutputStream out) throws IOException {
        out.write('i');
        out.write(Long.toString(value).getBytes("US-ASCII"));
        out.write('e');
    }
    private static void encodeBytes(byte[] bytes, OutputStream out) throws IOException {
        out.write(Integer.toString(bytes.length).getBytes("US-ASCII"));
        out.write(':');
        out.write(bytes);
    }
    private static void encodeString(String str, OutputStream out) throws IOException {
        encodeBytes(str.getBytes("UTF-8"), out);
    }
    private static void encodeMap(Map<String,Object> map, OutputStream out) throws IOException{
        // Sort the map. A generic encoder should sort by key bytes
        SortedMap<String,Object> sortedMap = new TreeMap<String, Object>(map);
        out.write('d');
        for (Entry<String, Object> e : sortedMap.entrySet()) {
            encodeString(e.getKey(), out);
            encodeObject(e.getValue(), out);
        }
        out.write('e');
    }
    private static byte[] hashPieces(File file, int pieceLength) throws IOException {
        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new Error("SHA1 not supported");
        }
        InputStream in = new FileInputStream(file);
        ByteArrayOutputStream pieces = new ByteArrayOutputStream();
        byte[] bytes = new byte[pieceLength];
        int pieceByteCount  = 0, readCount = in.read(bytes, 0, pieceLength);
        while (readCount != -1) {
            pieceByteCount += readCount;
            sha1.update(bytes, 0, readCount);
            if (pieceByteCount == pieceLength) {
                pieceByteCount = 0;
                pieces.write(sha1.digest());
            }
            readCount = in.read(bytes, 0, pieceLength-pieceByteCount);
        }
        in.close();
        if (pieceByteCount > 0)
            pieces.write(sha1.digest());
        return pieces.toByteArray();
    }
    
    private static String buildTorrentName(String fileName) {
		String torrentName = fileName.replaceFirst(".i", "_i").replaceFirst(".I", "_I") + ".torrent";
		
		return torrentName;
	}
	
}
