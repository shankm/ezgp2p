package co.mattshank.iagp2p_middle.objects.filedata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class P2PITGUData extends P2PFileReader implements P2PFileDataInterface {
	File file;
	public ArrayList<Integer> tagAgencyID;
	public ArrayList<Integer> tagNumber;
	public ArrayList<Integer> tagStatusID;
	public ArrayList<Boolean> validForNonTollParking;
	public ArrayList<Boolean> validForNonTollNonParking;
	public ArrayList<Integer> discountPlanID;
	
	public P2PITGUData(File file) {
		this.file = file;
		tagAgencyID = new ArrayList<Integer>();
		tagNumber = new ArrayList<Integer>();
		tagStatusID = new ArrayList<Integer>();
		validForNonTollParking = new ArrayList<Boolean>();
		validForNonTollNonParking = new ArrayList<Boolean>();
		discountPlanID = new ArrayList<Integer>();
	}
	
	/**
	 * Loads the file into the specialized data object
	 * @return the number of records in the file
	 * @throws IOException
	 */
	public int readData() throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String data;
		//String accountInfo;
		int index = 0;
		
		// throw out the header row
		reader.readLine();
		
		// initialize fields in file
		ArrayList<char[]> fields = new ArrayList<char[]>();
		fields.add(new char[3]); // TAG_AGENCY_ID
		fields.add(new char[8]); // TAG_SERIAL_NUMBER
		fields.add(new char[1]); // TAG_STATUS
		fields.add(new char[6]); // TAG_ACCT_INFO
		
		while((data = reader.readLine()) != null) {
			readLine(data, fields);
			
			tagAgencyID.add(Integer.parseInt(String.valueOf(fields.get(0))));
			tagNumber.add(Integer.parseInt(String.valueOf(fields.get(1))));
			tagStatusID.add(Integer.parseInt(String.valueOf(fields.get(2))));
			
			// TODO: Fully implement support for these fields
			//accountInfo = String.valueOf(fields.get(3));
			
			index++;
		}
		
		reader.close();
		
		return index;
	}
}
