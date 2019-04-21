package co.mattshank.iagp2p_middle.objects;

import java.time.LocalDateTime;

import co.mattshank.iagp2p_middle.objects.P2PFileConfig.ValidFileType;

public class P2PFileLogRecord {
	int fileID, fromAgencyID, toAgencyID, recordCount;
	public ValidFileType fileType;
	String fileName, comments;
	String fileStatus;
	LocalDateTime fileDate;
	
	public P2PFileLogRecord(String fileName, ValidFileType fileType, int fromAgencyID, int toAgencyID, LocalDateTime fileDate) {
		fileID = -1;
		recordCount = -1;
		comments = "";
		this.fileName = fileName;
		this.fileType = fileType;
		this.fromAgencyID = fromAgencyID;
		this.toAgencyID = toAgencyID;
		this.fileDate = fileDate;
	}
	
	public int getFileID() {return fileID;}
	public void setFileID(int id) {fileID = id;}
	public int getFileTypeID() {return fileType.getID();}
	public String getFileTypeName() {return fileType.toString();}
	public String getFileStatus() {return fileStatus;}
	public void setFileStatus(String status) {fileStatus = comments = status;}
	public String getFileName() {return fileName;}
	public LocalDateTime getFileDate() {return fileDate;}
	public int getFromAgencyID() {return fromAgencyID;}
	public int getToAgencyID() {return toAgencyID;}
	public String getComments() {return comments;}
	public int getRecordCount() {return recordCount;}
}
