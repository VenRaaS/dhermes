package org.venraas.hermes.common;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


public class ValidToken {
	
	@NotNull 
	@Size(max = 32)
	@Pattern(regexp = "[A-Za-z0-9_]*")	
	String token;

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
	

}
