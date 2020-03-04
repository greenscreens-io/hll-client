/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.async;

import io.greenscreens.hllapi.HllAPI;

/**
 * Async HLL reader with adjustable receive buffer
 */
public class Reader implements Runnable {

	private volatile int buffer_size = 1024;

	private boolean isStop = false;

	private final HllAPI api;
	private final MessageListener listener;

	/**
	 * Main constructor
	 * 
	 * @param buffer_size
	 * @param api
	 * @param listener
	 */
	public Reader(final int buffer_size, final MessageListener listener) {
		super();
		this.buffer_size = buffer_size;
		this.api = new HllAPI(true);
		this.listener = listener;
	}
	
	/**
	 * Main constructor
	 * 
	 * @param buffer_size
	 * @param api
	 * @param listener
	 */
	public Reader(final int buffer_size, final HllAPI api, final MessageListener listener) {
		super();
		this.buffer_size = buffer_size;
		this.api = api;
		this.listener = listener;
	}

	/**
	 * Signal reader stop
	 */
	public void stop() {
		isStop = true;
	}
	
	public boolean isStop() {
		return isStop;
	}

	/**
	 * Message loop
	 */
	@Override
	public void run() {

		long retVal = 0;
		
		while (!isStop) {			
			
			retVal = api.read(0, null, buffer_size, 0);
			
			final int fn = api.getFn();
			final int ps = api.getPS();
			
			if (fn == 0) {
				Thread.yield();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {					
					e.printStackTrace();
					isStop = true;
				}
				continue;
			}
			
			if (fn == 200 && ps==999) {
				isStop = true;
			} 
			
			try {
				listener.onData(api, retVal);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	public int getBufferSize() {
		return buffer_size;
	}

	public void setBufferSize(int buffer_size) {
		this.buffer_size = buffer_size;
	}

	/**
	 * Get last function used.
	 * Might not be correct for async calls
	 * 
	 * @return
	 */
	public int getFn() {
		return api.getFn();
	}

	/**
	 * Get last PS used.
	 * Might not be correct for async calls
	 * 
	 * @return
	 */
	public int getPS() {
		return api.getPS();
	}
	
	/**
	 * Get last data length used.
	 * Might not be correct for async calls
	 * 
	 * @return
	 */
	public int getLength() {
		return api.getLength();
	}

}
