package org.venraas.hermes.apollo.hermes;

import java.util.ArrayList;
import java.util.List;

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

	@Cacheable(value="cache_conf", key="{#codeName, #cacheField}")	
	public EnumResetInterval get_routing_reset_interval(String codeName, String cacheField) {
		
		VEN_LOGGER.info("caching get_routing_reset_interval({})", codeName);
				
		EnumResetInterval enumInt = EnumResetInterval.HOUR;
		
		if (null == codeName || codeName.isEmpty()) return enumInt;				
		
		try {						
	        SearchResponse resp = _search_conf(codeName);
	        	        
	        if (0 < resp.getHits().getTotalHits()) {
	        	SearchHit h = resp.getHits().getAt(0);	        		        	
	        	String jsonStr = h.getSourceAsString();	        
	        	
	        	Gson g = new Gson();	        	    	        		
	        	Conf con = g.fromJson(jsonStr, Conf.class);
	        	String val = con.getRouting_reset_interval();
	        	
	        	enumInt = EnumResetInterval.valueOf(val);	        	
	        }	        
		} catch(Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		} 
        
        return enumInt;        
	}
	
	@CachePut(value="cache_conf", key="{#codeName, #cacheField}")
	public EnumResetInterval set_routing_reset_interval(String codeName, String cacheField, EnumResetInterval enumInt) {
		
		VEN_LOGGER.info("update and caching set_routing_reset_interval({})", codeName);				
		
		if (null == codeName || codeName.isEmpty()) return enumInt;
						
		try {
			Conf con = new Conf();	        

	        SearchResponse resp = _search_conf(codeName);
	        if (0 < resp.getHits().getTotalHits()) {
	        	SearchHit h = resp.getHits().getAt(0);
//	        	String docID = h.getId();        	
	        	String jsonStr = h.getSourceAsString();	        	
	        	Gson g = new Gson();
	        	con = g.fromJson(jsonStr, Conf.class);	        	
	        }
	        
        	//-- updated json
        	con.setRouting_reset_interval(enumInt.name());
        	con.setUpdate_dt(Utility.now());
        	Gson g = new Gson();
        	String updateJson = g.toJson(con);
	                	
        	IndexResponse indexResp = _index_conf_(codeName, updateJson);        	
        	boolean isCreated = indexResp.isCreated();
        	
        	if (isCreated) {
        		String indexName = String.format("%s_hermes", codeName);
        		VEN_LOGGER.info("a record of {}/{} is created: {}", indexName, TYPE_NAME, updateJson);
        	}
	        
		} catch(Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		}
        
        return enumInt;
	}
	
	@Cacheable(value="cache_conf", key="{#codeName, #cacheField}")	
	public double get_traffic_percent_normal(String codeName, String cacheField) {
		
		VEN_LOGGER.info("caching get_traffic_percent_normal({})", codeName);
		
		double pct = Constant.TRAFFIC_PERCENT_NORMAL;
	
		if (null == codeName || codeName.isEmpty()) return pct;	
		
		try {
			SearchResponse resp = _search_conf(codeName);
			
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
		
		if (null == codeName || codeName.isEmpty()) return pct;
						
		try {
			Conf con = new Conf();	        

	        SearchResponse resp = _search_conf(codeName);
	        if (0 < resp.getHits().getTotalHits()) {
	        	SearchHit h = resp.getHits().getAt(0);
//	        	String docID = h.getId();        	
	        	String jsonStr = h.getSourceAsString();	        	
	        	Gson g = new Gson();
	        	con = g.fromJson(jsonStr, Conf.class);	        	
	        }
	        
        	//-- updated JSON
        	con.setTraffic_pct_normal(pct);
        	con.setUpdate_dt(Utility.now());
        	Gson g = new Gson();
        	String updateJson = g.toJson(con);
	                	
        	IndexResponse indexResp = _index_conf_(codeName, updateJson);
        	
        	boolean isCreated = indexResp.isCreated();
        	
        	if (isCreated) {
        		String indexName = String.format("%s_hermes", codeName);
        		VEN_LOGGER.info("a record of {}/{} is created: {}", indexName, TYPE_NAME, updateJson);
        	}
	        
		} catch(Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		}
        
        return pct;
	}
		
	/**
	 * Gets header names which'll be forwarding to next tier.
	 * 
	 * @param codeName
	 * @param cacheField
	 * @return
	 */
	@Cacheable(value="cache_conf", key="{#codeName, #cacheField}")
	public List<String> get_http_forward_headers(String codeName, String cacheField) {
		
		VEN_LOGGER.info("get and caching get_http_forward_headers({})", codeName);
		
		List<String> headers = new ArrayList<String>();
		
		if (null == codeName || codeName.isEmpty()) return headers;
						
		try {
	        SearchResponse resp = _search_conf(codeName);
	        	        
	        if (0 < resp.getHits().getTotalHits()) {
	        	SearchHit h = resp.getHits().getAt(0);	        		        	
	        	String jsonStr = h.getSourceAsString();	        
	        	
	        	Gson g = new Gson();
	        	Conf con = g.fromJson(jsonStr, Conf.class);
	        	headers = con.getHttp_forward_headers();	
	        }
		} catch(Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		}
        
        return headers;
	}
	
	@CachePut(value="cache_conf", key="{#codeName, #cacheField}")
	public List<String> set_http_forward_headers(String codeName, String cacheField, List<String> headers) {
		
		VEN_LOGGER.info("update and caching set_http_forward_headers({})", codeName);		
				
		if (null == codeName || codeName.isEmpty()) return headers;
						
		try {
			Conf con = new Conf();

			//-- get setting from ES
	        SearchResponse resp = _search_conf(codeName);
	        if (0 < resp.getHits().getTotalHits()) {
	        	SearchHit h = resp.getHits().getAt(0);        
	        	String jsonStr = h.getSourceAsString();	        	
	        	Gson g = new Gson();
	        	con = g.fromJson(jsonStr, Conf.class);	        	
	        }	        	        
	        
        	//-- updated Json	        
	        con.setHttp_forward_headers(headers);        	
        	con.setUpdate_dt(Utility.now());
        	Gson g = new Gson();
        	String updateJson = g.toJson(con);
	                	
        	IndexResponse indexResp = _index_conf_(codeName, updateJson);        	
        	boolean isCreated = indexResp.isCreated();
        	
        	if (isCreated) {
        		String indexName = String.format("%s_hermes", codeName);
        		VEN_LOGGER.info("a record of {}/{} is created: {}", indexName, TYPE_NAME, updateJson);
        	}
	        
		} catch(Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		}
        
        return headers;
	}
	

	private IndexResponse _index_conf_(String codeName, String updateJson) {
		
		String indexName = String.format("%s_hermes", codeName);
		
    	IndexResponse indexResp = _apo.esClient().prepareIndex(indexName, TYPE_NAME)
        		.setSource(updateJson)
        		.get();
    	
    	return indexResp;
	}
	
		
	private SearchResponse _search_conf(String codeName) {
		
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
