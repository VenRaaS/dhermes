package org.venraas.hermes.apollo.hermes;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
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

	public int get_routing_reset_interval(String codeName) {
		int interval = _client.get_routing_reset_interval(codeName);		
		return interval;
	}
	
	public double get_traffic_percent_normal(String codeName) { 
		double pct = _client.get_traffic_percent_normal(codeName);		
		return pct;
	}
	
}
