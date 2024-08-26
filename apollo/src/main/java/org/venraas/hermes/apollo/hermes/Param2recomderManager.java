package org.venraas.hermes.apollo.hermes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


public class Param2recomderManager {	
	
	static LoadingCache<String, Object> _cache_param2recomder;
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(Param2recomderManager.class);
	
	static {
		//-- Guava cache - https://github.com/google/guava/wiki/CachesExplained#refresh
		_cache_param2recomder = CacheBuilder.newBuilder()
				.maximumSize(Constant.CACHE_SIZE_10K)						
				.refreshAfterWrite(Constant.NUM_TIMEUNIT_10, TimeUnit.MINUTES)
				.build(
					new CacheLoader<String, Object>() {						
						public Object load(String key) throws Exception {
							Object rt = null;
							
							int qi = key.indexOf(Constant.FUNCTION_DELIMITER);
							String funName = key.substring(0, qi);
							String[] ps = key.substring(qi+1).split(String.valueOf(Constant.PARAM_DELIMITER));
							
							Param2recomderClient client = new Param2recomderClient();
							switch (funName) {
								case "getDistinctGroups":
									rt = client.getDistinctGroups(ps[0]);
									break;
								case "getGroupMapping":
									rt = client.getGroupMapping(ps[0], ps[1]);
									break;							
							}
							
							return rt;
						}
						
						public ListenableFuture<Object> reload (final String key, Object oldVal) {
							//-- async call to get the value from source
							ListenableFuture<Object> task = 
								Utility.CacheRefreshLES.submit(
									new Callable<Object>() {
										public Object call() throws Exception {
											return load(key);
										}
									});
							
			                return task;
						}
					}
				);
	}
	
	public Param2recomderManager() {}
	
	public List<String> getDistinctGroups(String codeName) {
		return getDistinctGroups(codeName, false);
	}
	
	public List<String> getDistinctGroups (String codeName, boolean forceRefresh) {
		List<String> grps = new ArrayList<String>();
		
		try
		{
			String k = String.format("getDistinctGroups?%s", codeName);			
			if (forceRefresh) {
				_cache_param2recomder.refresh(k);
			}
			grps = (List<String>) _cache_param2recomder.get(k);
		}
		catch (Exception ex) {
			VEN_LOGGER.error(ex.getMessage());
			VEN_LOGGER.error(Utility.stackTrace2string(ex));	
		}
		
		return grps;
	}
	
	public List<Map<String, Object>> getGroupMapping (String codeName, String grpKey) {
		List<Map<String, Object>> grpMaps = new ArrayList<Map<String, Object>> ();
		
		try
		{
			String k = String.format("getGroupMapping?%s&%s", codeName, grpKey);
			grpMaps = (List<Map<String, Object>>) _cache_param2recomder.get(k);
		}
		catch (Exception ex) {
			VEN_LOGGER.error(ex.getMessage());
			VEN_LOGGER.error(Utility.stackTrace2string(ex));	
		}
		
		return grpMaps;
	}
	
///	public List<Map<String, String>> getGroupMapping_keys2recomder (String codeName, String grpKey) {
///		return _client.getGroupMapping_inKeys2recomder(codeName, grpKey);
///	}
	
	public Map<String, Map<String, List<Object>>> ls_grp (String codeName) {
		
		Map<String, Map<String, List<Object>>> m = new HashMap<String, Map<String, List<Object>>>(); 
		
		try {
			Param2recomderClient client = new Param2recomderClient();
			m = client.getAllMappings(codeName);
		}
		catch (Exception ex) {
			VEN_LOGGER.error(ex.getMessage());
			VEN_LOGGER.error(Utility.stackTrace2string(ex));	
		}
		
		return m;
	}
	
//	public String registerMapping (String codeName, String mappingJson) {
//		Param2recomderClient client = new Param2recomderClient();
//		return client.indexMapping(codeName, mappingJson);
//	}
	
//	public List<String> rm_group (String codeName, String grpKey) {
//		Param2recomderClient client = new Param2recomderClient();
//		return client.rm_group(codeName, grpKey);
//	}
	
//	public String rm_mapping (String codeName, String mid) {
//		Param2recomderClient client = new Param2recomderClient();
//		return client.rm_mapping(codeName, mid);
//	}
	
	
}
