package org.venraas.hermes;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.common.Constant;
import org.venraas.hermes.common.Utility;

public class APIConnector {
	
	static CloseableHttpClient _httpClient = null;
	static RequestConfig _reqConfig = null;
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(APIConnector.class);
	
	public APIConnector() {
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
	}
	
	public String post(String apiURL, String body) {
//TODO... $apiURL health check MAP and periodical resume polling		
		
		String resp = "";

		HttpPost post = new HttpPost(apiURL);
		CloseableHttpResponse httpResponse = null;
		
		try {
			StringEntity body_entity = new StringEntity(body);
			body_entity.setContentType("application/json");			
			post.setEntity(body_entity);
//TODO... post.setConfig(_reqConfig);
			
			httpResponse = _httpClient.execute(post);
			int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
			if (String.valueOf(httpStatusCode).startsWith("2")) {
				HttpEntity entity = httpResponse.getEntity();
			    if (entity != null) {			        
			        resp = EntityUtils.toString(entity);
//			        long len = entity.getContentLength();
//			        if (len != -1 || 5120 <= len) {
//			        	VEN_LOGGER.warn("invalid size of reponse message");
//			        }
			    }
			}
			else {
				VEN_LOGGER.warn("HTTP error code : {}", httpResponse.getStatusLine().getStatusCode());
			}
		}
		catch (Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		}
		finally {
			try {
				if (null != httpResponse) httpResponse.close();
			} catch (Exception ex) {
				VEN_LOGGER.error(Utility.stackTrace2string(ex));
				VEN_LOGGER.error(ex.getMessage());
			}				
		}
		
		return resp;
	}
	
	
	

}
