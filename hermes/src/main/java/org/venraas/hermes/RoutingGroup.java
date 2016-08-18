package org.venraas.hermes;

import org.venraas.hermes.common.Constant;

public class RoutingGroup {
	
	public static final String GROUP_KEY = "group_key";
	public static final String TRAFFIC_TYPE = "traffic_type";
	public static final String TRAFFIC_PCT = "traffic_pct";
	
	String group_key = Constant.NORMAL_GROUP_KEY;
	
	String traffic_type = Constant.TRAFFIC_TYPE_NORMAL;
	
	String traffic_pct = String.valueOf(Constant.TRAFFIC_PERCENT_NORMAL);

	public String getGroup_key() {
		return group_key;
	}

	public RoutingGroup setGroup_key(String group_key) {
		this.group_key = group_key;
		return this;
	}

	public String getTraffic_type() {
		return traffic_type;
	}

	public RoutingGroup setTraffic_type(String traffic_type) {
		this.traffic_type = traffic_type;
		return this;
	}

	public String getTraffic_pct() {
		return traffic_pct;
	}

	public RoutingGroup setTraffic_pct(String traffic_pct) {
		this.traffic_pct = traffic_pct;
		return this;
	}
	
	

}
