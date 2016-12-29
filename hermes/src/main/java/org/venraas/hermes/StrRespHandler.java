package org.venraas.hermes;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.common.Utility;


class StrRespHandler implements ResponseHandler<Map.Entry<Integer, String> > {
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(StrRespHandler.class);
	
	@Override
	public Map.Entry<Integer, String> handleResponse(final HttpResponse response) {
		
		Map.Entry<Integer, String> resp = null;					
		
		try {
			int status = response.getStatusLine().getStatusCode();				
			HttpEntity entity = response.getEntity();
			
			if (null != entity) {						
				String respStr = EntityUtils.toString(entity);
				resp = new AbstractMap.SimpleEntry<Integer, String>(status, respStr);
			}
			
			if (status < 200 || 300 <= status) {					
				VEN_LOGGER.warn("Unexpected response http status code: {}", status);				
			}
		} catch (ParseException | IOException ex) {
			VEN_LOGGER.error(ex.getMessage());
			VEN_LOGGER.error(Utility.stackTrace2string(ex));				
		}
		
		return resp;
	}
	
}
