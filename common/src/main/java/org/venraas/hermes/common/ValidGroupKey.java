package org.venraas.hermes.common;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class ValidGroupKey {

	@NotNull 
	@Size(max = 64)
	@Pattern(regexp = "[A-Za-z0-9_.\\-]*")
	String grpkey;
	
	public String getGrpkey() {
		return grpkey;
	}

	public void setGrpkey(String grpkey) {
		this.grpkey = grpkey;
	}
}
