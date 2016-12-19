package org.venraas.hermes.apollo.hermes;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.apollo.mappings.EnumConf;
import org.venraas.hermes.common.Constant;
import org.venraas.hermes.common.EnumResetInterval;
import org.venraas.hermes.common.Utility;
import org.venraas.hermes.data_entity.Conf;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class ConfManager {

	static ConfClient _client = new ConfClient();
	static LoadingCache<String, String> _cache_conf;
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(ConfManager.class);
	
	static {
		//-- Guava cache - https://github.com/google/guava/wiki/CachesExplained#refresh
		_cache_conf = CacheBuilder.newBuilder()
				.maximumSize(Constant.CACHE_SIZE_10K)						
				.refreshAfterWrite(Constant.NUM_TIMEUNIT_10, TimeUnit.MINUTES)
				.build(
					new CacheLoader<String, String>() {						
						public String load(String key) throws Exception {
							String rt = "";
							
							int qi = key.indexOf(Constant.FUNCTION_DELIMITER);
							String funName = key.substring(0, qi);
							String[] ps = key.substring(qi+1).split(String.valueOf(Constant.PARAM_DELIMITER));
							
							switch (funName) {
								case "get_routing_reset_interval":
									rt = _client.get_routing_reset_interval(ps[0], ps[1]);
									break;
								case "get_traffic_percent_normal":
									rt = _client.get_traffic_percent_normal(ps[0], ps[1]);
									break;
								case "get_http_forward_headers":
									rt = _client.get_http_forward_headers(ps[0], ps[1]);
									break;							
							}
							
							return rt;
						}
						
						public ListenableFuture<String> reload (final String key, String oldVal) {
							//-- async call to get the value from source
							ListenableFuture<String> task = 
								Utility.CacheRefreshLES.submit(
									new Callable<String>() {
										public String call() throws Exception {
											return load(key);
										}
									});
							
			                return task;
						}
					}
				);			
	}
	
	public ConfManager() {}	

	public EnumResetInterval get_routing_reset_interval(String codeName) {
		EnumResetInterval enumInt = EnumResetInterval.DAY;
		
		try
		{
			String k = String.format("get_routing_reset_interval?%s&%s", codeName, EnumConf.routing_reset_interval.name());
			String jsonStr = _cache_conf.get(k);
						
	    	Conf con =  Utility.json2instance(jsonStr, Conf.class);
	    	String val = con.getRouting_reset_interval();
	    	enumInt = EnumResetInterval.valueOf(val);
    	}
		catch (Exception ex) {
			VEN_LOGGER.error(ex.getMessage());
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
		}
				
		return enumInt;
	}
	
	public EnumResetInterval set_routing_reset_interval(String codeName, EnumResetInterval enumInt) {
		EnumResetInterval resetInterval = _client.set_routing_reset_interval(codeName, EnumConf.routing_reset_interval.name(), enumInt);
		
		String k = String.format("get_routing_reset_interval?%s&%s", codeName, EnumConf.routing_reset_interval.name());
		_cache_conf.refresh(k);
		
		return resetInterval;
	}
	
	public double get_traffic_percent_normal(String codeName) {
		
		double pct = Constant.TRAFFIC_PERCENT_NORMAL;
		
		try {
			String k = String.format("get_traffic_percent_normal?%s&%s", codeName, EnumConf.traffic_pct_normal.name());
			String jsonStr = _cache_conf.get(k);

	    	Conf con = Utility.json2instance(jsonStr, Conf.class);
	    	pct = con.getTraffic_pct_normal();
    	} 
		catch (Exception ex) {
			VEN_LOGGER.error(ex.getMessage());
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
		}
    	
		return pct;
	}
	
	public double set_traffic_percent_normal(String codeName, double pct) {
		_client.set_traffic_percent_normal(codeName, EnumConf.traffic_pct_normal.name(), pct);
		
		String k = String.format("get_traffic_percent_normal?%s&%s", codeName, EnumConf.traffic_pct_normal.name());
		_cache_conf.refresh(k);
		
		return pct;
	}
	
	public List<String> get_http_forward_headers(String codeName) {
		List<String> headers = new ArrayList<String>();
		
		try {
			String k = String.format("get_http_forward_headers?%s&%s", codeName, EnumConf.http_forward_headers.name());			
			String jsonStr = _cache_conf.get(k);
			Conf con = Utility.json2instance(jsonStr, Conf.class);			
        	headers = con.getHttp_forward_headers();	
		}
		catch (Exception ex) {
			VEN_LOGGER.error(ex.getMessage());
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
		}
		
		return headers;
	}
	
	public List<String> add_http_forward_headers(String codeName, String jsonArray) {
		
		List<String> rsHeaders = new ArrayList<String>();
		
		try {
			Gson g = new Gson();
			Type listType = new TypeToken<List<String>>() {}.getType();
			List<String> inHeaders = g.fromJson(jsonArray, listType);
			
			if (null != inHeaders && ! inHeaders.isEmpty()) {
				List<String> orgHeaders = get_http_forward_headers(codeName);
			
				//-- combine input and original headers without duplication
				Set<String> headerSet = new HashSet<String>(orgHeaders);
				for (String h : inHeaders) {
			        headerSet.add(h);
			    }
				rsHeaders.addAll(headerSet);
				rsHeaders = _client.set_http_forward_headers(codeName, EnumConf.http_forward_headers.name(), rsHeaders);
			}
			
			String k = String.format("get_http_forward_headers?%s&%s", codeName, EnumConf.http_forward_headers.name());			
			_cache_conf.refresh(k);
		} 
		catch (Exception ex) {
			VEN_LOGGER.error(ex.getMessage());
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
				_client.set_http_forward_headers(codeName, EnumConf.http_forward_headers.name(), inHeaders);
				isSuccess = true;
				
				String k = String.format("get_http_forward_headers?%s&%s", codeName, EnumConf.http_forward_headers.name());			
				_cache_conf.refresh(k);				
			}
		} 
		catch (Exception ex) {
			VEN_LOGGER.error(ex.getMessage());
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
		}
		
		return isSuccess;
	}
	
	

	
}
