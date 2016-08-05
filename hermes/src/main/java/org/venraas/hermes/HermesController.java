package org.venraas.hermes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.venraas.hermes.apollo.hermes.Param2recomderClient;
import org.venraas.hermes.apollo.mappings.EnumParam2recomder;
import org.venraas.hermes.apollo.raas.CompanyClient;
import org.venraas.hermes.common.EnumOptionBase;
import org.venraas.hermes.common.Utility;
import org.venraas.hermes.common.option.RecOption;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


@RestController
@RequestMapping("/api")
public class HermesController {
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(HermesController.class);
	
	@CrossOrigin
	@RequestMapping(value = "/goods/rank", method = RequestMethod.GET)	
	public Object get_goods_rank_GET(@RequestParam Map<String, Object> paramMap) {
		return get_goods_rank(paramMap);
	}
	
	@CrossOrigin
	@RequestMapping(value = "/goods/rank", method = RequestMethod.POST)	
	public Object get_goods_rank_POST(@RequestBody String jsonStr) {
		Gson gson = new Gson();
		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		Map<String, Object> paramMap = gson.fromJson(jsonStr, type);
		
		return get_goods_rank(paramMap);
	}
	
	@CrossOrigin
	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public Map<String, Object> hello(@RequestParam Map<String, Object> m)
	{		
		return m;
	}
	
	@CrossOrigin
	@RequestMapping(value = "/hello", method = RequestMethod.POST)	
	public Map<String, Object> hello(@RequestBody String jsonStr)
	{
		Gson gson = new Gson();
		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		Map<String, Object> m = gson.fromJson(jsonStr, type);

//		Map<String, String> m = gson.fromJson(s, Map);

		return m;
	}
	
	
	private Map<String, Object> get_goods_rank(Map<String, Object> paramMap) {
		String clientID = String.format("%s_%s_%s", 
				paramMap.get(EnumOptionBase.token.name()), 
				paramMap.get(EnumOptionBase.ven_guid.name()), 
				paramMap.get(EnumOptionBase.ven_session.name()));

		CompanyClient comClient = new CompanyClient();
		String token = (String)paramMap.get(EnumOptionBase.token.name());
		String codeName = comClient.getCodeName(token);
						
		GroupRoute gr = new GroupRoute();
		String grpKey = gr.routing(codeName, clientID);		
		
		Map<String, Object> mapping = null;		
		Param2recomderClient p2r = new Param2recomderClient();
		List<Map<String, Object>> maps = p2r.getGroupMapping(codeName, grpKey);		
		for (Map<String, Object> m : maps) {			
			boolean matchAllKeys = true;
			
			List<String> fields = (List<String>) m.get(EnumParam2recomder.keys2recomder.name());						
			for (String f : fields) {
				String inputV = (String) paramMap.get(f);
				String regV = (String) m.get(f);
				
				if (null == inputV || null == regV || 
					inputV.isEmpty() || regV.isEmpty() || 
					!inputV.equals(regV)) 
				{
					matchAllKeys = false;
					break;
				}
			}
			
			if (matchAllKeys) {
				mapping = m;
				break;
			} 
		}
			
		String resp = "";		
		if (null != mapping) {
			HashMap<String, Object> outParamMap = new HashMap<String, Object> (paramMap);
			List<String> apiURLs = (List<String>) mapping.get(EnumParam2recomder.api_url.name());
			List<String> fields = (List<String>) mapping.get(EnumParam2recomder.output_params.name());
			for (String f : fields) {
				String v = (String) mapping.get(f);
				outParamMap.put(f, v);			
			}
			
			Gson g = new Gson();
			String outParam = g.toJson(outParamMap);
			
			CloseableHttpClient httpClient = HttpClients.createDefault();
///TODO... LB and select an Available RecAPI
///$apiURLs null check			
			String apiURL = apiURLs.get(0);
			HttpPost post = new HttpPost(apiURL);
			CloseableHttpResponse httpResponse = null;
			try {
				StringEntity input = new StringEntity(outParam);
				input.setContentType("application/json");
				post.setEntity(input);
				
				httpResponse = httpClient.execute(post);
	
				int httpStatusCode = httpResponse.getStatusLine().getStatusCode();
				if (! String.valueOf(httpStatusCode).startsWith("2")) {
					throw new RuntimeException("Failed : HTTP error code : "
						+ httpResponse.getStatusLine().getStatusCode());
				}
				
				HttpEntity entity = httpResponse.getEntity();
			    if (entity != null) {			        
			        resp = EntityUtils.toString(entity);
//TODO...			        
			        long len = entity.getContentLength();
			        if (len != -1 || 5120 <= len) {			            
			        	VEN_LOGGER.warn("invalid size of reponse message");
			        }
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
		}
		else {
			Gson g = new Gson();
			VEN_LOGGER.warn("input param 2 recomder mapping cannot be found. input: {}", g.toJson(paramMap));			
		}
 				
		Gson g = new Gson();
		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		Map<String, Object> m = g.fromJson(resp, type);
		return m;	
	}	
	

}
