/*
 * Copyright (C) 2015, 2016  Green Screens Ltd.
 */
package io.greenscreens.application;

import io.greenscreens.async.HllEngine;
import io.greenscreens.async.MessageListener;
import io.greenscreens.hllapi.IHllAPI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.stage.WindowEvent;

/**
 * Asynchronous blocking HLL call example.
 * Calls will not block. Received data is processed by listener
 *
 */
public class SampleControllerAsync implements MessageListener {
		
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
	
	HllEngine engine = null;
	
	public void initialize() {
	  txtPS.setText("0");
	  txtLog.setFont(Font.font("Lucida Console"));
	  engine = new HllEngine(this);
	  
	  ApplicationGUI.stage.setOnCloseRequest((WindowEvent evt) -> {
	        engine.release();
	    });
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
				fn = engine.getNumeric(args[0], 0);
			} else {
				fn = 0;
			}
			
			if (args.length>1) {
				data = args[1];
			} else {
				data = "";
			}
			
			if (args.length>2) {
				ps = engine.getNumeric(args[2], 0);				
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

	}
	
	@FXML
	public void onWrite(ActionEvent e) {
		
		int fn = getFunction();
		int ps = getPS();
		int len = getLen();
		final String data = txtData.getText();
		
		preSend(fn, ps, len, data);
		long ret = engine.send(fn, data, ps);
		postSend(true, ret, engine.getApi());
	}
	
	public long onSend(int fn, int ps, int len, String data) {
		
    	if (fn == 2) {
    		txtLog.clear();
    	}
    	    	
    	preSend(fn, ps, len, data );		
    	long res = engine.send(fn, data, ps);
    	postSend(true, res, engine.getApi());
    	    	
    	int ret = engine.post(getPS(), 100);
    	System.out.println(ret);
    	
    	return res;
	}

	public void preSend(int fn, int ps, int len, String data ) {
    	final String log = Util.getLog(true, fn, len, ps, 0, data);
    	txtLog.appendText(log);
	}
	
	public void postSend(boolean isSend, long res, final IHllAPI hll) {
    	txtLog.appendText(Util.getLog(isSend, false, res, hll));
    	txtLog.appendText(Util.getLog(isSend, true, res, hll));
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
		return engine.getNumeric(txtPS.getText(), 0);
	}

	@Override
	public void onData(final IHllAPI api, final long retVal) {
		postSend(false, retVal, api);		
		engine.releaseOnRequest();	
	}
	
}
