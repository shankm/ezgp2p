package co.mattshank.iagp2p_middle.objects;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class P2PProperties {
	public static Properties loadProperties(String propertiesPath, List<String> requiredProperties) {	
		Properties prop = new Properties();
		InputStream input = null;
		
		try {
			input = new FileInputStream(propertiesPath);
	
			// load the properties file
			prop.load(input);
	
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		// Verify that all required properties have been provided
		if(!verifyProperties(prop, requiredProperties))
			return null;
		
		// fix malformed properties based on expectations
		prop = cleanUpProperties(prop);
		
		return prop;
	}
	
	private static boolean verifyProperties(Properties prop, List<String> required) {
		
		for (String p : required) {
			if (prop.getProperty(p) == null || prop.getProperty(p).trim().equals(""))
				return false;
		}
		
		return true;
	}
	
	private static Properties cleanUpProperties(Properties properties) {
		@SuppressWarnings("rawtypes")
		Enumeration en;
		Properties prop = properties;
		String key, property;
		
		en = prop.propertyNames();
		while(en.hasMoreElements()) {
			key = (String) en.nextElement();
			
			property = prop.getProperty(key);
			property.replace("\\", "/");
			
			if(key.contains("directory")) {
				if(!property.substring(property.length()-1).equals("/"))
					property += "/";
			}
			
			prop.setProperty(key, property);
		}
		
		return prop;
	}
}