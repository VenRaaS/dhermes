package org.venraas.hermes.apollo.raas;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.venraas.hermes.apollo.Apollo;
import org.venraas.hermes.apollo.mappings.Com_pkgs;
import org.venraas.hermes.common.Utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class CompanyClient {
	
	private static final String VENRAAS_INDEX_NAME = "venraas";
	
	static private final String TYPE_NAME = "com_pkgs"; 
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(CompanyClient.class);	
	
	private Apollo _apo = null;	

	
	public CompanyClient() {
		if (null == _apo) {
			_apo = Apollo.getInstance();
		}		
	}
	
	@CacheEvict(value="cache_company", allEntries=true)
	public void reset() { }

	@Cacheable(value="cache_company", key="{#token}")
	public String getCodeName(String token) {
		VEN_LOGGER.info("caching getCodeName({})", token);
		
		String codeName = "";
		
		if(null == token || token.isEmpty())	return codeName;				
		
		try {			
			BoolQueryBuilder bq = QueryBuilders.boolQuery().filter(QueryBuilders.termQuery(Com_pkgs.token, token));
			
	        SearchResponse resp = _apo.esClient()
	        		.prepareSearch(VENRAAS_INDEX_NAME)
	                .setTypes(TYPE_NAME)
	                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	                .setQuery(bq)
	                .addSort(Com_pkgs.webServerTime, SortOrder.DESC)
	                .setSize(1)
	                .execute()
	                .actionGet();
	        	        
	        if (0 < resp.getHits().getTotalHits()) {
	        	SearchHit h = resp.getHits().getAt(0);	        		        	
	        	String jsonStr = h.getSourceAsString();
	        	
	        	JsonParser jsonParser = new JsonParser();
	        	JsonObject o = jsonParser.parse(jsonStr).getAsJsonObject();	        	
	        	JsonArray comps = o.getAsJsonArray("companies");
	        		        	
	        	for (JsonElement v : comps) {
	        		JsonObject c = (JsonObject)v;
	        		String tok = c.get("token").getAsString();
	        		
	        		if (0 == tok.compareToIgnoreCase(token)) {
	        			codeName = c.get("codeName").getAsString();
	        			break;
	        		}
	        	}	        	
	        }
	        
		} catch(Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		} 
        
        return codeName;        
	}
}
