package org.venraas.hermes.common;

public class Constant {
	
	public static final String HERMES_INDEX_SUFFIX = "_hermes";
	
	public static final String HERMES_CONF_CACHE_TRAFFIC_PCT = "traffic_pct";
	
	public static final String HERMES_CONF_CACHE_ROUTING_RESET_INTERVAL = "routing_reset_interval";
	
	//-- 100%
	public static final double TRAFFIC_PERCENT_NORMAL = 1.0;
	
	public static final String TRAFFIC_TYPE_NORMAL = "normal";
	
	public static final String TRAFFIC_TYPE_TEST = "test";
	
	public static final int MAX_NUM_GROUPS = 100;
	
	public static final String NORMAL_GROUP_KEY = "normal";
	
	public static final int HTTP_REQUEST_TIMEOUT = 2 * 1000;
	
	
	public final static long CACHE_SIZE_100K = 100 * 1000;
	
	public final static long CACHE_EXPIRE_AFTER_30_MINS = 30;
	
	public final static long CACHE_EXPIRE_AFTER_60_MINS = 60;
	
	
	public final static int CONNECTION_POOL_MAX_TOTAL = 1000;
	
	
}
