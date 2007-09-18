package com.ngranek.unsolved.client.test;

import com.ngranek.unsolved.client.config.KADATHConfig;


/**
 * @author Ing. William Anez (cucho)
 *
 */
public class TestKadathConfig {

	public static void main(String[] args) {
		
		System.out.println("Base Dir = " + KADATHConfig.getProperty("com.ngranek.unsolved.base.dir"));
		
	}
	
}
