package org.venraas.hermes.apollo.hermes;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.venraas.hermes.context.AppContext;

public class Param2recomderManager {

	private static Param2recomderClient _client = null;
	private static Param2recomderManager _mgr = new Param2recomderManager();
	
	private Param2recomderManager() {}
	
	static public Param2recomderManager getInstance() {
		
		if (null == _client) {
			
			synchronized (Param2recomderManager.class) {
				
				if (null == _client) {
					AnnotationConfigApplicationContext ctx = AppContext.getCacheAnnotContext();
					_client = ctx.getBean(Param2recomderClient.class);
				}
			}
		}
		
		return _mgr;
	}
	
	public void reset() {
		_client.reset();
	}
	
	public List<String> getDistinctGroups (String codeName) {
		return _client.getDistinctGroups(codeName);
	}
	
	public List<Map<String, Object>> getGroupMapping (String codeName, String grpKey) {
		return _client.getGroupMapping(codeName, grpKey);
	}
		
	
	
}
