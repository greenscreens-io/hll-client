/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.application;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.greenscreens.hllapi.IHllAPI;

/**
 * Simple util class for string handling
 */
public enum Util {
	;

	final protected static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
	final protected static DateFormat formatter = new SimpleDateFormat("HH:mm:ss:SSS");	
	
	public static String getLog(boolean isSend, boolean isHex, long res, final IHllAPI hll) {		
		return getLog(isSend, hll.getFn(), hll.getLength(), hll.getPS(), res, hll.getData(isHex));
	}
	
	public static String getLog(boolean isSend, int fn, int len, int ps, long res, String data) {

		String output = null;
    	Date date = new Date(System.currentTimeMillis());
    	String dateFormatted = formatter.format(date);
    	
    	if (isSend) {
    		output = String.format("[%s] SND >> RET: NA, FN: %d, LN: %d, PS: %d, DATA: %s \n", dateFormatted, fn, len, ps, data);	
    	} else {
    		output = String.format("[%s] RCV >> RET: %d,  FN: %d, LN: %d, PS: %d, DATA: %s \n", dateFormatted, res, fn, len, ps, data);
    	}
    	
    	return output;
	}

	/**
	 * Converts byte array to hex string
	 * 
	 * @param bytes
	 * @return
	 */
	public static String bytesToHex(final byte[] bytes) {
		
		final char[] hexChars = new char[bytes.length * 2];
		int v = 0;
		
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		
		return new String(hexChars);
	}

	public static String charsToHex(final char[] bytes) {
		
		final char[] hexChars = new char[bytes.length * 2];
		int v = 0;
		
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j];
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		
		return new String(hexChars);
	}
	
	/**
	 * Converts hex string into byte array
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] hexStringToByteArray(final String s) {
		
		final int len = s.length();
		final byte[] data = new byte[len / 2];
		
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}


}
