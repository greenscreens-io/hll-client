/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import io.greenscreens.hllapi.HllAPI;
import io.greenscreens.hllapi.IHllAPI;

/**
 * Asynchronous HLL calls and processing.
 * NOTE: Receiving text buffer MUST be EQ || GT received data. 
 */
public final class HllEngine {

	private final HllAPI api = new HllAPI(true);

	private final ExecutorService executor;
	private final Reader reader;
	
	/**
	 * Main constructor with default receive buffer of 1KB 
	 * @param listener
	 */
	public HllEngine(final MessageListener listener) {
		super();
		this.reader= new Reader(1024, listener);
		this.executor = start();
	}	
	
	/**
	 * Main constructor with adjustable buffer size
	 * @param system
	 * @param ssid
	 * @param partition
	 */
	public HllEngine(final int buffer_size, final MessageListener listener) {
		super();		
		this.reader = new Reader(buffer_size, api, listener);
		this.executor = start();
	}	
	
	/**
	 * Start reader thread 
	 * @return
	 */
	final ExecutorService start() {
		ExecutorService executor = Executors.newFixedThreadPool(2, new ThreadFactory() {			
			@Override
			public Thread newThread(Runnable r) {
				final String name = String.format("HLL ENGINE-%s", reader.hashCode());
				return new Thread(r, name);
			}
		});		
		executor.execute(reader);
		return executor;
	}
	
	public final void registerClose() {
		final HllEngine engine = this;
		Runtime.getRuntime().addShutdownHook(new Thread(() -> engine.release()));
	}
	
	/**
	 * Clear all resources
	 */
	public final void release() {
		
		if (!reader.isStop()) {
			reader.stop();
			api.write(200, 999);
		}
		
		reader.stop();
		executor.shutdown();
	}
	
	public final boolean releaseOnRequest() {
		
		boolean isStop = reader.isStop();
		
		if (isStop) {
			release();
		}
		
		return isStop;
	}
	
	public final boolean isActive() {
		return reader.isStop() && executor.isTerminated();
	}

	/**
	 * Send HLL message to host
	 * @param functionNum
	 * @param data
	 * @param psPosition
	 * @return
	 */
	public long send(final int functionNum, final String data, final int psPosition) {
		
		if (data == null) {
			return api.write(functionNum, psPosition);
		}
		
		return api.write(functionNum, data, psPosition);
	}
	
	/**
	 * Post windows message to browser terminal session
	 * @param lParam
	 * @param wParam
	 * @return
	 */
	public int post(final int wParam, final int lParam) {
		//return api.postHLLMessage(wParam, lParam);
		api.postMessage(wParam, lParam);
		return 0;
	}
	
	/**
	 * Send windows message to browser terminal session
	 * @param lParam
	 * @param wParam
	 * @return
	 */
	public int send(final int lParam, final int wParam) {
		return api.sendHLLMessage(wParam, lParam);
	}

	/**
	 * Get data receive buffer size
	 * @return
	 */
	public int getBufferSize() {
		return reader.getBufferSize();
	}

	/**
	 * Set data receive buffer size
	 * @param buffer_size
	 */
	public void setBufferSize(int buffer_size) {
		reader.setBufferSize(buffer_size);
	}

	/**
	 * Get HLL dll wrapper
	 * @return
	 */
	public IHllAPI getApi() {
		return api;
	}

	public int getNumeric(final String value, final int def) {
		return api.getNumeric(value, def);
	}

}
