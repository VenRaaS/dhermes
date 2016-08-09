package org.venraas.hermes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.apollo.hermes.Param2recomderClient;
import org.venraas.hermes.apollo.mappings.EnumParam2recomder;

public class Param2RestAPI {
	String _codeName;
	String _grpKey;
	Map<String, Object> _inParamMap;
	Map<String, Object> _rsMap = new HashMap<String, Object>();
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(Param2RestAPI.class);	
	
	private Param2RestAPI() { }

	public static Param2RestAPI getBuilder() {
		return new Param2RestAPI();
	}

	public Param2RestAPI set_codeName(String codeName) {
		this._codeName = codeName;
		return this;
	}
	
	public Param2RestAPI set_grpKey(String grpKey) {
		this._grpKey = grpKey;
		return this;
	}
	
	public Param2RestAPI set_inParamMap(Map<String, Object> inParamMap) {
		this._inParamMap = inParamMap;
		return this;
	}
	
	public Param2RestAPI build() {
		
		VEN_LOGGER.info("codeName: {}, grpKey: {}", _codeName, _grpKey);
		
		Map<String, Object> _rsMap = null;

		Param2recomderClient p2r = new Param2recomderClient();
		
		//-- list available mappings with specified GroupKey
		List<Map<String, Object>> maps = p2r.getGroupMapping(_codeName, _grpKey);
		
		for (Map<String, Object> m : maps) {
			boolean matchAllKeys = true;
			
			List<String> fields = (List<String>) m.get(EnumParam2recomder.in_keys2recomder.name());						
			for (String f : fields) {
				String inputV = (String) _inParamMap.get(f);
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
				_rsMap = m;
				break;
			}
		}
		
		return this;
	}

	public Map<String, Object> getMapping() {
		return _rsMap;
	}
	
	
}
