package org.venraas.hermes.apollo.hermes;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.venraas.hermes.context.AppContext;

public class JumperManager {
	
	static JumperClient _client = null;
	static JumperManager _mgr = new JumperManager();	
	
	static public JumperManager getInstance() {

		if (null == _client) {
			
			synchronized (JumperManager.class) {
				
				if (null == _client) {
					AnnotationConfigApplicationContext ctx = AppContext.getCacheAnnotContext();
					_client = ctx.getBean(JumperClient.class);
				}
			}
		}
		
		return _mgr;
	}
	
	public void reset() {
		_client.reset();
	}
	
	public String get_group_key(String codeName, String uid) {
		return _client.get_group_key(codeName, uid);		
	}
	
	public boolean set_jumper(String codeName, String uid, String grpKey) {
		return _client.set_jumper(codeName, uid, grpKey);
	}
	

}
