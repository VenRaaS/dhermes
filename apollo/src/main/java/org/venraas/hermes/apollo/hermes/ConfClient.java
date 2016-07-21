package org.venraas.hermes.apollo.hermes;

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
import org.venraas.hermes.data_entity.Conf;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
	public String get_routing_reset_interval(String codeName) {
		VEN_LOGGER.info("caching get_routing_reset_interval({})", codeName);
		
		String interval = "";
		
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
///	        	JsonReader r = Json.createReader(new StringReader(jsonStr));
///	        	JsonArray comps = r.readObject().getJsonArray("companies");
	        	
	        	JsonParser jsonParser = new JsonParser();
	        	JsonArray comps = jsonParser.parse(jsonStr).getAsJsonArray();
	        	
	        	Gson g = new Gson();
	        	for (JsonElement e : comps) {	        		
	        		JsonObject o = (JsonObject)e;
	        		Conf con = g.fromJson(e, Conf.class);
	        		interval = con.getRouting_reset_interval();
	        	}
	        	
	        }
		} catch(Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		} 
        
        return interval;        
	}
}
