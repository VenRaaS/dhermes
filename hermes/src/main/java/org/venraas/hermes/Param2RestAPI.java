package org.venraas.hermes;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.apollo.hermes.Param2recomderManager;
import org.venraas.hermes.apollo.mappings.EnumParam2recomder;
import org.venraas.hermes.common.Constant;
import org.venraas.hermes.common.Utility;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

public class Param2RestAPI {
	String _codeName;
	String _grpKey;	

	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(Param2RestAPI.class);	

	public Param2RestAPI() {}
	
	public Param2RestAPI(String codeName, String grpKey) {
		this._codeName = codeName;
		this._grpKey = grpKey;				
	}
				
	public Map<String, Object> getMapping(Map<String, Object> inParamMap) {
		
		VEN_LOGGER.info("codeName: {}, grpKey: {}", _codeName, _grpKey);
		
		Map<String, Object> rsMap = new HashMap<String, Object>();
		
		Param2recomderManager p2rMgr = Param2recomderManager.getInstance();
		
		//-- list all available mappings with respect to $_codeName and $_grpKey
		List<Map<String, Object>> regMaps = p2rMgr.getGroupMapping(_codeName, _grpKey);
		
		//-- looking for the first Mapping which satisfies the $inParamMap 
		for (Map<String, Object> m : regMaps) {
			boolean matchAllKeys = true;
			
			List<String> regFields = (List<String>) m.getOrDefault(EnumParam2recomder.in_keys2recomder.name(), new ArrayList<String>());
			
			for (String regF : regFields) {
				String inputV = (String) inParamMap.getOrDefault(regF, "");
				String regV = (String) m.getOrDefault(regF, "");
				
				if (inputV.isEmpty() || regV.isEmpty() || !inputV.equals(regV)) 
				{
					matchAllKeys = false;
					break;
				}
			}
			
			if (matchAllKeys) {
				rsMap = m;
				break;
			}
		}
		
		return rsMap;
	}
	
	public String regsiterMapping(String codeName, String trafficType, String regJson) {
		
		String msg = "";
		
		try {
			//-- input validation
			Gson g = new Gson();			
			Type type = new TypeToken<Map<String, Object>>(){}.getType();
			Map<String, Object> regMap = g.fromJson(regJson, type);
			
			JsonObject rootJO = g.fromJson(regJson, JsonObject.class);			

			// "group_key"
			String group_key = (String) regMap.getOrDefault(EnumParam2recomder.group_key.name(), "");
			if ( group_key.isEmpty() && ! trafficType.equals(Constant.TRAFFIC_TYPE_NORMAL) ) {
				msg = String.format("Invalid input, \"%s\" is unavailable or empty in the input Json!", EnumParam2recomder.group_key.name());
				throw new IllegalArgumentException(msg);				
			}
			
			// "in_keys2recomder"
			List<String> inKeys = (List<String>) regMap.getOrDefault(EnumParam2recomder.in_keys2recomder.name(), new ArrayList<String>());
			for (String k : inKeys){
				String val = String.valueOf(regMap.getOrDefault(k, ""));
				if (val.isEmpty()) {
					msg = String.format("Invalid input, key \"%s\" in \"%s\" is unavailable or empty in the input Json!", k, EnumParam2recomder.in_keys2recomder.name());
					throw new IllegalArgumentException(msg);
				}
			}
			
			// "out_aux_params"
			Map<String, String> testingParam = new HashMap<String, String>();
			JsonArray outAuxParams = rootJO.get(EnumParam2recomder.out_aux_params.name()).getAsJsonArray();
			for (JsonElement p : outAuxParams) {
				String k = p.getAsString();
				JsonElement e = rootJO.get(k);
				if (null == e) {
					msg = String.format("Invalid input, key \"%s\" in \"%s\" is unavailable or empty in the input Json!", k, EnumParam2recomder.out_aux_params.name());
					throw new IllegalArgumentException(msg);
				} 
				else {
					String v = g.toJson(e);
					testingParam.put(k, v);
				}
				String s = g.toJson(e);
				testingParam.put(k, s);				
			}
			
/*///
			List<String> outAuxParams = (List<String>) regMap.getOrDefault(EnumParam2recomder.out_aux_params.name(), new ArrayList<String>());
			for (String k : outAuxParams){
				String val = (String) regMap.getOrDefault(k, "");
				if (val.isEmpty()) {
					msg = String.format("Invalid input, key \"%s\" in \"%s\" is unavailable or empty in the input Json!", k, EnumParam2recomder.out_aux_params.name());
					throw new IllegalArgumentException(msg);
				} else {
					testingParam.put(k, val);
				}
			}
*/			
			
			// "valid api_url"
			List<String> api_urls = (List<String>) regMap.getOrDefault(EnumParam2recomder.api_url.name(), new ArrayList<String>());
			APIConnector conn = APIConnector.getInstance();
			for (String url : api_urls) {
				Gson gg =  new Gson();
				String body = gg.toJson(testingParam);				
				if (! conn.isValidURL(url, body)) {
					msg = String.format("Invalid input, \"%s\" can't be connected with Json parameter: %s!", url, body);
					throw new IllegalArgumentException(msg);
				}
			}
									
			// add "availability", "group_key", "traffic_type", udpate_dt
			regMap.put(EnumParam2recomder.availability.name(), 1);
			if ( trafficType.equals(Constant.TRAFFIC_TYPE_NORMAL) ) {
				regMap.put(EnumParam2recomder.group_key.name(), Constant.NORMAL_GROUP_KEY);
				regMap.put(EnumParam2recomder.traffic_type.name(), trafficType);
			}
			else {
				regMap.put(EnumParam2recomder.traffic_type.name(), trafficType);
			}
			regMap.put(EnumParam2recomder.update_dt.name(), Utility.now());
			
			g = new Gson();			
			Param2recomderManager p2rMgr = Param2recomderManager.getInstance();			
			String docID = p2rMgr.registerMapping(codeName, g.toJson(regMap));			
			if (docID.isEmpty()) throw new RuntimeException("error, input mapping doesn't register to normal channel!");
			
			msg = String.format("ok, the input been registered to %s channel with ID: %s", trafficType, docID); 				  
		}
		catch (Exception ex) {
			msg = ex.getMessage();
			VEN_LOGGER.error(msg);
		}
		
		return msg;
		
	}
	
	
}
