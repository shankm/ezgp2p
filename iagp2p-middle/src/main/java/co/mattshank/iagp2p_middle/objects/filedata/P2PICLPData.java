package co.mattshank.iagp2p_middle.objects.filedata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class P2PICLPData extends P2PFileReader implements P2PFileDataInterface {
	File file;
	public ArrayList<String> licensePlateState;
	public ArrayList<String> licensePlate;
	public ArrayList<String> licensePlateType;
	public ArrayList<Integer> tagAgencyID;
	public ArrayList<Integer> tagNumber;
	
	public P2PICLPData(File file) {
		this.file = file;
		licensePlateState = new ArrayList<String>();
		licensePlate = new ArrayList<String>();
		licensePlateType = new ArrayList<String>();
		tagAgencyID = new ArrayList<Integer>();
		tagNumber = new ArrayList<Integer>();
	}
	
	public int readData() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String data;
		//String accountInfo;
		int index = 0;
		
		// throw out the header row
		reader.readLine();
		
		// initialize fields in file
		ArrayList<char[]> fields = new ArrayList<char[]>();
		fields.add(new char[2]);  // LIC_STATE
		fields.add(new char[10]); // LIC_NUMBER
		fields.add(new char[2]);  // LIC_TYPE
		fields.add(new char[3]);  // TAG_AGENCY_ID
		fields.add(new char[8]);  // TAG_SERIAL_NUMBER
		
		while((data = reader.readLine()) != null) {
			readLine(data, fields);
			
			licensePlateState.add(String.valueOf(fields.get(0)));
			licensePlate.add(String.valueOf(fields.get(1)));
			licensePlateType.add(String.valueOf(fields.get(2)));
			tagAgencyID.add(Integer.parseInt(String.valueOf(fields.get(3))));
			tagNumber.add(Integer.parseInt(String.valueOf(fields.get(4))));
			
			// TODO: Fully implement support for these fields
			//accountInfo = String.valueOf(fields.get(3));
			
			index++;
		}
		
		reader.close();
		
		return index;
	}
}
