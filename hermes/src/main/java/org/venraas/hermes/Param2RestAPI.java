package org.venraas.hermes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.apollo.hermes.Param2recomderClient;
import org.venraas.hermes.apollo.hermes.Param2recomderManager;
import org.venraas.hermes.apollo.mappings.EnumParam2recomder;

public class Param2RestAPI {
	String _codeName;
	String _grpKey;	

	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(Param2RestAPI.class);	
	
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
			
			List<String> regFields = (List<String>) m.get(EnumParam2recomder.in_keys2recomder.name());
			
			for (String regF : regFields) {
				String inputV = (String) inParamMap.get(regF);
				String regV = (String) m.get(regF);
				
				if (null == inputV || null == regV || 
					inputV.isEmpty() || regV.isEmpty() || 
					!inputV.equals(regV)) 
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
	
	
}
