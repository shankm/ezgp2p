package co.mattshank.iagp2p_middle.objects.filedata;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

public class P2PFileReader {
	
	public void readLine(String data, ArrayList<char[]> fields, int ignoreFirstCharacters) throws IOException {
		Reader reader = new StringReader(data);
		char[] ignore = new char[ignoreFirstCharacters];
		reader.read(ignore);
		for(char[] c : fields) {
			reader.read(c);
		}
	}
	
	public void readLine(String data, ArrayList<char[]> fields) throws IOException {
		Reader reader = new StringReader(data);
		for(char[] c : fields) {
			reader.read(c);
		}
	}
}
