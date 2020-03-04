/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.application;

import io.greenscreens.hllapi.HllAPI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;

/**
 * Synchronous blocking HLL call example.
 * Call function use write/read and returns after reply is sent back. 
 * If host does not respond, app will block.
 */
public class SampleController {
		
	@FXML
	ComboBox<StringPair> fldFunction;
	
	@FXML
	TextField txtData;
		
	@FXML
	TextField txtPS;
	
	@FXML
	TextArea txtLog;
	
	@FXML
	TextArea txtIns;
	
	HllAPI hll = new HllAPI();

	public void initialize() {
	  txtPS.setText("0");
	  txtLog.setFont(Font.font("Lucida Console"));
	}
	
	
	@FXML
	public void onPlay(ActionEvent e) {		
		
		String [] list = txtIns.getText().trim().split("\n");
		
		int fn = 0;
		int ps = 0;
		int len = 0;
		String data = "";
		
		long res = 0;
		
		for (String ins : list) {			
			String [] args = ins.split(";");
			
			if (args.length>0) {
				fn = hll.getNumeric(args[0], 0);
			} else {
				fn = 0;
			}
			
			if (args.length>1) {
				data = args[1];
			} else {
				data = "";
			}
			
			if (args.length>2) {
				ps = hll.getNumeric(args[2], 0);				
			} else {
				ps = 0;
			}
			
			len = data.length();				
			
			//if (fn > 0)
			{
				res = onSend(fn, ps, len, data);
				if (res == 9 || res == 4) break;
			}
			
		}
		
	}	
	
	
	@FXML
	public void onSend(ActionEvent e) {
		int fn = getFunction();
		int ps = getPS();
		int len = getLen();
		String data = txtData.getText();
		onSend(fn, ps, len, data);
	}

	@FXML
	public void onRead(ActionEvent e) {
		int fn = getFunction();
		int ps = getPS();
		int len = getLen();
		String data = txtData.getText();
		preSend(fn, ps, len, data);		
		long ret = hll.read(fn, data, ps);
		postSend(ret, hll);
	}
	
	@FXML
	public void onWrite(ActionEvent e) {
		int fn = getFunction();
		int ps = getPS();
		int len = getLen();
		final String data = txtData.getText();
		preSend(fn, ps, len, data );		
		long ret = hll.write(fn, data, ps);
		postSend(ret, hll);
	}
	
	public long onSend(int fn, int ps, int len, String data ) {
		
    	if (fn == 2) {
    		txtLog.clear();
    	}
    	
    	preSend(fn, ps, len, data );			
    	long res = hll.send(fn, data, ps);    	    	    	    	
    	postSend(res, hll);
    	    	
    	return res;
	}

	public void preSend(int fn, int ps, int len, String data ) {
    	final String log = Util.getLog(true, fn, len, ps, 0, data);
    	txtLog.appendText(log);
	}
	
	public void postSend(long res, final HllAPI hll) {
    	txtLog.appendText(Util.getLog(false, false, res, hll));
    	txtLog.appendText(Util.getLog(false, true, res, hll));
	}
	
	int getFunction() {
		StringPair pair = fldFunction.getValue();
		if (pair != null) {
			return pair.getKey();			
		}
		return 0;
	}

	int getLen() {
		return txtData.getText().length();
	}
	
	int getPS() {
		return hll.getNumeric(txtPS.getText(), 0);
	}

}
