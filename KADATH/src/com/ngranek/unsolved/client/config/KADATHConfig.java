package com.ngranek.unsolved.client.config;

import java.io.FileInputStream;
import java.util.Properties;


/**
 * @author Ing. William Anez (cucho)
 *
 */
public class KADATHConfig {

	private static final String KADATH_CONFIG_FILE = "conf/kadath.properties";
	private static Properties properties = null;
	
	public KADATHConfig() {
		
	}

	private static boolean initPropertyFile() {
		
		try {
			if (properties == null) {
				properties = new Properties();
				FileInputStream file = new FileInputStream( KADATH_CONFIG_FILE );
				properties.load(file);
			}
			
			return true;
			
		} catch (Exception e) {
			
			e.printStackTrace();
			return false;
		}
		
	}
	
	public static String getProperty(String propName) {

		if (!initPropertyFile()) {
			return null;
		}
		
		return properties.getProperty(propName);
		
	}

	public static long getLongProperty(String propName) {
		
		if (!initPropertyFile()) {
			return -1;
		}

		try {
			return Long.parseLong( properties.getProperty(propName) );
		} catch (NumberFormatException e) {
			return 0l;
		}

	}

	public static int getIntProperty(String propName) {

		if (!initPropertyFile()) {
			return -1;
		}
		
		try {
			return Integer.parseInt( properties.getProperty(propName) );
		} catch (NumberFormatException e) {
			return 0;
		}
		
	}

	public static boolean getBooleanProperty(String propName) {

		if (!initPropertyFile()) {
			return false;
		}
		
		try {
			return Boolean.parseBoolean( properties.getProperty(propName) );
		} catch (NumberFormatException e) {
			return false;
		}

	}
	
	
}
