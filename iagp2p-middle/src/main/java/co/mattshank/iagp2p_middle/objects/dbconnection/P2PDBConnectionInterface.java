package co.mattshank.iagp2p_middle.objects.dbconnection;

import co.mattshank.iagp2p_middle.objects.P2PFile;
import co.mattshank.iagp2p_middle.objects.P2PFileLogRecord;

public interface P2PDBConnectionInterface {
	void open() throws Exception;
	int createUpdateFileRecord(P2PFileLogRecord loggedFile) throws Exception;
	boolean fileAlreadyExists(P2PFileLogRecord loggedFile) throws Exception;
	void loadFileIntoDatabase(P2PFile file) throws Exception;
	void loadITAG(P2PFile file) throws Exception;
	void loadICLP(P2PFile file) throws Exception;
	void loadITGU(P2PFile file) throws Exception;
	void close() throws Exception;
}
