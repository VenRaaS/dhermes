package org.venraas.hermes;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
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
	
	static RequestConfig _reqConfig = null;
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(APIConnector.class);
	
	public APIConnector() {
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
	
	public String post(String apiURL, String body) {
//TODO... $apiURL health check MAP and periodical resume polling		
				
		String resp = "";
		
		CloseableHttpClient httpClient = null;		
		
		try {
			httpClient =  HttpClients.createDefault();
			
			StringEntity body_entity = new StringEntity(body);
			body_entity.setContentType("application/json");
			
			HttpPost post = new HttpPost(apiURL);
			post.setEntity(body_entity);
//TODO... post.setConfig(_reqConfig);
			
			StrRespHandler respHd = new StrRespHandler();
			resp = httpClient.execute(post, respHd);
			
		} catch (Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		}
		finally {
			try {				
				if (null != httpClient) httpClient.close();
			} catch (Exception ex) {
				VEN_LOGGER.error(Utility.stackTrace2string(ex));
				VEN_LOGGER.error(ex.getMessage());
			}				
		}
				
		return resp;
	}
	
	class StrRespHandler implements ResponseHandler<String> {
		
		@Override
		public String handleResponse(final HttpResponse response) {
			
			String resp = "";
			
			int status = response.getStatusLine().getStatusCode();
			
			try {
				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();					
					if (null != entity) 
						resp = EntityUtils.toString(entity);															
				} else {
					VEN_LOGGER.warn("Unexpected response http status code: {}", status);				
				}
			} catch (ParseException | IOException ex) {
				VEN_LOGGER.error(Utility.stackTrace2string(ex));
				VEN_LOGGER.error(ex.getMessage());
			}
			
			return resp;
		}
		
	}
	

}
