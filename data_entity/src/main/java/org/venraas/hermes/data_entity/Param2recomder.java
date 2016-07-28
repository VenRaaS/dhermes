package org.venraas.hermes.data_entity;

import java.util.List;

public class Param2recomder {	
	
	String group_key;
	
	String traffic_type;
	
	String api_url;
	
	List<String> keys2recomder;
	
	List<String> output_params;
	
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

	public List<String> getKeys2recomder() {
		return keys2recomder;
	}

	public void setKeys2recomder(List<String> keys2recomder) {
		this.keys2recomder = keys2recomder;
	}

	public List<String> getOutput_params() {
		return output_params;
	}

	public void setOutput_params(List<String> output_params) {
		this.output_params = output_params;
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

}
