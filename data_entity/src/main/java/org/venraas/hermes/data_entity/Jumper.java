package org.venraas.hermes.data_entity;

import org.venraas.hermes.common.Utility;

public class Jumper {
	
	String uid;
	
	String group_key;
	
	String update_dt = Utility.now();
	

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getGroup_key() {
		return group_key;
	}

	public void setGroup_key(String group_key) {
		this.group_key = group_key;
	}

	public String getUpdate_dt() {
		return update_dt;
	}

	public void setUpdate_dt(String update_dt) {
		this.update_dt = update_dt;
	}
	

}
