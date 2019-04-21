package co.mattshank.iagp2p_middle.objects.filedata;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import co.mattshank.iagp2p_middle.objects.P2PFileConfig.ValidTagStatus;

public class P2PITAGHeader extends P2PFileReader implements P2PFileHeaderInterface {
	String header;
	int fromAgencyID;
	LocalDateTime fileDate;
	int recordCount;
	Map<Integer, Integer> recordCountPerTagStatus;

	static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	
	public P2PITAGHeader(String header) throws IOException, ParseException {
		this.header = header;
		
		recordCountPerTagStatus = new HashMap<Integer, Integer>();
		
		ArrayList<char[]> fields = new ArrayList<char[]>();
		fields.add(new char[3]);
		fields.add(new char[8]);
		fields.add(new char[6]);
		fields.add(new char[8]);
		fields.add(new char[8]);
		fields.add(new char[8]);
		fields.add(new char[8]);
		fields.add(new char[8]);
		
		readLine(this.header, fields, 4);
		
		fromAgencyID = Integer.parseInt(String.valueOf(fields.get(0)));
		fileDate = LocalDateTime.parse(String.valueOf(fields.get(1)) + String.valueOf(fields.get(2)), dateFormatter);
		recordCount = Integer.parseInt(String.valueOf(fields.get(3)));
		
		int i = 4;
		for(ValidTagStatus s : ValidTagStatus.values()) {
			if(s != ValidTagStatus.UNKNOWN) 
				recordCountPerTagStatus.put(s.getID(), Integer.parseInt(String.valueOf(fields.get(i++))));
		}
	}
	
	public int getFromAgencyID() {return fromAgencyID;}
	public int getRecordCount() {return recordCount;}
	public LocalDateTime getFileDate() {return fileDate;}
}
