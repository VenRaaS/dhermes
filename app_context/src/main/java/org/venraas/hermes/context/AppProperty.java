package org.venraas.hermes.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.venraas.hermes.common.Utility;

@Configuration
@PropertySource("classpath:application.properties")
public class AppProperty {

	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(AppProperty.class);
	
	@Autowired
    Environment env;
	
	/**
	 * The Singleton instance creates as application startup automatically.
	 *   
	 * @return Config the singleton instance
	 */
	@Bean
	public Config getConfig() {
		
		Config cfg = Config.getInstance();
		VEN_LOGGER.info("loading configuration - application.properties");
	
		try {
			cfg.setConn_timeout(Integer.valueOf(env.getProperty("conn.timeout_ms")));
			cfg.setConn_fail_cond_interval(Integer.valueOf(env.getProperty("conn.fail.cond.interval_sec")));
			cfg.setConn_fail_cond_count(Integer.valueOf(env.getProperty("conn.fail.cond.count")));
			cfg.setConn_failover_url(env.getProperty("conn.failover.url"));	
			cfg.setConn_fail_resume_interval(Integer.valueOf(env.getProperty("conn.fail.resume.interval_sec")));
		}
		catch (Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
		}
		
		return cfg;		
	}
	
	
}
