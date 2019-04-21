package co.mattshank.iagp2p_middle.objects.exceptions;

public class UnsupportedDatabaseServerException extends Exception {
	public UnsupportedDatabaseServerException(String serverType) {
		super("The specified database server type is unsupported: " + serverType);
	}
	
	private static final long serialVersionUID = 1203517436908414290L;

}
