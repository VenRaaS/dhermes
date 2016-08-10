package org.venraas.hermes.data_entity;

import java.util.List;

public class Param2recomder {	

	String group_key;
	
	String traffic_type;
	
	String api_url;
	
	List<String> in_keys2recomder;
	
	List<String> out_aux_params;
	
	int availability;
	
	String update_dt;
		

	public String getGroup_key() {
		return group_key;
	}

	public void setGroup_key(String group_key) {
		this.group_key = group_key;
	}

	public String getTraffic_type() {
		return traffic_type;
	}

	public void setTraffic_type(String traffic_type) {
		this.traffic_type = traffic_type;
	}	

	public String getApi_url() {
		return api_url;
	}

	public void setApi_url(String api_url) {
		this.api_url = api_url;
	}

	public int getAvailability() {
		return availability;
	}

	public void setAvailability(int availability) {
		this.availability = availability;
	}

	public String getUpdate_dt() {
		return update_dt;
	}

	public void setUpdate_dt(String update_dt) {
		this.update_dt = update_dt;
	}
	
	public List<String> getIn_keys2recomder() {
		return in_keys2recomder;
	}

	public void setIn_keys2recomder(List<String> in_keys2recomder) {
		this.in_keys2recomder = in_keys2recomder;
	}

	public List<String> getOut_aux_params() {
		return out_aux_params;
	}

	public void setOut_aux_params(List<String> out_aux_params) {
		this.out_aux_params = out_aux_params;
	}

}
