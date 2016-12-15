package org.venraas.hermes.common;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class ValidDocID {

	/**
	 * _id, indexed document id in ES
	 * Each document is a mapping between input parameter and a back-end service API 
	 */
	@NotNull 
	@Size(max = 32)
	@Pattern(regexp = "[A-Za-z0-9_\\-]*")	
	String mid;
	
	
	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

}
