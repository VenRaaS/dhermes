package org.venraas.hermes;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.venraas.hermes.apollo.hermes.ConfManager;
import org.venraas.hermes.apollo.mappings.EnumParam2recomder;
import org.venraas.hermes.apollo.raas.CompanyManager;
import org.venraas.hermes.common.Constant;
import org.venraas.hermes.common.EnumOptionBase;
import org.venraas.hermes.common.Utility;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.twitter.jsr166e.ThreadLocalRandom;


@RestController
@RequestMapping("/api")
public class HermesController {
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(HermesController.class);
	
	@CrossOrigin
	@RequestMapping(value = "/goods/rank", method = RequestMethod.GET)	
	public Object get_goods_rank_GET(@RequestParam Map<String, Object> paramMap, HttpServletRequest req) {		
		return get_goods_rank(paramMap, req);
	}
	
	@CrossOrigin
	@RequestMapping(value = "/goods/rank", method = RequestMethod.POST)	
	public Object get_goods_rank_POST(@RequestBody String jsonStr, HttpServletRequest req) {		
		Gson gson = new Gson();
		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		Map<String, Object> paramMap = gson.fromJson(jsonStr, type);
		
		return get_goods_rank(paramMap, req);
	}
	
	
	
	private Map<String, Object> get_goods_rank(Map<String, Object> inParamMap, HttpServletRequest req) {
		
		String resp = "";
				
		String clientID = String.format("%s_%s_%s", 
				inParamMap.get(EnumOptionBase.token.name()), 
				inParamMap.get(EnumOptionBase.ven_guid.name()), 
				inParamMap.get(EnumOptionBase.ven_session.name()));
		
		try {
			String token = (String)inParamMap.get(EnumOptionBase.token.name());
			CompanyManager comMgr = CompanyManager.getInstance();
			String codeName = comMgr.getCodeName(token);			
			String uid = (String) inParamMap.getOrDefault(EnumOptionBase.uid.name(), ""); 
		
			//-- routing to target group according to $clientID
			GroupRoute gr = new GroupRoute();			
			RoutingGroup targetGrp = gr.routing(codeName, clientID, uid);
			
			Param2RestAPI p2r = new Param2RestAPI(codeName, targetGrp.getGroup_key());			
			Map<String, Object> mapping = p2r.getMapping(inParamMap);
			
			//-- redirect to Normal (default) Group, if input parameter doesn't match
			if (! Constant.NORMAL_GROUP_KEY.equalsIgnoreCase(targetGrp.group_key) && mapping.isEmpty()) {
				p2r = new Param2RestAPI(codeName, Constant.NORMAL_GROUP_KEY);
				mapping = p2r.getMapping(inParamMap);
			}
			
			if (! mapping.isEmpty()) {
				HashMap<String, Object> outParamMap = new HashMap<String, Object> (inParamMap);
				
				List<String> apiURLs = (List<String>) mapping.getOrDefault(EnumParam2recomder.api_url.name(), new ArrayList<String>());
				List<String> auxFields = (List<String>) mapping.getOrDefault(EnumParam2recomder.out_aux_params.name(), new ArrayList<String>());
				
				if (auxFields.isEmpty()) VEN_LOGGER.info("none of register key: {}", EnumParam2recomder.out_aux_params.name());
				
				//-- forward attaching fields for back-end usage
				// traffic info
				outParamMap.put(RoutingGroup.GROUP_KEY, targetGrp.getGroup_key());
				outParamMap.put(RoutingGroup.TRAFFIC_TYPE, targetGrp.getTraffic_type());
				outParamMap.put(RoutingGroup.TRAFFIC_PCT, targetGrp.getTraffic_pct());
				//  auxiliary key/value which are specified by registration mapping. 
				for (String f : auxFields) {
					String v = (String) mapping.get(f);
					outParamMap.put(f, v);
				}				

				String apiURL = "";				
				Gson g = new Gson();
				String outParam = g.toJson(outParamMap);
				
				if (1 == apiURLs.size()) {
					apiURL = apiURLs.get(0);
				} else if (2 <= apiURLs.size()) {
					int r = ThreadLocalRandom.current().nextInt(apiURLs.size());
					apiURL = apiURLs.get(r);
				} else {
					VEN_LOGGER.error("invalid register key/value: {} / {}", EnumParam2recomder.api_url.name(), apiURL);
				}
				
				if (! apiURL.isEmpty()) {
					ConfManager confMgr = ConfManager.getInstance();
					List<String> headers = confMgr.get_http_forward_headers(codeName);
					
					APIConnector apiConn = APIConnector.getInstance();
					resp = apiConn.post(apiURL, outParam, req, headers);
				}
			}
			else {
				Gson g = new Gson();
				VEN_LOGGER.warn("input parameter to recomder mapping cannot be found. input: {}", g.toJson(inParamMap));			
			}
		} catch(Exception ex) {
			VEN_LOGGER.error("{} with input {} ", ex.getMessage(), new Gson().toJson(inParamMap));
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
		}

		Gson g = new Gson();
		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		Map<String, Object> m = g.fromJson(resp, type);
		return m;	
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
	
}
