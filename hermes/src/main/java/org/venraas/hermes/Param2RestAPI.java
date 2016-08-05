package org.venraas.hermes;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.apollo.hermes.Param2recomderClient;
import org.venraas.hermes.apollo.mappings.EnumParam2recomder;

public class Param2RestAPI {
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(Param2RestAPI.class);	
	
	private Param2RestAPI() { }

	public Param2RestAPI getBuilder() {
		return new Param2RestAPI();
	}
	
	public Param2RestAPI set_codeName(String codeName) {	
//TODO...		
		return this;
	}
	
	public Map<String, Object> getMapping(String codeName, String grpKey, Map<String, Object> inParamMap) {
		VEN_LOGGER.info("codeName: {}, grpKey: {}", codeName, grpKey);
		
		Map<String, Object> mapping = null;

		Param2recomderClient p2r = new Param2recomderClient();
		List<Map<String, Object>> maps = p2r.getGroupMapping(codeName, grpKey);
		
		for (Map<String, Object> m : maps) {
			boolean matchAllKeys = true;
			
			List<String> fields = (List<String>) m.get(EnumParam2recomder.keys2recomder.name());						
			for (String f : fields) {
				String inputV = (String) inParamMap.get(f);
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
		
		return mapping;
	}
	
}
