package org.venraas.hermes.apollo.hermes;

import java.util.List;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.venraas.hermes.common.Constant;
import org.venraas.hermes.common.EnumResetInterval;
import org.venraas.hermes.context.AppContext;

public class ConfManager {

	static ConfClient _client = null;
	static ConfManager _mgr = new ConfManager();
	
	
	private ConfManager() {}
	
	static public ConfManager getInstance() {

		if (null == _client) {
			
			synchronized (ConfManager.class) {
				
				if (null == _client) {
					AnnotationConfigApplicationContext ctx = AppContext.getCacheAnnotContext();
					_client = ctx.getBean(ConfClient.class);
				}
			}
		}
		
		return _mgr;
	}
	
	public void reset() {
		_client.reset();
	}

	public EnumResetInterval get_routing_reset_interval(String codeName) {
		EnumResetInterval enumInt = _client.get_routing_reset_interval(codeName, Constant.HERMES_CONF_CACHE_ROUTING_RESET_INTERVAL);		
		return enumInt;
	}
	
	public EnumResetInterval set_routing_reset_interval(String codeName, EnumResetInterval enumInt) {
		return _client.set_routing_reset_interval(codeName, Constant.HERMES_CONF_CACHE_ROUTING_RESET_INTERVAL, enumInt);		
	}
	
	public double get_traffic_percent_normal(String codeName) { 
		double pct = _client.get_traffic_percent_normal(codeName, Constant.HERMES_CONF_CACHE_TRAFFIC_PCT);		
		return pct;
	}
	
	public double set_traffic_percent_normal(String codeName, double pct) {
		pct = _client.set_traffic_percent_normal(codeName, Constant.HERMES_CONF_CACHE_TRAFFIC_PCT, pct);		
		return pct;
	}
	
	public List<String> get_http_forward_headers(String codeName) {
		List<String> headers = _client.get_http_forward_headers(codeName, Constant.HERMES_CONF_HTTP_FORWARD_HEADER);		
		return headers;
	}
	
	

	
}
