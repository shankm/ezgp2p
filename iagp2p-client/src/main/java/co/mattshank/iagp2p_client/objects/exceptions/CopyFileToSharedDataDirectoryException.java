package co.mattshank.iagp2p_client.objects.exceptions;

public class CopyFileToSharedDataDirectoryException extends Exception {
	public CopyFileToSharedDataDirectoryException() {
		super("Failed to copy file to shared data directory");
	}
	
	private static final long serialVersionUID = 1203517436908414290L;
}
