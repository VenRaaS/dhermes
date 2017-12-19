package org.venraas.hermes;

import java.lang.reflect.Type;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
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
import org.venraas.hermes.common.OptionUtility;
import org.venraas.hermes.common.Utility;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.twitter.jsr166e.ThreadLocalRandom;


@RestController
@RequestMapping("/api")
public class HermesController {
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(HermesController.class);
	
	@CrossOrigin
	@RequestMapping(value = "/{subject:\\w+}/{action:\\w+}", method = RequestMethod.GET)
	public Object get_goods_rank_GET(
			@PathVariable("subject") String subject, 
			@PathVariable("action") String action, 
			@RequestParam Map<String, Object> paramMap,
			HttpServletRequest req) 
	{
		Gson gson = new Gson();
		VEN_LOGGER.info(gson.toJson(paramMap));
		
		return get_goods_rank(paramMap, req);
	}
	
	@CrossOrigin
	@RequestMapping(value = "/{subject:\\w+}/{action:\\w+}", method = RequestMethod.POST)
	public Object get_goods_rank_POST(@RequestBody String jsonStr, HttpServletRequest req) {
		VEN_LOGGER.info(jsonStr);
		
		Gson gson = new Gson();
		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		Map<String, Object> paramMap = gson.fromJson(jsonStr, type);
		
		return get_goods_rank(paramMap, req);
	}	
	
	private Map<String, Object> get_goods_rank(Map<String, Object> inParamMap, HttpServletRequest req) {
		Map<String, Object> errMsg = null;
		String resp = "";
		
		OptionUtility opt = new OptionUtility();
		inParamMap = opt.cp_cc2ven(inParamMap);
		
		String clientID = String.format("%s_%s_%s", 
				inParamMap.get(EnumOptionBase.token.name()), 
				inParamMap.get(EnumOptionBase.ven_guid.name()), 
				inParamMap.get(EnumOptionBase.ven_session.name()));
		
		try {
			String token = (String)inParamMap.get(EnumOptionBase.token.name());
			CompanyManager comMgr = new CompanyManager();
			String codeName = comMgr.getCodeName(token);			
			if (codeName.isEmpty()) {
				throw new InvalidParameterException(String.format("an invalid token: '%s', or mapping isn't undefined yet!", token));
			}
			
			String uid = (String) inParamMap.getOrDefault(EnumOptionBase.uid.name(), "");
			if (null == uid || uid.isEmpty()) {
				uid = (String) inParamMap.getOrDefault(EnumOptionBase.ven_guid.name(), "");
			}
		
			//-- routing to target group according to $clientID
			GroupRoute gr = new GroupRoute();
			RoutingGroup targetGrp = gr.routing(codeName, clientID, uid);
			
			Param2RestAPI p2r = new Param2RestAPI(codeName, targetGrp.getGroup_key());
			Map<String, Object> mapping = p2r.getMapping(inParamMap);
			
			//-- normal channel
			Param2RestAPI n_p2r = new Param2RestAPI(codeName, Constant.NORMAL_GROUP_KEY);
			Map<String, Object> n_mapping = n_p2r.getMapping(inParamMap);
			List<String> n_apiURLs = (List<String>) n_mapping.getOrDefault(EnumParam2recomder.api_url.name(), new ArrayList<String>());
			String n_apiURL = (n_apiURLs.isEmpty()) ? "" : n_apiURLs.get(0);
			
			//-- redirect to Normal (default) Group, if input parameter doesn't match
			if (! Constant.NORMAL_GROUP_KEY.equalsIgnoreCase(targetGrp.group_key) && mapping.isEmpty()) {
				VEN_LOGGER.info("redirect to Normal Group due to there's no mapping match input parameter in {}", targetGrp.getGroup_key());				
				p2r = n_p2r;
				mapping = n_mapping;
			}
			
			if (mapping.isEmpty()) {
				throw new InvalidParameterException("input parameter to recommender mapping cannot be found!");
			}
			
			List<String> apiURLs = (List<String>) mapping.getOrDefault(EnumParam2recomder.api_url.name(), new ArrayList<String>());
			List<String> auxFields = (List<String>) mapping.getOrDefault(EnumParam2recomder.out_aux_params.name(), new ArrayList<String>());
			
			if (auxFields.isEmpty()) VEN_LOGGER.info("none of register key: {}", EnumParam2recomder.out_aux_params.name());
			
			//-- forward attaching fields for back-end usage
			// traffic info
			HashMap<String, Object> outParamMap = new HashMap<String, Object> (inParamMap);
			outParamMap.put(RoutingGroup.GROUP_KEY, targetGrp.getGroup_key());
			outParamMap.put(RoutingGroup.TRAFFIC_TYPE, targetGrp.getTraffic_type().toString());
			outParamMap.put(RoutingGroup.TRAFFIC_PCT, Float.parseFloat(targetGrp.getTraffic_pct()));
			//  auxiliary key/value which are specified by registration mapping. 
			for (String f : auxFields) {
				Object v = mapping.get(f);
				outParamMap.put(f, v);
			}
			
			VEN_LOGGER.info("clientID: {} => {}", clientID, targetGrp.toString()); 

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
				ConfManager confMgr = new ConfManager();
				List<String> headers = confMgr.get_http_forward_headers(codeName);
				
				APIConnector apiConn = new APIConnector();										
				resp = apiConn.post(targetGrp.getTraffic_type(), apiURL, n_apiURL, outParam, req, headers);
			}
		} catch(Exception ex) {
			String err = String.format("%s", ex.getMessage());
			errMsg = new HashMap<String, Object>();
			errMsg.put("input", inParamMap);
			errMsg.put("error", err);
			
			VEN_LOGGER.error(err);
			VEN_LOGGER.error(Utility.stackTrace2string(ex));			
		}

		Map<String, Object> respMap = null;
		if (null == errMsg) {
			Gson g = new Gson();
			Type type = new TypeToken<Map<String, Object>>(){}.getType();
			respMap = g.fromJson(resp, type);
		}
		else {
			respMap = errMsg;
		}
		
		return respMap;	
	}	
	
	@CrossOrigin
	@RequestMapping(value = "/status", method = RequestMethod.GET)
	public String status()
	{
		return "\"Good\"";
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
