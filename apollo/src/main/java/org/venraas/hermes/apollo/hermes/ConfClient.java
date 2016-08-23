package org.venraas.hermes.apollo.hermes;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.venraas.hermes.apollo.Apollo;
import org.venraas.hermes.apollo.mappings.EnumConf;
import org.venraas.hermes.common.Utility;
import org.venraas.hermes.common.Constant;
import org.venraas.hermes.common.EnumResetInterval;
import org.venraas.hermes.data_entity.Conf;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


@Service
public class ConfClient {	
	
	static private final String TYPE_NAME = "conf"; 
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(ConfClient.class);	
	
	private Apollo _apo = null;	

	
	public ConfClient() {
		if (null == _apo) {
			_apo = Apollo.getInstance();
		}		
	}
	
	@CacheEvict(value="cache_conf", allEntries=true)
	public void reset() { }

	@Cacheable(value="cache_conf", key="{#root.methodName, #codeName}")	
	public int get_routing_reset_interval(String codeName) {
		
		VEN_LOGGER.info("caching get_routing_reset_interval({})", codeName);
				
		int interval = EnumResetInterval.HOUR.get_enumCode();
		
		if (null == codeName || codeName.isEmpty()) return interval;				
		
		try {						
	        SearchResponse resp = get_conf_(codeName);
	        	        
	        if (0 < resp.getHits().getTotalHits()) {
	        	SearchHit h = resp.getHits().getAt(0);	        		        	
	        	String jsonStr = h.getSourceAsString();	        
	        	
	        	Gson g = new Gson();	        	    	        		
	        	Conf con = g.fromJson(jsonStr, Conf.class);
	        	String val = con.getRouting_reset_interval();
	        		        	
	        	switch (val) {
	        		case "SECOND":
	        			interval = EnumResetInterval.SECOND.get_enumCode();	        			
	        			break;
	        		case "MINUTE":
	        			interval = EnumResetInterval.MINUTE.get_enumCode();	        			
	        			break;
	        		case "DAY":
	        			interval = EnumResetInterval.DAY.get_enumCode();
	        			break;
	        		case "WEEK":
	        			interval = EnumResetInterval.WEEK.get_enumCode();
	        			break;
	        		case "MONTH":
	        			interval = EnumResetInterval.MONTH.get_enumCode();
	        			break;
	        		case "HOUR":
	        		default:
	        			interval = EnumResetInterval.HOUR.get_enumCode();
	        			break;
	        	}
	        }
	        
		} catch(Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		} 
        
        return interval;        
	}
	
	@Cacheable(value="cache_conf", key="{#codeName, #cacheField}")	
	public double get_traffic_percent_normal(String codeName, String cacheField) {
		
		VEN_LOGGER.info("caching get_traffic_percent_normal({})", codeName);
		
		double pct = Constant.TRAFFIC_PERCENT_NORMAL;
	
		if (null == codeName || codeName.isEmpty()) return pct;	
		
		try {
			SearchResponse resp = get_conf_(codeName);
			
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
	
	@CachePut(value="cache_conf", key="{#codeName, #cacheField}")	
	public double set_traffic_percent_normal(String codeName, String cacheField, double pct) {
		
		VEN_LOGGER.info("update and caching set_traffic_percent_normal({})", codeName);				
		
		if(null == codeName || codeName.isEmpty()) return pct;
						
		try {
			Conf con = new Conf();	        

	        SearchResponse resp = get_conf_(codeName);
	        if (0 < resp.getHits().getTotalHits()) {
	        	SearchHit h = resp.getHits().getAt(0);
//	        	String docID = h.getId();        	
	        	String jsonStr = h.getSourceAsString();	        	
	        	Gson g = new Gson();
	        	con = g.fromJson(jsonStr, Conf.class);	        	
	        }
	        
        	//-- updated json
        	con.setTraffic_pct_normal(pct);
        	con.setUpdate_dt(Utility.now());
        	Gson g = new Gson();
        	String updateJson = g.toJson(con);
	        
        	String indexName = String.format("%s_hermes", codeName);
        	IndexResponse indexResp = _apo.esClient().prepareIndex(indexName, TYPE_NAME)
        		.setSource(updateJson)
        		.get();
        	
        	boolean isCreated = indexResp.isCreated();
        	
        	if (isCreated) { 
        		VEN_LOGGER.info("a record of {}/{} is created: {}", indexName, TYPE_NAME, updateJson);
        	}
	        
		} catch(Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		}
        
        return pct;
	}
	
	
	private SearchResponse get_conf_(String codeName) {
		
		SearchResponse resp = null;

		String indexName = String.format("%s_hermes", codeName);

		QueryBuilder qb = QueryBuilders.matchAllQuery();

		resp = _apo.esClient().prepareSearch(indexName)
                .setTypes(TYPE_NAME)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(qb)	                	                
                .addSort(EnumConf.update_dt.name(), SortOrder.DESC)
                .setSize(1)
                .execute()
                .actionGet();

		return resp;
	}
	
}
