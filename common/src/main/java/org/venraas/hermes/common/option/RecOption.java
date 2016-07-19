package org.venraas.hermes.common.option;

import javax.validation.constraints.Pattern;

public class RecOption {
	
	@Pattern(regexp = "[A-Za-z0-9_]*")
	String token;
	
	@Pattern(regexp = "[A-Za-z0-9_.-]*")
	String ven_guid;
	
	@Pattern(regexp = "[A-Za-z0-9_.-]*")
	String ven_session;
	
	public String getVen_guid() {
		return ven_guid;
	}

	public void setVen_guid(String ven_guid) {
		this.ven_guid = ven_guid;
	}
	
	public String getVen_session() {
		return ven_session;
	}

	public void setVen_session(String ven_session) {
		this.ven_session = ven_session;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	
}
