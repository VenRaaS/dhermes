package org.venraas.hermes.apollo.hermes;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.apollo.Apollo;
import org.venraas.hermes.apollo.mappings.EnumConf;
import org.venraas.hermes.apollo.mappings.EnumJumper;
import org.venraas.hermes.common.Constant;
import org.venraas.hermes.common.Utility;
import org.venraas.hermes.data_entity.Jumper;

import com.google.gson.Gson;


public class JumperClient {
	
	static private final String TYPE_NAME = "jumper"; 
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(JumperClient.class);	
	
	private Apollo _apo = null;	

	
	public JumperClient() {
		if (null == _apo) {
			_apo = Apollo.getInstance();
		}
	}
	
	public String get_group_key (String codeName, String uid) {
		
		String grpKey = "";
		
		VEN_LOGGER.info("caching get_group_key({}, {})", codeName, uid);
		
		if (null == codeName || codeName.isEmpty() || 
			null == uid || uid.isEmpty()) 
			return grpKey;				
		
		try {						
			String indexName = String.format("%s_hermes", codeName);			
			
			QueryBuilder qb = 
					QueryBuilders.boolQuery().filter(
						QueryBuilders.termQuery(EnumJumper.uid.name(), uid)
					);

			SearchResponse resp = 
					_apo.esClient().prepareSearch(indexName)
	                .setTypes(TYPE_NAME)
	                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	                .setQuery(qb)	                	                
	                .addSort(EnumConf.update_dt.name(), SortOrder.DESC)
	                .setSize(1)
	                .execute()
	                .actionGet(Constant.TIMEOUT_SEARCH_MILLIS);
	        	        
	        if (0 < resp.getHits().getTotalHits()) {
	        	SearchHit h = resp.getHits().getAt(0);	        		        	
	        	String jsonStr = h.getSourceAsString();	        
	        	
	        	Gson g = new Gson();	        	    	        		
	        	Jumper jumper = g.fromJson(jsonStr, Jumper.class);
	        	grpKey = jumper.getGroup_key();
	        }
		} catch(Exception ex) {
			VEN_LOGGER.error(ex.getMessage());
			VEN_LOGGER.error(Utility.stackTrace2string(ex));			
		} 
        
        return grpKey;
	}
		
	public boolean set_jumper (String codeName, String uid, String grpKey) {
		
		boolean isSuccess = false;
		
		VEN_LOGGER.info("set_jumper({}, {}, {})", codeName, uid, grpKey);				
		
		if (null == codeName || codeName.isEmpty()) return isSuccess;
		if (null == uid ||  uid.isEmpty()) return isSuccess;
		if (null == grpKey || grpKey.isEmpty()) return isSuccess;
						
		try {			
			Jumper jumper = new Jumper();

        	String docID = "";
        	
			//-- query specified jumper setting if any 
	        SearchResponse resp = _search_jumper(codeName, uid);
	        if (0 < resp.getHits().getTotalHits()) {
	        	SearchHit h = resp.getHits().getAt(0);
	        	
	        	docID = h.getId();
	        	String jsonStr = h.getSourceAsString();	        	
	        	Gson g = new Gson();
	        	jumper = g.fromJson(jsonStr, Jumper.class);	        	
	        }
	        
        	//-- update json
	        jumper.setGroup_key(grpKey);
        	jumper.setUid(uid);
        	jumper.setUpdate_dt(Utility.now());
        	Gson g = new Gson();
        	String updateJson = g.toJson(jumper);
        	
        	String indexName = String.format("%s_hermes", codeName);        	        	
        	
        	if (docID.isEmpty()) {
	        	IndexResponse indexResp = 
	        			_apo.esClient().prepareIndex(indexName, TYPE_NAME)
	            		.setSource(updateJson)
	            		.execute()
	            		.actionGet(Constant.TIMEOUT_INDEX_MILLIS);
	        	
	        	VEN_LOGGER.info("a record of {}/{} is created: {}", indexName, TYPE_NAME, updateJson);
        	} else {
        		UpdateResponse updateResp =
        				_apo.esClient().prepareUpdate(indexName, TYPE_NAME, docID)
        				.setDoc(updateJson)
        				.execute()
        				.actionGet(Constant.TIMEOUT_INDEX_MILLIS);
        		
        		VEN_LOGGER.info("a record of {}/{} is updated: {}", indexName, TYPE_NAME, updateJson);        	
        	}
        	        	        	
        	isSuccess = true;	        
		} catch(Exception ex) {
			VEN_LOGGER.error(ex.getMessage());
			VEN_LOGGER.error(Utility.stackTrace2string(ex));			
		}
        
        return isSuccess;
	}
	
	private SearchResponse _search_jumper(String codeName, String uid) {
		
		String indexName = String.format("%s_hermes", codeName);
		
		QueryBuilder qb = 
				QueryBuilders.boolQuery().filter(
					QueryBuilders.termQuery(EnumJumper.uid.name(), uid)
				);

		SearchResponse resp = 
				_apo.esClient().prepareSearch(indexName)
                .setTypes(TYPE_NAME)
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(qb)	                	                
                .addSort(EnumConf.update_dt.name(), SortOrder.DESC)
                .setSize(1)
                .execute()
                .actionGet(Constant.TIMEOUT_SEARCH_MILLIS);
		
		return resp;		
	}

	
	
	
}
