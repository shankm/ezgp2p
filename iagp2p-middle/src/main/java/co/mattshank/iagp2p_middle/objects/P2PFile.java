package co.mattshank.iagp2p_middle.objects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;

import co.mattshank.iagp2p_middle.objects.P2PFileConfig.ValidFileType;
import co.mattshank.iagp2p_middle.objects.exceptions.UnsupportedFileTypeException;
import co.mattshank.iagp2p_middle.objects.filedata.P2PFileDataInterface;
import co.mattshank.iagp2p_middle.objects.filedata.P2PFileHeaderInterface;
import co.mattshank.iagp2p_middle.objects.filedata.P2PICLPData;
import co.mattshank.iagp2p_middle.objects.filedata.P2PICLPHeader;
import co.mattshank.iagp2p_middle.objects.filedata.P2PITAGData;
import co.mattshank.iagp2p_middle.objects.filedata.P2PITAGHeader;
import co.mattshank.iagp2p_middle.objects.filedata.P2PITGUData;
import co.mattshank.iagp2p_middle.objects.filedata.P2PITGUHeader;

public class P2PFile {
	Properties properties;
	File file;
	public boolean loadedIntoApp, loadedIntoDB;
	int recordCount;
	private BufferedReader reader;
	ValidFileType fileType;
	P2PFileHeaderInterface header;
	P2PFileDataInterface body;
	P2PFileLogRecord logRecord;
	
	public P2PFile (Properties properties, File file) throws IOException, UnsupportedFileTypeException {
		this.properties = properties;
		this.file = file;
		loadedIntoApp = loadedIntoDB = false;
	}
	
	public void loadFile() {
		System.out.println(file.getPath());
		
		try {
			readHeader();
			readBody();
			loadedIntoApp = true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	public void readHeader() throws IOException, UnsupportedFileTypeException, ParseException {
		System.out.print("Reading header... ");
		System.out.flush();
		
		String headerAsString;
		
		reader = new BufferedReader(new FileReader(file));
		headerAsString = reader.readLine();
		reader.close();
		
		try {
			switch(headerAsString.substring(0, 4)) {
				case "ITAG":	fileType = ValidFileType.ITAG;
								header = new P2PITAGHeader(headerAsString);
								break;
				case "ICLP":	fileType = ValidFileType.ICLP;
								header = new P2PICLPHeader(headerAsString);
								break;
				case "ITGU":	fileType = ValidFileType.ITGU;
								header = new P2PITGUHeader(headerAsString);
								break;
				default:		throw new UnsupportedFileTypeException(file.getName(), headerAsString.substring(0, 4));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		logRecord = new P2PFileLogRecord(getFileName(), fileType, header.getFromAgencyID(), Integer.parseInt(properties.getProperty("home_agency_id")), header.getFileDate());
		System.out.println("SUCCESS");
	}
	//String fileName, ValidFileType fileType, int fromAgencyID, int toAgencyID, Date fileDate
	
	public void readBody() throws IOException, UnsupportedFileTypeException {
		System.out.print("Reading body... ");
		System.out.flush();
		
		switch(logRecord.fileType) {
			case ITAG:	body = new P2PITAGData(file);
						break;
			case ICLP:	body = new P2PICLPData(file);
						break;
			case ITGU:	body = new P2PITGUData(file);
						break;
			default:	break;
		}
		
		try {
			recordCount = logRecord.recordCount = body.readData();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		System.out.println("SUCCESS");
	}
	
	public String getFileName() {return file.getName();}
	public String getFilePath() {return file.getPath();}
	public File getFile() {return file;}
	public void setFile(File file) {this.file = file;}
	public ValidFileType getFileType() {return fileType;}
	public P2PFileLogRecord getFileLogRecord() {return logRecord;}
	public boolean getLoadedIntoApp() {return loadedIntoApp;}
	public boolean getLoadedIntoDB() {return loadedIntoDB;}
	public int getRecordCount() {return recordCount;}
	public P2PFileDataInterface getBody() {return body;}
}
