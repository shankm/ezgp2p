package co.mattshank.iagp2p_middle.objects;

public class P2PFileConfig {

	public static enum ValidFileType {
		ITAG(1), ICLP(2), ITGU(3), UNKNOWN(-1);
			
		private final int id;
	    private ValidFileType(int id) {
	        this.id = id;
	    }

	    public int getID() {
	        return id;
	    }
	}
	
	public static enum ValidTagStatus {
		ACTIVE(1), LOW_BALANCE(2), INACTIVE(3), LOST_STOLEN(4), UNKNOWN(-1);
		
		private final int id;
	    private ValidTagStatus(int id) {
	        this.id = id;
	    }

	    public int getID() {
	        return id;
	    }
	}
	
}
