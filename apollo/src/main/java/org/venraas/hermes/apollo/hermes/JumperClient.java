package org.venraas.hermes.apollo.hermes;

import java.util.Base64;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;


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
		
		////////////////////////////////////////////////////////////	
//		String indexName = String.format("%s_hermes", codeName);			
//		
//		QueryBuilder qb = 
//				QueryBuilders.boolQuery().filter(
//					QueryBuilders.termQuery(EnumJumper.uid.name(), uid)
//				);
//
//		SearchResponse resp = 
//				_apo.esClient().prepareSearch(indexName)
//                .setTypes(TYPE_NAME)
//                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .setQuery(qb)	                	                
//                .addSort(EnumConf.update_dt.name(), SortOrder.DESC)
//                .setSize(1)
//                .execute()
//                .actionGet(Constant.TIMEOUT_SEARCH_MILLIS);
//        	        
//        if (0 < resp.getHits().getTotalHits()) {
//        	SearchHit h = resp.getHits().getAt(0);
//        	String jsonStr = h.getSourceAsString();
//        	
//        	Gson g = new Gson();
//        	Jumper jumper = g.fromJson(jsonStr, Jumper.class);
//        	grpKey = jumper.getGroup_key();
//        }
//
//        
//        return grpKey;
        
        ////////////////////////////////////////////////      
		
		try {

			String username = "elastic";
			String password = System.getenv("ES_COMP_PWD");
			String auth = username + ":" + password;
			String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
			String authHeader = "Basic " + encodedAuth;

			String post_query = "{ \"query\": { \"bool\": { \"filter\": [ { \"term\": { \"uid\": \"" + uid
					+ "\" } } ] } } ," + "\"sort\": [{\"update_dt\": {\"order\": \"desc\"}}]," + "\"size\": 1}";

			String indexName = codeName + "_hermes_jumper";
			
			Content content = Request.Post("http://es-comp.venraas.private:9200/" + indexName + "/_search")
					.connectTimeout(Constant.TIMEOUT_SEARCH_MILLIS).socketTimeout(Constant.TIMEOUT_SEARCH_MILLIS)
					.setHeader("Authorization", authHeader).bodyString(post_query, ContentType.APPLICATION_JSON)
					.execute().returnContent();
			String jsonStr = content.asString();
			
			JsonParser jp = new JsonParser();
			JsonObject resp = jp.parse(jsonStr).getAsJsonObject();
			
			if (null != resp) {
				JsonObject hits_obj = resp.getAsJsonObject("hits");
				JsonObject total_object = hits_obj.getAsJsonObject("total");
				int totalHits = total_object.getAsJsonPrimitive("value").getAsInt();
				if (0 < totalHits) {
					JsonArray hits_array = hits_obj.getAsJsonArray("hits");
					JsonObject hit_obj = hits_array.get(0).getAsJsonObject();
					JsonObject source_obj = hit_obj.getAsJsonObject("_source");
					String source_string = source_obj.toString();
					Gson g = new Gson();
		        	Jumper jumper = g.fromJson(source_string, Jumper.class);
		        	grpKey = jumper.getGroup_key();
				}
			}

		} catch (Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
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
			JsonObject search_resp = _search_jumper(codeName, uid);
			if (null != search_resp) {
				JsonObject hits_obj = search_resp.getAsJsonObject("hits");
				JsonObject total_object = hits_obj.getAsJsonObject("total");
				int totalHits = total_object.getAsJsonPrimitive("value").getAsInt();
				if (0 < totalHits) {
					JsonArray hits_array = hits_obj.getAsJsonArray("hits");
					JsonObject hit_obj = hits_array.get(0).getAsJsonObject();
					JsonPrimitive id_obj = hit_obj.getAsJsonPrimitive("_id");
					docID = id_obj.getAsString();
					JsonObject source_obj = hit_obj.getAsJsonObject("_source");
					String jsonStr = source_obj.toString();
					Gson g = new Gson();
					jumper = g.fromJson(jsonStr, Jumper.class);
				}
			}

        	//-- update json
	        jumper.setGroup_key(grpKey);
        	jumper.setUid(uid);
        	jumper.setUpdate_dt(Utility.now());
        	Gson g = new Gson();
        	String updateJson = g.toJson(jumper);

        	if (docID.isEmpty()) {
        		///////////////////////////////////////////////////////////////////
//	        	IndexResponse indexResp = 
//	        			_apo.esClient().prepareIndex(indexName, TYPE_NAME)
//	            		.setSource(updateJson)
//	            		.execute()
//	            		.actionGet(Constant.TIMEOUT_INDEX_MILLIS);
//	        	
//	        	VEN_LOGGER.info("a record of {}/{} is created: {}", indexName, TYPE_NAME, updateJson);
	        	/////////////////////////////////////////////////////////////////////
	        	
	        	String username = "elastic";
				String password = System.getenv("ES_COMP_PWD");
				String auth = username + ":" + password;
				String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
				String authHeader = "Basic " + encodedAuth;

	        	String indexName = String.format("%s_hermes_jumper", codeName);    
	        	
				Content content = Request.Post("http://es-comp.venraas.private:9200/" + indexName + "/_doc")
						.connectTimeout(Constant.TIMEOUT_INDEX_MILLIS).socketTimeout(Constant.TIMEOUT_INDEX_MILLIS)
						.setHeader("Authorization", authHeader).bodyString(updateJson, ContentType.APPLICATION_JSON)
						.execute().returnContent();
				String jsonStr = content.asString();

				JsonParser jp = new JsonParser();
				JsonObject resp = jp.parse(jsonStr).getAsJsonObject();
				JsonPrimitive result_obj = resp.getAsJsonPrimitive("result");
				String result_string = (null == result_obj ? "" : result_obj.getAsString());
				boolean isCreated = (result_string.equals("created"));
				
				if (isCreated) {
					isSuccess = true;
					VEN_LOGGER.info("a record of {} is created: {}", indexName, updateJson);
				} else {
					VEN_LOGGER.info("a record of {} is NOT created: {}", indexName, updateJson);
				}
	        	
        	} else {
        		///////////////////////////////////////////////////////////////////
//        		UpdateResponse updateResp =
//        				_apo.esClient().prepareUpdate(indexName, TYPE_NAME, docID)
//        				.setDoc(updateJson)
//        				.execute()
//        				.actionGet(Constant.TIMEOUT_INDEX_MILLIS);
//        		
//        		VEN_LOGGER.info("a record of {}/{} is updated: {}", indexName, TYPE_NAME, updateJson);
        		///////////////////////////////////////////////////////////////////
        		
        		String username = "elastic";
				String password = System.getenv("ES_COMP_PWD");
				String auth = username + ":" + password;
				String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
				String authHeader = "Basic " + encodedAuth;

	        	String indexName = String.format("%s_hermes_jumper", codeName);    
	        	
				Content content = Request.Post("http://es-comp.venraas.private:9200/" + indexName + "/_doc/" + docID)
						.connectTimeout(Constant.TIMEOUT_INDEX_MILLIS).socketTimeout(Constant.TIMEOUT_INDEX_MILLIS)
						.setHeader("Authorization", authHeader).bodyString(updateJson, ContentType.APPLICATION_JSON)
						.execute().returnContent();
				String jsonStr = content.asString();
//
				JsonParser jp = new JsonParser();
				JsonObject resp = jp.parse(jsonStr).getAsJsonObject();
				JsonPrimitive result_obj = resp.getAsJsonPrimitive("result");
				String result_string = (null == result_obj ? "" : result_obj.getAsString());
				boolean isUpdated = (result_string.equals("updated"));
				
				if (isUpdated) {
					isSuccess = true;
					VEN_LOGGER.info("a record of {} is updated: {}", indexName, updateJson);
				} else {
					VEN_LOGGER.info("a record of {} is NOT updated: {}", indexName, updateJson);
				}	
        	}
  
		} catch(Exception ex) {
			VEN_LOGGER.error(ex.getMessage());
			VEN_LOGGER.error(Utility.stackTrace2string(ex));			
		}
        
        return isSuccess;
	}
	
	private JsonObject _search_jumper(String codeName, String uid) {
		
///////////////////////////////////////////////////////////////
		
//		String indexName = String.format("%s_hermes", codeName);
//		
//		QueryBuilder qb = 
//				QueryBuilders.boolQuery().filter(
//					QueryBuilders.termQuery(EnumJumper.uid.name(), uid)
//				);
//
//		SearchResponse resp = 
//				_apo.esClient().prepareSearch(indexName)
//                .setTypes(TYPE_NAME)
//                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .setQuery(qb)	                	                
//                .addSort(EnumConf.update_dt.name(), SortOrder.DESC)
//                .setSize(1)
//                .execute()
//                .actionGet(Constant.TIMEOUT_SEARCH_MILLIS);
//		
//		return resp;
		
///////////////////////////////////////////////////////////////

		JsonObject resp = null;
		
		try {

			String username = "elastic";
			String password = System.getenv("ES_COMP_PWD");
			String auth = username + ":" + password;
			String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
			String authHeader = "Basic " + encodedAuth;

			String post_query = "{ \"query\": { \"bool\": { \"filter\": [ { \"term\": { \"uid\": \"" + uid
					+ "\" } } ] } } ," + "\"sort\": [{\"update_dt\": {\"order\": \"desc\"}}]," + "\"size\": 1}";

			String indexName = codeName + "_hermes_jumper";
			Content content = Request.Post("http://es-comp.venraas.private:9200/" + indexName + "/_search")
					.connectTimeout(Constant.TIMEOUT_SEARCH_MILLIS).socketTimeout(Constant.TIMEOUT_SEARCH_MILLIS)
					.setHeader("Authorization", authHeader).bodyString(post_query, ContentType.APPLICATION_JSON)
					.execute().returnContent();
			String jsonStr = content.asString();

			JsonParser jp = new JsonParser();
			resp = jp.parse(jsonStr).getAsJsonObject();

		} catch (Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		}

		return resp;
	}

	
	
	
}
