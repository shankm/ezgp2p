package co.mattshank.iagp2p_middle.objects.filedata;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class P2PICLPHeader extends P2PFileReader implements P2PFileHeaderInterface {
	String header;
	int fromAgencyID;
	LocalDateTime fileDate;
	int recordCount;
	
	static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	
	public P2PICLPHeader(String header) throws IOException, ParseException {
		this.header = header;
		
		ArrayList<char[]> fields = new ArrayList<char[]>();
		fields.add(new char[3]);
		fields.add(new char[8]);
		fields.add(new char[6]);
		fields.add(new char[8]);
		
		readLine(this.header, fields, 4);
		
		fromAgencyID = Integer.parseInt(String.valueOf(fields.get(0)));
		fileDate = LocalDateTime.parse(String.valueOf(fields.get(1)) + String.valueOf(fields.get(2)), dateFormatter);
		recordCount = Integer.parseInt(String.valueOf(fields.get(3)));
	}
	
	public int getFromAgencyID() {return fromAgencyID;}
	public int getRecordCount() {return recordCount;}
	public LocalDateTime getFileDate() {return fileDate;}
}