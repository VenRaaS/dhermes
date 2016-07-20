package org.venraas.hermes.apollo.raas;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class CompanyManager {
	
	static CompanyClient _client = null;
	static CompanyManager _mgr = new CompanyManager();
	
	
	private CompanyManager() {}
	
	static public CompanyManager getInstance() {
		
		if (null == _client) {
			
			synchronized (CompanyManager.class) {
				
				if (null == _client) {
					
//TODO...					AnnotationConfigApplicationContext ctx = AppContext.getCacheAnnotContext();
//TODO...					_client = ctx.getBean(CompanyClient.class);
				}
			}
		}
		
		return _mgr;
	}
	
	public void reset() {
		_client.reset();
	}
	
	public String getCodeName(String token) {
		String codeName = _client.getCodeName(token);		
		return codeName;
	}

}
