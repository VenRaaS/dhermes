package org.venraas.hermes.data_entity;

import org.venraas.hermes.common.Constant;

public class Conf {
	
	double traffic_pct_normal = Constant.TRAFFIC_PERCENT_NORMAL;

	String routing_reset_interval;
	
	String update_dt;
	
	
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

	public String getUpdate_dt() {
		return update_dt;
	}

	public void setUpdate_dt(String update_dt) {
		this.update_dt = update_dt;
	}

}
