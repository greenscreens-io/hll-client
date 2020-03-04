/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.hllapi;

import java.nio.charset.Charset;

//This is a class that facilitates passing a String by reference to
//C library routines so that the string  may be modified by the C
//routine and the modifications is reflected on JAVA side as well
//Save this in a file StringByReference.java in current directory

import com.sun.jna.ptr.ByReference;

/**
 * Helper class to send string into dll as reference
 *
 */
class StringByReference extends ByReference {

	private int size;

	public static StringByReference get(String str, Charset encoding) {
		byte[] bb = str.getBytes(encoding);		
		StringByReference ref = new StringByReference(bb.length);
		ref.setValue(bb);
		return ref;
	}
	
	public StringByReference() {
		this(0);
	}

	public StringByReference(int size) {
		super(getSize(size));
		this.size = getSize(size);
		//super(size);
		//this.size = size;		
		getPointer().clear(this.size);
	}

	public StringByReference(String str, Charset encoding) {
		super(getSize(str, encoding));
		this.size = getSize(str, encoding);
		setValue(str, encoding);		
	}
	
	public StringByReference(String str) {
		super(getSize(str));
		this.size = getSize(str);
		setValue(str);
	}

	private static int getSize(int size) {
		//return size < 4 ? 4 : size + 1;
		return size == 0 ? 4 : size + 1;
	}
	
	private static int getSize(String str) {
		return getSize(str, null);
	}
	
	private static int getSize(String str, Charset encoding) {
		int size = getSize(str.length());
		if (encoding != null) {
			if (encoding.name().contains("UTF")) {
				size = size * 2;
			}	
		}
		return size;
	}
	
	public void setValue(byte[] bb) {
		int i  = 0;
		for (byte b : bb) {
			getPointer().setByte(i, b);
			i++;
		}
	}
	
	public void setValue(String str, Charset encoding) {
		byte[] bb = str.getBytes(encoding);
		setValue(bb);
	}
	
	public void setValue(String str, String encoding) {		
		getPointer().setString(0, str, encoding);
	}

	public void setValue(String str) {
		getPointer().setWideString(0, str);
	}

	public String getDefaultValue() {
		return getPointer().getString(0);
	}

	public String getValue(int length, Charset encoding) {
		byte[] buf = getBytes(length);
		return new String(buf, encoding);
	}
	
	public String getValue(int length, String encoding) {
		byte[] buf = getBytes(length);
		return new String(buf, Charset.forName(encoding));
	}

	public String getValueW(int length) {
		byte[] buf = getBytes(length);
		return new String(buf);
	}

	public byte[] getBytes(int length) {
		int l = length;
		if (length > this.size) {
			l = this.size;
		}
		byte[] buf = new byte[l];
		getPointer().read(0, buf, 0, l);
		return buf;
	}

}