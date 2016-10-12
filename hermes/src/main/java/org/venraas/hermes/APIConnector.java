package org.venraas.hermes;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.common.Constant;
import org.venraas.hermes.common.Utility;
import org.venraas.hermes.context.Config;

import com.google.common.io.CharStreams;


public class APIConnector {
	
	private static CloseableHttpClient _httpClient = null;
	
	private static final APIConnector _conn = new APIConnector();
	
	private static final ConcurrentHashMap<String, APIStatus> _APIStatusMap = new ConcurrentHashMap<String, APIStatus>(); 
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(APIConnector.class);
	
			
	private APIConnector() {
/*///
 		if (null == _httpClient) {
			synchronized (APIConnector.class){
				if (null == _httpClient) {
					_httpClient = HttpClients.createDefault();										
				}				
			}
		}

		
		if (null == _reqConfig) {
			synchronized (APIConnector.class) {
				if (null == _reqConfig) {
					_reqConfig = RequestConfig.custom()
				    .setConnectionRequestTimeout(Constant.HTTP_REQUEST_TIMEOUT)
				    .build();
				}
			}
		}
*/
	}
	
	public static APIConnector getInstance() {
		
		if (null == _httpClient) {
			
			synchronized (APIConnector.class) {
				
				if (null == _httpClient) { 
					PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
				    cm.setMaxTotal(Constant.CONNECTION_POOL_MAX_TOTAL);
				    
				    _httpClient = HttpClients.custom()
				    	.setConnectionManager(cm)
				    	.setConnectionManagerShared(true)
				    	.build();
				}
			}
		}
		
		return _conn;
	}
	
	public String post(String apiURL, String body, HttpServletRequest req, List<String> headers) {				
		if (null == apiURL || apiURL.isEmpty()) return "";
		
//TODO... $apiURL health check MAP and periodical resume polling
		_APIStatusMap.putIfAbsent(apiURL, new APIStatus(apiURL));
		
		APIStatus status = _APIStatusMap.get(apiURL);
		if (status.isSuspending()) return "";
		
		Map.Entry<Integer, String> resp = new AbstractMap.SimpleEntry<Integer, String>(-1, "");				
			
		try {
			//-- validation
			new URL(apiURL);		
			
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
		} catch (ConnectTimeoutException | ConnectException ex) {			
			VEN_LOGGER.error("{} on {}",  ex.getMessage(), apiURL);
			_connectTimeoutHelper(apiURL);
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
				
		return resp.getValue();
	}
	
	public boolean isSuspending(String apiURL) {				
		if (null == apiURL || apiURL.isEmpty()) return true;
			
		APIStatus status = _APIStatusMap.get(apiURL);
		if (null == status) return true;
		
		return status.isSuspending();
	}
	
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
				.setConnectTimeout(timeout)
				.build();		
	}
	
	private void _connectTimeoutHelper(String apiURL) {
		
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
	
	
	
	class StrRespHandler implements ResponseHandler<Map.Entry<Integer, String> > {
		
		@Override
		public Map.Entry<Integer, String> handleResponse(final HttpResponse response) {
			
			Map.Entry<Integer, String> resp = new AbstractMap.SimpleEntry<Integer, String>(-1, "");
			
			int status = response.getStatusLine().getStatusCode();
			
			try {
				
				if (status >= 200 && status < 300) {					
					HttpEntity entity = response.getEntity();					
					if (null != entity) {						
						String respStr = EntityUtils.toString(entity);
						resp = new AbstractMap.SimpleEntry<Integer, String>(status, respStr);
					}
				} else {
					VEN_LOGGER.warn("Unexpected response http status code: {}", status);				
				}
			} catch (ParseException | IOException ex) {
				VEN_LOGGER.error(ex.getMessage());
				VEN_LOGGER.error(Utility.stackTrace2string(ex));				
			}
			
			return resp;
		}
		
	}
	
		
	class APIStatus {
		
		String apiURL = "";
		
	    boolean suspending = false;
		
		Date connFailBeg_dt = new Date();
		
		Date suspendBeg_dt = new Date();
		
		AtomicInteger cnt_connFail = new AtomicInteger(0);
		
		
		public APIStatus() {}
		
		public APIStatus(String url) {
			apiURL = url;
		}
		
		public boolean isSuspending() {
			
			if (suspending) {
				
				synchronized (this) {
					
					if (suspending) {								
						long suspendPeriod = getSuspendingDurationSec();					
						Config conf = Config.getInstance();
						
						if (conf.getConn_fail_resume_interval() < suspendPeriod) {
							VEN_LOGGER.info("try to resume API: {} after suspending in {} seconds.", apiURL, suspendPeriod);
							
							try {
								URL url = new URL(apiURL);								
								String result = CharStreams.toString(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));								
								clearSuspending();
								
								VEN_LOGGER.info("resume API: {} successfully after suspending in {} seconds.", apiURL, suspendPeriod);
							} catch (Exception ex) {								
								setSuspending();
								VEN_LOGGER.info("resume API: {} fail.", apiURL);
								
								VEN_LOGGER.error(ex.getMessage());
								VEN_LOGGER.error(Utility.stackTrace2string(ex));
							}	
						}
					}
				}
			}
			
			return suspending;
		}		

		public void setSuspending() {
			if (! suspending) {
				synchronized (this) {
					if (! suspending) {
						suspending = true;
						suspendBeg_dt = new Date();
					}
				}
			}
		}
		
		public void clearSuspending() {
			if (suspending) {
				synchronized (this) {
					if (suspending) {
						suspending = false;
						cnt_connFail.set(0);
					}
				}
			}
		}
		
		public long getSuspendingDurationSec() {
			synchronized (this) {
				return Utility.duration_sec(suspendBeg_dt, new Date());
			}
		}
		
		public int failIncrementAndGet() {
			int failCnt = cnt_connFail.get();
			
			if (! suspending) {
				synchronized (this) {
					if (! suspending) {
						failCnt = cnt_connFail.incrementAndGet();
						if (1 == failCnt) connFailBeg_dt = new Date();						
					}
				}
			}
			
			return failCnt;
		}
		
		public void clearConnFailCnt() {
			synchronized (this) {
				cnt_connFail.set(0);
			}			
		}
		
		public long getConnFailDurationSec() {
			synchronized (this) {
				long dursec = Utility.duration_sec(connFailBeg_dt, new Date());
				return dursec;
			}
		}
		
		
	}

}
