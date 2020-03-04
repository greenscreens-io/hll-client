/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.hllapi;

import java.nio.charset.Charset;
import java.util.stream.IntStream;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.User32Util;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.ByReference;
import com.sun.jna.ptr.IntByReference;

import io.greenscreens.application.Util;

/**
 * Link to Green Screens hll.dll native lib through JNA
 * 
 * NOTE: For async calls use 2 instances. One for read, one for write operations.
 * Reason: Instance holds send data including buffer size
 */
public class HllAPI implements IHllAPI {

	// functions with characters only
	private final static int[] fnChars = {200, 53, 25, 110, 3, 20, 24, 31, 32, 15, 33, 8, 5, 34, 1};
	
	private final static int PTR_SIZE = 1024*32;
		
	static HLLLibrary INSTANCE = null;
	static final Charset CODEPAGE = Charset.forName("Cp1250");
	static final Charset CODEUTF8 = Charset.forName("UTF8");
	
	static final String HLLPATH = "hll";
	
	static final int msgid = User32Util.registerWindowMessage("WM_GREENSCREENS_HLL_CLI");		
	static final HWND hwnd = User32.HWND_BROADCAST;
	
	static {
		// initialize hll.dll - load into JNA instance
		INSTANCE = (HLLLibrary)  Native.loadLibrary(HLLPATH, HLLLibrary.class);
		
	}
	
	/**
	 * JNA mapper for hll.dll
	 *
	 */
    public interface HLLLibrary extends Library {
    	   	  
    	NativeLong  hllapi(ByReference functionNum, Pointer dataString, ByReference length, ByReference psPosition);
    	NativeLong  hllapi_read(ByReference functionNum, Pointer dataString, ByReference length, ByReference psPosition);    	
    	NativeLong  hllapi_write(ByReference functionNum, Pointer dataString, ByReference length, ByReference psPosition);

    	WinDef.UINT SendHLLMessage(WinDef.WPARAM wParam, WinDef.LPARAM lParam);
    	WinDef.UINT PostHLLMessage(WinDef.WPARAM wParam, WinDef.LPARAM lParam);
    }
    
    StringByReference ref = new StringByReference(HllAPI.PTR_SIZE);
    
	final IntByReference fn;
	final IntByReference len;
	final IntByReference pos;
    
	final boolean async;
	
	/**
	 * Default synchronous constructor
	 */
	public HllAPI() {
		this.async = false;
		fn = new IntByReference();
    	len = new IntByReference();
    	pos = new IntByReference();
	}
	
	/**
	 * Aynchronous constructor
	 * All helper calls will use write instead of call
	 * 
	 * @param async
	 */
    public HllAPI(final boolean async) {
		super();
		this.async = async;
    	fn = new IntByReference();
    	len = new IntByReference();
    	pos = new IntByReference();
	}
        
    /**
     * Get received string data as bytes
     * @return
     */
    @Override
	public byte [] getBytes() {    	
    	int ln = len.getValue();
    	return getBytes(ln);    	    	
    }
    
    /**
     * Get received string data as bytes with specific length
     * @param ln
     * @return
     */
    @Override
	public byte [] getBytes(int ln) {
    	
    	int fnc = fn.getValue();    	
    	boolean contains = IntStream.of(fnChars).anyMatch(x -> x == fnc);
    	
    	if (contains) {
    		ln = ln * 2;
    	}
    	return ref.getBytes(ln);    	
    }
    
    /**
     * Read received string data from hll message 
     * @return
     */
    @Override
	public String getData() {
    	
    	int fnc = fn.getValue();
    	int ln = len.getValue();
    	
    	boolean contains = IntStream.of(fnChars).anyMatch(x -> x == fnc);
    	
    	if (contains) {
    		ln = ln * 2;
    	}

    	if (fnc == 200) {    		
    		return ref.getValue(ln, CODEUTF8);	
    	}
    	return ref.getValue(ln, CODEPAGE);

    	//return ref.getValueW(ln);
    }

    @Override
	public String getData(final boolean asHex) {
    	
    	String data = null;
    	
    	if (asHex) {
	    	byte [] tmp = getBytes();    		
			data = Util.bytesToHex(tmp);
    	} else {
    		data = getData();
    	}
    	
    	return data;
    }
    
    /**
     * Generic hll read for functions without data
     * Use for asynchronous call.
     * 
     * @param functionNum
     * @param psPosition
     * @return
     */
	public long read(int functionNum, int psPosition) {
		return read(functionNum, null, 0, psPosition);
	}

	/**
	 * Generic hll read for functions with data
	 * Use for asynchronous call.
	 * 
	 * @param functionNum
	 * @param data
	 * @param psPosition
	 * @return
	 */
    public long read(int functionNum, String data, int psPosition) {
    	long retVal = -1;
    	if (data == null) {
    		retVal = read(functionNum, null, 0, psPosition);
    	} else {
    		retVal = read(functionNum, data, data.length(), psPosition);    		
    	}
    	return retVal;
    }

    /**
     * Generic hll read by specification format
     * Use for asynchronous call.
     * 
     * @param functionNum
     * @param data
     * @param length
     * @param psPosition
     * @return
     */
	public long read(int functionNum, String data, int length, int psPosition) {
		
		fn.setValue(functionNum);
		len.setValue(length);
		pos.setValue(psPosition);
		
		setRef(data, functionNum, length);
				
		final NativeLong  res = INSTANCE.hllapi_read(fn, ref.getPointer(), len, pos);
		
		return res.longValue();
    }
	
	/**
	 * Generic hll write for functions without data
	 * Use for asynchronous call.
	 * 
	 * @param functionNum
	 * @param psPosition
	 * @return
	 */
	public long write(int functionNum, int psPosition) {
		return write(functionNum, null, 0, psPosition);
	}

	/**
	 * Generic hll write for functions with data
	 * Use for asynchronous call.
	 * 
	 * @param functionNum
	 * @param data
	 * @param psPosition
	 * @return
	 */
    public long write(int functionNum, String data, int psPosition) {
    	long retVal = -1;
    	if (data == null) {
    		retVal = write(functionNum, null, 0, psPosition);
    	} else {
    		retVal = write(functionNum, data, data.length(), psPosition);    		
    	}
    	return retVal;
    }

    /**
     * Generic hll read by specification format
     * Use for asynchronous call.
     * 
     * @param functionNum
     * @param data
     * @param length
     * @param psPosition
     * @return
     */
	public long write(int functionNum, String data, int length, int psPosition) {
		
		fn.setValue(functionNum);
		len.setValue(length);
		pos.setValue(psPosition);
		
		setRef(data, functionNum, length);
				
		final NativeLong  res = INSTANCE.hllapi_write(fn, ref.getPointer(), len, pos);
		
		return res.longValue();
    }
	
	/**
	 * Generic hll call (read/write) for functions without data
	 * Use for synchronous call.
	 * 
	 * @param functionNum
	 * @param psPosition
	 * @return
	 */
	public long call(int functionNum, int psPosition) {
		return call(functionNum, null, 0, psPosition);
	}

	/**
	 * Generic hll call (read/write) for functions with data
	 * Use for synchronous call.
	 * 
	 * @param functionNum
	 * @param data
	 * @param psPosition
	 * @return
	 */
    public long call(int functionNum, String data, int psPosition) {
    	long retVal = -1;
    	if (data == null) {
    		retVal = call(functionNum, null, 0, psPosition);
    	} else {
    		retVal = call(functionNum, data, data.length(), psPosition);    		
    	}
    	return retVal;
    }
    
    /**
     * Generic hll call (read/write) by specification format
     * Use for synchronous call.
     * 
     * @param functionNum
     * @param data
     * @param length
     * @param psPosition
     * @return
     */
	public long call(int functionNum, String data, int length, int psPosition) {

		/*
		if (async) {
			throw new RuntimeException("Synchronous functions not supported in async mode");
		}
		*/
		
		fn.setValue(functionNum);
		len.setValue(length);
		pos.setValue(psPosition);
		
		setRef(data, functionNum, length);
				
		NativeLong res = INSTANCE.hllapi(fn, ref.getPointer(), len, pos);

		return res.longValue();
    }
    
	/**
	 * Prepare String reference for hll api call
	 * @param data
	 * @param functionNum
	 * @param length
	 */
	void setRef(final String data, final int functionNum, final int length) {		
		if (data == null || data.length() == 0) {
			ref = new StringByReference(length);
		} else if (functionNum == 200) {
			ref = StringByReference.get(data, CODEUTF8);
		} else {
			ref = StringByReference.get(data, CODEPAGE);
		}
	}

	/**
	 * Get last function used.
	 * Might not be correct for async calls
	 * 
	 * @return
	 */
	@Override
	public int getFn() {
		return fn.getValue();
	}

	/**
	 * Get last PS used.
	 * Might not be correct for async calls
	 * 
	 * @return
	 */
	@Override
	public int getPS() {
		return pos.getValue();
	}
	
	/**
	 * Get last data length used.
	 * Might not be correct for async calls
	 * 
	 * @return
	 */
	@Override
	public int getLength() {
		return len.getValue();
	}
	
	/**
	 * HLL API function call 1 helper
	 * 
	 * @param data
	 * @param psPosition
	 * @return
	 */
	public long connectPS(String data, int psPosition) {
		if (data == null || data.length()<1) {
			return 9;
		}
		return call(1, data, 1, psPosition);
	}
	
	// TODO - see spec how to call it
	/**
	 * HLL API function call 9 helper
	 * 
	 * @param data
	 * @param psPosition
	 * @return
	 */
	public long convertPosition(String data, int psPosition) {
		return call(99, data, psPosition);
	}
	
	/**
	 * HLL API function call 34 helper
	 * 
	 * @param psPosition
	 * @return
	 */
	public long copyFieldToString(int psPosition) {
		return call(34, psPosition);
	}
	
	/**
	 * HLL API function call 13 helper
	 * 
	 * @return
	 */
	public long copyOIA() {
		return call(13, null, 104, 0);
	}
	
	/**
	 * HLL API function call 5 helper
	 * 
	 * @return
	 */
	public long copyPS() {
		return call(5, null, 27*132, 0);
	}
	
	/**
	 * HLL API function call 8 helper
	 * 
	 * @param length
	 * @param psPosition
	 * @return
	 */
	public long copyPSToString(int length, int psPosition) {
		return call(8, null, length, psPosition);
	}

	/**
	 * HLL API function call 8 helper
	 * 
	 * @param data
	 * @param psPosition
	 * @return
	 */
	public long copyPSToString(String data, int psPosition) {
		return call(8, data, psPosition);
	}
	
	/**
	 * HLL API function call 33 helper
	 * 
	 * @param data
	 * @param psPosition
	 * @return
	 */
	public long copyStringToField(String data, int psPosition) {
		return call(33, data, psPosition);
	}

	/**
	 * HLL API function call 15 helper
	 * 
	 * @param data
	 * @param psPosition
	 * @return
	 */
	public long copyStringToPS(String data, int psPosition) {
		return call(15, data, psPosition);
	}	
	
	
	/**
	 * HLL API function call 2 helper
	 * 
	 * @return
	 */
	public long disconnectPS() {
		return call(2, null, 0, 0);
	}
	
	/**
	 * HLL API function call 32 helper
	 * 
	 * @param data
	 * @return
	 */
	public long findFieldLength(String data) {
		return call(32, data, 0);
	}
	
	/**
	 * HLL API function call 31 helper
	 * 
	 * @param data
	 * @return
	 */
	public long findFieldPosition(String data) {
		return call(31, data, 0);
	}	
	
	/**
	 * HLL API function call 51 helper
	 * 
	 * @param data
	 * @return
	 */
	public long getKey(String data) {
		return call(51, data, 12, 0);
	}
	
	/**
	 * HLL API function call 18 helper
	 * 
	 * @return
	 */
	public long pause() {
		return call(18, 0);
	}
	
	/**
	 * HLL API function call 7 helper
	 * 
	 * @return
	 */
	public long queryCursorLocation() {
		return call(7, 0);
	}
	
	/**
	 * HLL API function call 14 helper
	 * 
	 * @param psPosition
	 * @return
	 */
	public long queryFieldAttribute(int psPosition) {
		return call(14, 0);
	}
	
	/**
	 * HLL API function call 24 helper
	 * 
	 * @return
	 */
	public long queryHostUpdate() {
		return call(24, null, 0, 0);
	}
	
	/**
	 * HLL API function call 22 helper
	 * 
	 * @param data
	 * @return
	 */
	public long querySessionStatus(String data) {
		return call(22, data, 20, 0);
	}	
	
	/**
	 * HLL API function call 10 helper
	 * 
	 * @param length
	 * @return
	 */
	public long querySessions(int length) {		
		return call(10, null, 16*length, 0);
	}	
	
	/**
	 * HLL API function call 20 helper
	 * 
	 * @return
	 */
	public long querySystem() {
		return call(20, null, 36, 0);
	}
	
	/**
	 * HLL API function call 11 helper
	 * 
	 * @return
	 */
	public long reserve() {
		return call(11, null, 0);
	}
	
	/**
	 * HLL API function call 21 helper
	 * 
	 * @return
	 */
	public long resetSystem() {
		return call(21, null, 0);
	}

	/**
	 * HLL API function call 3 helper
	 * 
	 * @param data
	 * @return
	 */
	public long sendKey(String data) {
		return call(3, data, 0);
	}
	
	/**
	 * HLL API function call 40 helper
	 * 
	 * @param psPosition
	 * @return
	 */
	public long setCursor(int psPosition) {
		return call(40, psPosition);
	}

	/**
	 * HLL API function call 9 helper
	 * 
	 * @param data
	 * @param psPosition
	 * @return
	 */
	public long setSessionParameter(String data, int psPosition) {
		return call(9, data, psPosition);
	}

	/**
	 * HLL API function call 23 helper
	 * 
	 * @param data
	 * @return
	 */
	public long startHostNotification(String data) {
		return call(23, data, 16, 0);
	}

	/**
	 * HLL API function call 50 helper
	 * 
	 * TODO data is always 16 bytes, length is keystroke buffer -> return value
	 * 
	 * @param data
	 * @param length
	 * @return
	 */
	public long startKeyStrokeIntercept(String data, int length) {
		return call(50, data, length, 0);
	}
	
	/**
	 * HLL API function call 110 helper
	 * 
	 * @param data
	 * @return
	 */
	public long startPlayingMacro(String data) {
		return call(110, data, 10, 0);
	}
	
	/**
	 * HLL API function call 25 helper
	 * 
	 * @return
	 */
	public long stopHostNotification() {
		return call(25, null, 1, 0);
	}
	
	/**
	 * HLL API function call 53 helper
	 * 
	 * @param data
	 * @return
	 */
	public long stopKeyStrokeIntercept(String data) {
		return call(53, data, 0);
	}
	
	/**
	 * HLL API function call 4 helper
	 * 
	 * @return
	 */
	public long waitSession() {
		return call(4, null, 0);
	}

	/**
	 * Send helper which calls appropriate function
	 * @param fn
	 * @param data
	 * @param len
	 * @param ps
	 * @return
	 */
	public long send(int fn, String data, int ps) {
    	
		long res = 9;
    	
    	if (fn == 5) {
    		res = copyPS();
    	} else if (fn == 8) {
    		//res = hll.copyPSToString(getNumeric(data, 0), ps);
    		res = copyPSToString(data, ps);
    	} else if (fn == 20) {
    		res = querySystem();
    	} else if (fn == 22) {
    		res = querySessionStatus(data);
    	} else if (fn == 10) {
    		res  = querySessions(getNumeric(data, 1));
    	} else if (fn == 50) {
    		res  = startKeyStrokeIntercept(data, 16);
    	} else {
    		if (async) {
    			write(fn, data, ps);
    		} else {
    			res = call(fn, data, ps);
    		}
    	}

    	return res;
	}
	
	/**
	 * String conversion into number
	 * @param value
	 * @param def
	 * @return
	 */
	public int getNumeric(final String value, final int def) {
		
		int fn = -1;
		
		try {
			fn = Integer.parseInt(value.trim());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			fn = def;
		}
		return fn;
	}
	
	// TODO
	public int sendHLLMessage(final int wParam, final int lParam) {		
		final WinDef.WPARAM _wParam = new WinDef.WPARAM(wParam); 
		final WinDef.LPARAM _lParam = new WinDef.LPARAM(lParam);
    	WinDef.UINT uint = INSTANCE.SendHLLMessage(_wParam, _lParam);
    	return uint.intValue();
	}
	
	// TODO
	public int postHLLMessage(final int wParam, final int lParam) {		
		final WinDef.WPARAM _wParam = new WinDef.WPARAM(wParam); 
		final WinDef.LPARAM _lParam = new WinDef.LPARAM(lParam);
    	final WinDef.UINT uint = INSTANCE.PostHLLMessage(_wParam, _lParam);
    	return uint.intValue();
	}
	
	public void postMessage(final int wParam, final int lParam) {
		User32.WPARAM _wParam = new User32.WPARAM(wParam);
        User32.LPARAM _lParam = new User32.LPARAM(lParam);        
        User32.INSTANCE.PostMessage(hwnd, msgid, _wParam, _lParam);
	}

}