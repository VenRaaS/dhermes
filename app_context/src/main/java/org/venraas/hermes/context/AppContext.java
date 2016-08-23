package org.venraas.hermes.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.venraas.hermes.common.Utility;

public class AppContext {
	
	private static AnnotationConfigApplicationContext _cache_annot_ctx = null;
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(AppContext.class);

	
	private AppContext() { }	

	public static AnnotationConfigApplicationContext getCacheAnnotContext() {
		
		if (null == _cache_annot_ctx) {
			
			synchronized (AppContext.class) {
				
				if (null == _cache_annot_ctx) {
					
					try {
						_cache_annot_ctx = new AnnotationConfigApplicationContext();
						_cache_annot_ctx.register(CacheConfig.class);
						_cache_annot_ctx.refresh();
					} 
					catch (Exception ex){
						VEN_LOGGER.error(Utility.stackTrace2string(ex));
						VEN_LOGGER.error(ex.getMessage());
					}					
				}
			}
		}
		
		return _cache_annot_ctx;
	}
	
}
