package org.venraas.hermes.common;

import java.util.Calendar;

public enum EnumResetInterval {
	
		SECOND(Calendar.SECOND),
	
		MINUTE(Calendar.MINUTE),
		
		DAY(Calendar.DAY_OF_MONTH),	        			
		
		WEEK(Calendar.WEEK_OF_MONTH),
		
		MONTH(Calendar.MONTH),
	
		HOUR(Calendar.HOUR_OF_DAY);
	
	int _enumCode;
	
	EnumResetInterval(int code) {
		_enumCode = code;
	}
	
	public int get_enumCode() {
		return _enumCode;
	}


}
