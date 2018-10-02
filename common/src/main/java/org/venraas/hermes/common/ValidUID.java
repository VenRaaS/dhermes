package org.venraas.hermes.common;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


public class ValidUID {
		
	@Size(max = 64)
	@Pattern(regexp = "[A-Za-z0-9_.\\-=/+]*")
	String uid;
		
	@Size(max = 128)
	@Pattern(regexp = "[A-Za-z0-9_.\\-]*")
	String ven_guid;
	
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public String getVen_guid() {
		return ven_guid;
	}

	public void setVen_guid(String ven_guid) {
		this.ven_guid = ven_guid;
	}

}
