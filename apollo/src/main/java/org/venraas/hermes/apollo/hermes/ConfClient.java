package org.venraas.hermes.apollo.hermes;

import java.util.Calendar;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.apollo.Apollo;
import org.venraas.hermes.apollo.mappings.EnumConf;
import org.venraas.hermes.common.Utility;
import org.venraas.hermes.common.Constant;
import org.venraas.hermes.data_entity.Conf;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class ConfClient {	
	
	static private final String TYPE_NAME = "conf"; 
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(ConfClient.class);	
	
	private Apollo _apo = null;	

	
	public ConfClient() {
		if (null == _apo) {
			_apo = Apollo.getInstance();
		}		
	}
	
//	@CacheEvict(value="cache_conf", allEntries=true)
	public void reset() {
	}
	

//	@Cacheable(value="cache_conf", key="{#token}")
	public int get_routing_reset_interval(String codeName) {
		
		VEN_LOGGER.info("caching get_routing_reset_interval({})", codeName);
				
		int interval = Calendar.HOUR_OF_DAY;
		
		if(null == codeName || codeName.isEmpty()) return interval;
		
		String indexName = String.format("%s_hermes", codeName);
		
		try {
			QueryBuilder qb = QueryBuilders.matchAllQuery();
			
	        SearchResponse resp = _apo.esClient().prepareSearch(indexName)
	                .setTypes(TYPE_NAME)
	                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	                .setQuery(qb)	                	                
	                .addSort(EnumConf.update_dt.name(), SortOrder.DESC)
	                .setSize(1)
	                .execute()
	                .actionGet();
	        	        
	        if (0 < resp.getHits().getTotalHits()) {
	        	SearchHit h = resp.getHits().getAt(0);	        		        	
	        	String jsonStr = h.getSourceAsString();
	        	
	        	JsonParser jsonParser = new JsonParser();
	        	JsonObject o = jsonParser.parse(jsonStr).getAsJsonObject();
	        	
	        	Gson g = new Gson();	        	    	        		
	        	Conf con = g.fromJson(o, Conf.class);
	        	String val = con.getRouting_reset_interval();
	        	
	        	switch (val) {
	        		case "SECOND":
	        			interval = Calendar.SECOND;	        			
	        			break;
	        		case "MINUTE":
	        			interval = Calendar.MINUTE;	        			
	        			break;
	        		case "DAY":
	        			interval = Calendar.DAY_OF_MONTH;	        			
	        			break;
	        		case "WEEK":
	        			interval = Calendar.WEEK_OF_MONTH;
	        			break;
	        		case "MONTH":
	        			interval = Calendar.MONTH;
	        			break;
	        		case "HOUR":
	        		default:
	        			interval = Calendar.HOUR_OF_DAY;
	        			break;
	        	}
	        }
	        
		} catch(Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		} 
        
        return interval;        
	}
	
//	@Cacheable(value="cache_conf", key="{#token}")	
	public double get_traffic_percent_normal(String codeName) {
		
		VEN_LOGGER.info("caching get_traffic_percent_normal({})", codeName);
		
		double pct = Constant.TRAFFIC_PERCENT_NORMAL;
	
		if(null == codeName || codeName.isEmpty()) return pct;
		
		String indexName = String.format("%s_hermes", codeName);
		
		try {
			QueryBuilder qb = QueryBuilders.matchAllQuery();
			
	        SearchResponse resp = _apo.esClient().prepareSearch(indexName)
	                .setTypes(TYPE_NAME)
	                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	                .setQuery(qb)	                	                
	                .addSort(EnumConf.update_dt.name(), SortOrder.DESC)
	                .setSize(1)
	                .execute()
	                .actionGet();
	        	        
	        if (0 < resp.getHits().getTotalHits()) {
	        	SearchHit h = resp.getHits().getAt(0);	        		        	
	        	String jsonStr = h.getSourceAsString();
	        	
	        	JsonParser jsonParser = new JsonParser();
	        	JsonObject o = jsonParser.parse(jsonStr).getAsJsonObject();
	        	
	        	Gson g = new Gson();	        	    	        		
	        	Conf con = g.fromJson(o, Conf.class);
	        	pct = con.getTraffic_pct_normal();	        
	        }
	        
		} catch(Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		} 
        
        return pct;        
	}
}
