package co.mattshank.iagp2p_middle.objects.exceptions;

public class UnsupportedFileTypeException extends Exception {
	public UnsupportedFileTypeException(String fileName, String fileType) {
		super("The file type specified in the header of file (" + fileName + ") is unsupported: " + fileType);
	}
	
	private static final long serialVersionUID = 1203517436908414290L;
}
