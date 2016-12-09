package org.venraas.hermes.data_entity;

import java.util.ArrayList;
import java.util.List;

import org.venraas.hermes.common.Constant;
import org.venraas.hermes.common.EnumResetInterval;
import org.venraas.hermes.common.Utility;

public class Conf {
	
	double traffic_pct_normal = Constant.TRAFFIC_PERCENT_NORMAL;

	String routing_reset_interval = EnumResetInterval.DAY.name();
	
	List<String> http_forward_headers = new ArrayList<String>();

	String update_dt = Utility.now();
	
	
	public Conf() { }	
	
	public double getTraffic_pct_normal() {
		return traffic_pct_normal;
	}

	public void setTraffic_pct_normal(double traffic_pct_normal) {
		this.traffic_pct_normal = traffic_pct_normal;
	}
	
	public String getRouting_reset_interval() {
		return routing_reset_interval;
	}

	public void setRouting_reset_interval(String routing_reset_interval) {
		this.routing_reset_interval = routing_reset_interval;
	}
	
	public List<String> getHttp_forward_headers() {
		return http_forward_headers;
	}

	public void setHttp_forward_headers(List<String> http_forward_headers) {
		this.http_forward_headers = http_forward_headers;
	}

	public String getUpdate_dt() {
		return update_dt;
	}

	public void setUpdate_dt(String update_dt) {
		this.update_dt = update_dt;
	}

}
