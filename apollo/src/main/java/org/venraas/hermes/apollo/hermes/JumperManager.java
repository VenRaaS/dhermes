package org.venraas.hermes.apollo.hermes;


import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.common.Constant;
import org.venraas.hermes.common.Utility;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;


public class JumperManager {
	
	static JumperClient _client = new JumperClient();
	static JumperManager _mgr = new JumperManager();		
	static LoadingCache<String, String> _cache_jumper; 
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(JumperManager.class);	
	
	static {
		//-- Guava cache - https://github.com/google/guava/wiki/CachesExplained#refresh
		_cache_jumper = CacheBuilder.newBuilder()
				.maximumSize(Constant.CACHE_SIZE_10K)						
				.refreshAfterWrite(Constant.NUM_TIMEUNIT_30, TimeUnit.SECONDS)
				.build(
					new CacheLoader<String, String>() {
						public String load(String key) throws Exception {
							String rt = null;
							
							int qi = key.indexOf(Constant.FUNCTION_DELIMITER);
							String funName = key.substring(0, qi);
							String[] ps = key.substring(qi+1).split(String.valueOf(Constant.PARAM_DELIMITER));
							
							switch (funName) {
								case "get_group_key":
									rt = _client.get_group_key(ps[0], ps[1]);
									break;							
							}
							
							return rt;
						}
						
						public ListenableFuture<String> reload (final String key, String oldVal) {
							//-- async call to get the value from source
							ListenableFuture<String> task = 
								Utility.CacheRefreshLES.submit(
									new Callable<String>() {
										public String call() throws Exception {
											return load(key);
										}
									});
							
			                return task;
						}
					}
				);
	}
	
	static public JumperManager getInstance() {
		return _mgr;
	}
	
	public String get_group_key(String codeName, String uid) {
		String grpKey = "";
		
		try
		{
			String k = _cacheKey_get_group_key(codeName, uid);
			grpKey = _cache_jumper.get(k);
		}
		catch (Exception ex) {
			VEN_LOGGER.error(ex.getMessage());
			VEN_LOGGER.error(Utility.stackTrace2string(ex));	
		}
		
		return grpKey;
	}
	
	public boolean set_jumper(String codeName, String uid, String grpKey) {
		boolean rt = _client.set_jumper(codeName, uid, grpKey);
		
		//-- refresh cache
		String k = _cacheKey_get_group_key(codeName, uid);
		_cache_jumper.refresh(k);
		
		return rt;
	}
	
	private String _cacheKey_get_group_key(String codeName, String uid) {
		String k = String.format("get_group_key?%s&%s", codeName, uid);
		return k;
	}
	

}
