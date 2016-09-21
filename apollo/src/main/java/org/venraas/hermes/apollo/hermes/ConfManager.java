package org.venraas.hermes.apollo.hermes;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.venraas.hermes.common.Constant;
import org.venraas.hermes.common.EnumResetInterval;
import org.venraas.hermes.common.Utility;
import org.venraas.hermes.context.AppContext;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ConfManager {

	static ConfClient _client = null;
	static ConfManager _mgr = new ConfManager();
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(ConfManager.class);
	
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
	
	public List<String> add_http_forward_headers(String codeName, String jsonArray) {
		
		List<String> rsHeaders = new ArrayList<String>();
		
		try {
			Gson g = new Gson();
			Type listType = new TypeToken<List<String>>() {}.getType();
			List<String> inHeaders = g.fromJson(jsonArray, listType);
			
			if (null != inHeaders && ! inHeaders.isEmpty()) {
				List<String> orgHeaders = _client.get_http_forward_headers(codeName, Constant.HERMES_CONF_HTTP_FORWARD_HEADER);
			
				//-- combine input and original headers without duplication
				Set<String> headerSet = new HashSet<String>(orgHeaders);
				for (String h : inHeaders) {
			        headerSet.add(h);
			    }
				rsHeaders.addAll(headerSet);
			
				rsHeaders = _client.set_http_forward_headers(codeName, Constant.HERMES_CONF_HTTP_FORWARD_HEADER, rsHeaders);
			}
		} 
		catch (Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));	
		}
		
		return rsHeaders;
	}
	
	public boolean set_http_forward_headers(String codeName, String jsonArray) {			
		boolean isSuccess = false;
		
		try {
			Gson g = new Gson();
			Type listType = new TypeToken<List<String>>() {}.getType();
			List<String> inHeaders = g.fromJson(jsonArray, listType);					
			
			if (null != inHeaders) {
				_client.set_http_forward_headers(codeName, Constant.HERMES_CONF_HTTP_FORWARD_HEADER, inHeaders);
				isSuccess = true;
			}
		} 
		catch (Exception ex) {			
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
		}
		
		return isSuccess;
	}
	
	

	
}
