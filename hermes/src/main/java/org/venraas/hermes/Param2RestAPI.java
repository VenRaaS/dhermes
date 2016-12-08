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
			
			JsonObject rootJO = g.fromJson(regJson, JsonObject.class);			

			// "group_key"
			JsonElement group_key = rootJO.get(EnumParam2recomder.group_key.name());
			if (null == group_key && ! trafficType.equals(Constant.TRAFFIC_TYPE_NORMAL) ) {
				msg = String.format("Invalid input, \"%s\" is unavailable or empty in the input Json!", EnumParam2recomder.group_key.name());
				throw new IllegalArgumentException(msg);				
			}
			
			// "in_keys2recomder"
			JsonArray inKeys = rootJO.getAsJsonArray(EnumParam2recomder.in_keys2recomder.name());
			for (JsonElement inKey : inKeys) {
				String k = inKey.getAsString();
				if (null == rootJO.get(k)) {
					msg = String.format("Invalid input, key \"%s\" in \"%s\" is unavailable or empty in the input Json!", k, EnumParam2recomder.in_keys2recomder.name());
					throw new IllegalArgumentException(msg);
				}
			}
			
			// "out_aux_params"
			Map<String, JsonElement> testingParam = new HashMap<String, JsonElement>();
			JsonArray outAuxParams = rootJO.get(EnumParam2recomder.out_aux_params.name()).getAsJsonArray();
			for (JsonElement op : outAuxParams) {
				String k = op.getAsString();
				JsonElement e = rootJO.get(k);
				if (null == e) {
					msg = String.format("Invalid input, key \"%s\" in \"%s\" is unavailable or empty in the input Json!", k, EnumParam2recomder.out_aux_params.name());
					throw new IllegalArgumentException(msg);
				} 

				testingParam.put(k, e);
			}

			// "valid api_url"
			JsonArray api_urls = rootJO.getAsJsonArray(EnumParam2recomder.api_url.name());
			APIConnector conn = APIConnector.getInstance();
			for (JsonElement url : api_urls) {				
				String body = g.toJson(testingParam);
				String url_str = url.getAsString();
				if (! conn.isValidURL(url_str, body)) {
					msg = String.format("Invalid input, \"%s\" can't be connected with Json parameter: %s!", url, body);
					throw new IllegalArgumentException(msg);
				}
			}
			
			// add "availability", "group_key", "traffic_type", udpate_dt
			rootJO.addProperty(EnumParam2recomder.availability.name(), 1);
			if ( trafficType.equals(Constant.TRAFFIC_TYPE_NORMAL) ) {
				rootJO.addProperty(EnumParam2recomder.group_key.name(), Constant.NORMAL_GROUP_KEY);
			}
			rootJO.addProperty(EnumParam2recomder.traffic_type.name(), trafficType);			
			rootJO.addProperty(EnumParam2recomder.update_dt.name(), Utility.now());
			
			Param2recomderManager p2rMgr = Param2recomderManager.getInstance();
			String docID = p2rMgr.registerMapping(codeName, g.toJson(rootJO));	
				
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
