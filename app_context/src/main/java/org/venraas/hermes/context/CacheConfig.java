package org.venraas.hermes.context;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.venraas.hermes.common.Utility;


@Configuration
@ComponentScan("org.venraas.hermes.apollo.*")
@EnableCaching
public class CacheConfig {

	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(CacheConfig.class);

	@Bean(name="cacheMgr")
	public CacheManager cacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		
		List<GuavaCache> cache_list = new ArrayList<GuavaCache>();
		
		try
		{
/*///			
			GuavaCache cache_company = 
					new GuavaCache(
							"cache_company", 
							CacheBuilder.newBuilder()
							.maximumSize(Constant.CACHE_SIZE_10K)
							.expireAfterWrite(Constant.CACHE_EXPIRE_AFTER_30_TIMEUNIT, TimeUnit.MINUTES)
							.build());
			cache_list.add(cache_company);		
			
			GuavaCache cache_conf = 
				new GuavaCache(
						"cache_conf", 
						CacheBuilder.newBuilder()
						.maximumSize(Constant.CACHE_SIZE_10K)
						.expireAfterWrite(Constant.CACHE_EXPIRE_AFTER_30_TIMEUNIT, TimeUnit.MINUTES)
						.build());
			cache_list.add(cache_conf);

			
			GuavaCache cache_param2recomder = 
					new GuavaCache(
							"cache_param2recomder", 
							CacheBuilder.newBuilder()
							.maximumSize(Constant.CACHE_SIZE_10K)
							.expireAfterWrite(Constant.CACHE_EXPIRE_AFTER_30_TIMEUNIT, TimeUnit.MINUTES)
							.build());
			cache_list.add(cache_param2recomder);		
			
			GuavaCache cache_jumper = 
					new GuavaCache(
							"cache_jumper", 
							CacheBuilder.newBuilder()
							.maximumSize(Constant.CACHE_SIZE_10K)
							.expireAfterWrite(Constant.CACHE_EXPIRE_AFTER_10_TIMEUNIT, TimeUnit.SECONDS)
							.build());
			cache_list.add(cache_jumper);			
*/
			

						
			cacheManager.setCaches(cache_list);
		}
		catch (Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.equals(ex.getMessage());
		}
		
		return cacheManager;

	}
}
