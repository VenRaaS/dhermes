package org.venraas.hermes.apollo.raas;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.apollo.Apollo;
import org.venraas.hermes.apollo.mappings.EnumCompany;
import org.venraas.hermes.common.Utility;

//@Service
public class CompanyClient {
	
	private static final String VENRAAS_INDEX_NAME = "venraas";
	
	static private final String TYPE_NAME = "com_pkgs_test"; 
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(CompanyClient.class);	
	
	private Apollo _apo = null;	

	
	public CompanyClient() {
		if (null == _apo) {
			_apo = Apollo.getInstance();
		}		
	}
	
//	@CacheEvict(value="cache_company", allEntries=true)
	public void reset() {
	}
	

//	@Cacheable(value="cache_company", key="{#token}")
	public String getCodeName(String token) {
		VEN_LOGGER.info("caching getCodeName({})", token);
		
		String codeName = "";
		
		if(null == token || token.isEmpty())	return codeName;
		
		try {	        
			
	        SearchResponse resp = _apo.esClient().prepareSearch(VENRAAS_INDEX_NAME)
	                .setTypes(TYPE_NAME)
	                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	                .setQuery(QueryBuilders.termQuery(EnumCompany.token.name(), token))	                	                
	                .addSort(EnumCompany.webServerTime.name(), SortOrder.DESC)
	                .execute()
	                .actionGet();
	        	        
	        if (0 < resp.getHits().getTotalHits()) {
	        	SearchHit h = resp.getHits().getAt(0);	        		        	
	        	String jsonStr = h.getSourceAsString();
	        	JsonReader r = Json.createReader(new StringReader(jsonStr));
	        	JsonArray comps = r.readObject().getJsonArray("companies");	        	
	        	for (JsonValue v : comps) {
	        		JsonObject c = (JsonObject)v;
	        		String tok = c.getString(EnumCompany.token.name());
	        		
	        		if (0 == tok.compareToIgnoreCase(token)) {
	        			codeName = c.getString(EnumCompany.codeName.name());
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
