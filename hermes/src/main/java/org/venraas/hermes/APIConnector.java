package org.venraas.hermes;

import java.net.URL;
import java.util.AbstractMap;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.common.Constant;
import org.venraas.hermes.common.EnumTrafficType;
import org.venraas.hermes.common.Utility;
import org.venraas.hermes.context.Config;


public class APIConnector {
	
	private static CloseableHttpClient _httpClient = null;
	
	private static final ConcurrentHashMap<String, APIStatus> _APIStatusMap = new ConcurrentHashMap<String, APIStatus>();
	
	private static final ConnectionEvictionMonitor _cem;		

	static {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
		//-- total connection limit
	    cm.setMaxTotal(Constant.CONNECTION_POOL_MAX_TOTAL);
	    //-- connection limit per host
	    cm.setDefaultMaxPerRoute(Constant.CONNECTION_POOL_MAX_PER_ROUTE);
	    
	    _httpClient = HttpClients.custom()
	    	.setConnectionManager(cm)
	    	.setConnectionManagerShared(true)
	    	.build();
	    
	    _cem = new ConnectionEvictionMonitor(cm); 
	    _cem.start();
	}
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(APIConnector.class);
	
	
	public APIConnector() { }		
	
	public String post(EnumTrafficType tt, String apiURL, String apiURL_failover, String body, HttpServletRequest req, List<String> headers) {
		String rs = ""; 
		
		Map.Entry<Integer, String> resp = post(tt, apiURL, body, req, headers);		
		int status = resp.getKey();
		
		//-- error apply normal
		if (EnumTrafficType.Normal != tt) {
			if (status < 200 || 300 <= status) {
				if (null != apiURL_failover && ! apiURL_failover.isEmpty()) {
					resp = post(EnumTrafficType.Normal, apiURL_failover, body, req, headers);
				}
			}
		}
		
		rs =  resp.getValue();
		return rs;
	}
	
	public Map.Entry<Integer, String> post(EnumTrafficType tt, String apiURL, String body, HttpServletRequest req, List<String> headers) {
		
		Map.Entry<Integer, String> resp = new AbstractMap.SimpleEntry<Integer, String>(-1, "");
		
		if (null == apiURL || apiURL.isEmpty()) return resp;
		
		_APIStatusMap.putIfAbsent(apiURL, new APIStatus(apiURL));

		//-- suspending only for Test channel
		APIStatus status = _APIStatusMap.get(apiURL);
		if (EnumTrafficType.Normal != tt && status.isSuspending()) 
			return resp;
			
		try {		
			HttpPost post = new HttpPost(apiURL);

			//-- forwarding headers
			for (String h : headers) {
				Enumeration<String> vals = req.getHeaders(h);
				while (vals.hasMoreElements()) {
					String v = vals.nextElement();
					post.addHeader(h, v);
				}
			}

			StringEntity body_entity = new StringEntity(body);
			body_entity.setContentType("application/json");
			post.setEntity(body_entity);						
			post.setConfig(getTimeoutConfig());
												
			StrRespHandler resHd = new StrRespHandler();
			resp = _httpClient.execute(post, resHd);
//		} catch (ConnectTimeoutException | ConnectException ex) {			
//			VEN_LOGGER.error("{} on {}",  ex.getMessage(), apiURL);
//			_connectFailHelper(apiURL);
		} catch (Exception ex) {
			VEN_LOGGER.error("{} on {}",  ex.getMessage(), apiURL);
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			
			if (EnumTrafficType.Normal == tt) {
				VEN_LOGGER.error(String.format("Normal channge %s timeout or connection fail.", apiURL));
			} 
			else {
				_connectFailHelper(apiURL);
			}			
		}
		finally {
			try {
				if (null != _httpClient) _httpClient.close();
			} catch (Exception ex) {
				VEN_LOGGER.error("{} on {}",  ex.getMessage(), apiURL);
				VEN_LOGGER.error(Utility.stackTrace2string(ex));				
			}				
		}
				
		return resp;
	}
	
///	public boolean isSuspending(String apiURL) {				
///		if (null == apiURL || apiURL.isEmpty()) return true;
///			
///		APIStatus status = _APIStatusMap.get(apiURL);
///		if (null == status) return true;
///		
///		return status.isSuspending();
///	}
	
	public boolean isValidURL(String apiURL, String body) {
						
		boolean isValid = false;
		
		try {
			new URL(apiURL);
			
			StringEntity body_entity = new StringEntity(body);
			body_entity.setContentType("application/json");
			
			HttpPost post = new HttpPost(apiURL);
			post.setEntity(body_entity);
			post.setConfig(getTimeoutConfig());

			StrRespHandler resHd = new StrRespHandler();
			Map.Entry<Integer, String> resp = _httpClient.execute(post, resHd);
			
			if (resp.getKey() >= 200 && resp.getKey() < 300) {
				isValid = true;				
			}
		} catch (Exception ex) {
			VEN_LOGGER.error("{} on {}",  ex.getMessage(), apiURL);
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
		}
		finally {
			try {
				if (null != _httpClient) _httpClient.close();
			} catch (Exception ex) {
				VEN_LOGGER.error("{} on {}",  ex.getMessage(), apiURL);
				VEN_LOGGER.error(Utility.stackTrace2string(ex));				
			}				
		}
				
		return isValid;
	}
	
	public RequestConfig getTimeoutConfig() {
		int timeout = Config.getInstance().getConn_timeout();
		
		return RequestConfig.custom()
				.setSocketTimeout(timeout)
				.setConnectTimeout(timeout)
				.setConnectionRequestTimeout(timeout)
				.build();
	}
	
	private void _connectFailHelper(String apiURL) {
		
		if (null == apiURL || apiURL.isEmpty()) return;
		
		_APIStatusMap.putIfAbsent(apiURL, new APIStatus(apiURL));
		APIStatus s = _APIStatusMap.get(apiURL);
		
		synchronized (s) {
			
			if (! s.isSuspending()) {
				int failCnt = s.failIncrementAndGet();
				
				Config conf = Config.getInstance();
				if (conf.getConn_fail_cond_count() <= failCnt) {
					long failPeriod = s.getConnFailDurationSec();
										
					if (failPeriod <= conf.getConn_fail_cond_interval()) {
						//-- satisfy suspending condition  
						s.setSuspending();						
						VEN_LOGGER.error("API: {} is suspending for {} sec due to {} connection fails within {} sec", 
								apiURL, conf.getConn_fail_resume_interval(), 
								failCnt, conf.getConn_fail_cond_interval());						
					}
					else {
						//-- reset fail counting
						s.clearConnFailCnt();;
					}
				}
			}	
		}
		
	}
		
	
		
	
}
