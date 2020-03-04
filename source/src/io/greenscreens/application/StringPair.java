/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.application;

import javafx.beans.NamedArg;

public class StringPair {
	
	private final int key;
	private final String value;
	
	public StringPair(@NamedArg("key") int key, @NamedArg("value") String value) {
		super();
		this.key = key;
		this.value = value;
	}
	
	public int getKey() {
		return key;
	}
	
	public String getValue() {
		return value;
	}
	
    @Override
    public String toString() {
        return String.format("(%d) %s", key, value);
    }
	    	
}
