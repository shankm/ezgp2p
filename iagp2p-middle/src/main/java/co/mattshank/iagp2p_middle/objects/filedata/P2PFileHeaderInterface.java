package co.mattshank.iagp2p_middle.objects.filedata;

import java.time.LocalDateTime;

public interface P2PFileHeaderInterface {
	public int getFromAgencyID();
	public int getRecordCount();
	public LocalDateTime getFileDate();
}
