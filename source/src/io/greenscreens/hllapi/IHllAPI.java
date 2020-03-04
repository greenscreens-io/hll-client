package io.greenscreens.hllapi;

public interface IHllAPI {

	/**
	 * Get received string data as bytes
	 * @return
	 */
	byte[] getBytes();

	/**
	 * Get received string data as bytes with specific length
	 * @param ln
	 * @return
	 */
	byte[] getBytes(int ln);

	/**
	 * Read received string data from hll message 
	 * @return
	 */
	String getData();

	/**
	 * Read received string data from hll message as hex string
	 * @param asHex
	 * @return
	 */
	String getData(final boolean asHex);
	
	/**
	 * Get last function used.
	 * Might not be correct for async calls
	 * 
	 * @return
	 */
	int getFn();

	/**
	 * Get last PS used.
	 * Might not be correct for async calls
	 * 
	 * @return
	 */
	int getPS();

	/**
	 * Get last data length used.
	 * Might not be correct for async calls
	 * 
	 * @return
	 */
	int getLength();

}