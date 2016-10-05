package org.venraas.hermes.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utility {
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(Utility.class);

	static public String stackTrace2string(Exception ex) {

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);

		return sw.toString();
	}
	
	static public String now() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Date date = new Date();
    	
		return dateFormat.format(date);		
	}
	
	static public long duration_sec (Date beg, Date end) {
		return TimeUnit.SECONDS.toSeconds(end.getTime() - beg.getTime());		
	}
}
