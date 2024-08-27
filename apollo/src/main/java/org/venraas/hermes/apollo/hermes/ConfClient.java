package org.venraas.hermes.apollo.hermes;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.elasticsearch.action.index.IndexResponse;
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
import org.venraas.hermes.common.EnumResetInterval;
import org.venraas.hermes.data_entity.Conf;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class ConfClient {

	static private final String TYPE_NAME = "conf";

	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(ConfClient.class);

	private Apollo _apo = null;

	public ConfClient() {
		if (null == _apo) {
			_apo = Apollo.getInstance();
		}
	}

	public void reset() {
	}

	public String get_routing_reset_interval(String codeName, String cacheField) {

		VEN_LOGGER.info("caching get_routing_reset_interval({})", codeName);

		String jsonStr = "";

		if (null == codeName || codeName.isEmpty())
			return jsonStr;

		JsonObject resp = _search_conf(codeName);
		if (null != resp) {
			JsonObject hits_obj = resp.getAsJsonObject("hits");
			JsonObject total_object = hits_obj.getAsJsonObject("total");
			int totalHits = total_object.getAsJsonPrimitive("value").getAsInt();
			if (0 < totalHits) {
				JsonArray hits_array = hits_obj.getAsJsonArray("hits");
				JsonObject hit_obj = hits_array.get(0).getAsJsonObject();
				JsonObject source_obj = hit_obj.getAsJsonObject("_source");
				jsonStr = source_obj.toString();
			}
		}
		
		return jsonStr;
	}

	public EnumResetInterval set_routing_reset_interval(String codeName, String cacheField, EnumResetInterval enumInt) {

		VEN_LOGGER.info("update and caching set_routing_reset_interval({})", codeName);

		if (null == codeName || codeName.isEmpty())
			return enumInt;

		try {
			
			Conf con = new Conf();

			JsonObject resp = _search_conf(codeName);
			if (null != resp) {
				JsonObject hits_obj = resp.getAsJsonObject("hits");
				JsonObject total_object = hits_obj.getAsJsonObject("total");
				int totalHits = total_object.getAsJsonPrimitive("value").getAsInt();
				if (0 < totalHits) {
					JsonArray hits_array = hits_obj.getAsJsonArray("hits");
					JsonObject hit_obj = hits_array.get(0).getAsJsonObject();
					JsonObject source_obj = hit_obj.getAsJsonObject("_source");
					String jsonStr = source_obj.toString();
					Gson g = new Gson();
					con = g.fromJson(jsonStr, Conf.class);
				}
			}

			// -- updated json
			con.setRouting_reset_interval(enumInt.name());
			con.setUpdate_dt(Utility.now());
			Gson g = new Gson();
			String updateJson = g.toJson(con);

			JsonObject index_resp = _index_conf_(codeName, updateJson);
			JsonPrimitive result_obj = index_resp.getAsJsonPrimitive("result");
			String result_string = (null == result_obj ? "" : result_obj.getAsString());
			boolean isCreated = (result_string == "created");

			if (isCreated) {
				String indexName = String.format("%s_hermes_conf", codeName);
				VEN_LOGGER.info("a record of {} is created: {}", indexName, updateJson);
			}

		} catch (Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		}

		return enumInt;
	}

	public String get_traffic_percent_normal(String codeName, String cacheField) {

		VEN_LOGGER.info("caching get_traffic_percent_normal({})", codeName);

		String jsonStr = "";

		if (null == codeName || codeName.isEmpty())
			return jsonStr;

		JsonObject resp = _search_conf(codeName);
		if (null != resp) {
			JsonObject hits_obj = resp.getAsJsonObject("hits");
			JsonObject total_object = hits_obj.getAsJsonObject("total");
			int totalHits = total_object.getAsJsonPrimitive("value").getAsInt();
			if (0 < totalHits) {
				JsonArray hits_array = hits_obj.getAsJsonArray("hits");
				JsonObject hit_obj = hits_array.get(0).getAsJsonObject();
				JsonObject source_obj = hit_obj.getAsJsonObject("_source");
				jsonStr = source_obj.toString();
			}
		}

		return jsonStr;
	}

	public double set_traffic_percent_normal(String codeName, String cacheField, double pct) {

		VEN_LOGGER.info("update and caching set_traffic_percent_normal({})", codeName);

		if (null == codeName || codeName.isEmpty())
			return pct;

		try {

			Conf con = new Conf();

			JsonObject resp = _search_conf(codeName);
			if (null != resp) {
				JsonObject hits_obj = resp.getAsJsonObject("hits");
				JsonObject total_object = hits_obj.getAsJsonObject("total");
				int totalHits = total_object.getAsJsonPrimitive("value").getAsInt();
				if (0 < totalHits) {
					JsonArray hits_array = hits_obj.getAsJsonArray("hits");
					JsonObject hit_obj = hits_array.get(0).getAsJsonObject();
					JsonObject source_obj = hit_obj.getAsJsonObject("_source");
					String jsonStr = source_obj.toString();
					Gson g = new Gson();
					con = g.fromJson(jsonStr, Conf.class);
				}
			}

			// -- updated JSON
			con.setTraffic_pct_normal(pct);
			con.setUpdate_dt(Utility.now());
			Gson g = new Gson();
			String updateJson = g.toJson(con);

			JsonObject index_resp = _index_conf_(codeName, updateJson);
			JsonPrimitive result_obj = index_resp.getAsJsonPrimitive("result");
			String result_string = (null == result_obj ? "" : result_obj.getAsString());
			boolean isCreated = (result_string == "created");

			if (isCreated) {
				String indexName = String.format("%s_hermes_conf", codeName);
				VEN_LOGGER.info("a record of {} is created: {}", indexName, updateJson);
			}

		} catch (Exception ex) {
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
	public String get_http_forward_headers(String codeName, String cacheField) {
		String jsonStr = "";

		VEN_LOGGER.info("get and caching get_http_forward_headers({})", codeName);

		if (null == codeName || codeName.isEmpty())
			return jsonStr;

		JsonObject resp = _search_conf(codeName);
		if (null != resp) {
			JsonObject hits_obj = resp.getAsJsonObject("hits");
			JsonObject total_object = hits_obj.getAsJsonObject("total");
			int totalHits = total_object.getAsJsonPrimitive("value").getAsInt();
			if (0 < totalHits) {
				JsonArray hits_array = hits_obj.getAsJsonArray("hits");
				JsonObject hit_obj = hits_array.get(0).getAsJsonObject();
				JsonObject source_obj = hit_obj.getAsJsonObject("_source");
				jsonStr = source_obj.toString();
			}
		}

		return jsonStr;
	}

	public List<String> set_http_forward_headers(String codeName, String cacheField, List<String> headers) {

		VEN_LOGGER.info("update and caching set_http_forward_headers({})", codeName);

		if (null == codeName || codeName.isEmpty())
			return headers;

		try {
			
			Conf con = new Conf();

			// -- get setting from ES
			JsonObject resp = _search_conf(codeName);
			if (null != resp) {
				JsonObject hits_obj = resp.getAsJsonObject("hits");
				JsonObject total_object = hits_obj.getAsJsonObject("total");
				int totalHits = total_object.getAsJsonPrimitive("value").getAsInt();
				if (0 < totalHits) {
					JsonArray hits_array = hits_obj.getAsJsonArray("hits");
					JsonObject hit_obj = hits_array.get(0).getAsJsonObject();
					JsonObject source_obj = hit_obj.getAsJsonObject("_source");
					String jsonStr = source_obj.toString();
					Gson g = new Gson();
					con = g.fromJson(jsonStr, Conf.class);
				}
			}

			// -- updated Json
			con.setHttp_forward_headers(headers);
			con.setUpdate_dt(Utility.now());
			Gson g = new Gson();
			String updateJson = g.toJson(con);

			JsonObject index_resp = _index_conf_(codeName, updateJson);
			JsonPrimitive result_obj = index_resp.getAsJsonPrimitive("result");
			String result_string = (null == result_obj ? "" : result_obj.getAsString());
			boolean isCreated = (result_string.equals("created"));

			if (isCreated) {
				String indexName = String.format("%s_hermes_conf", codeName);
				VEN_LOGGER.info("a record of {} is created: {}", indexName, updateJson);
			}

		} catch (Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());

			headers = null; // means failed
		}

		return headers;
	}

	private JsonObject _index_conf_(String codeName, String updateJson) {

		///////////////////////////////////////////////////////////////

//		String indexName = String.format("%s_hermes", codeName);
//		
//    	IndexResponse indexResp = _apo.esClient().prepareIndex(indexName, TYPE_NAME)
//        		.setSource(updateJson)
//        		.execute()
//        		.actionGet(Constant.TIMEOUT_INDEX_MILLIS);
//    	
//    	return indexResp;

		///////////////////////////////////////////////////////////////

		JsonObject resp = null;

		try {

			String username = "elastic";
			String password = System.getenv("ES_COMP_PWD");
			String auth = username + ":" + password;
			String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
			String authHeader = "Basic " + encodedAuth;

			String indexName = codeName + "_hermes_conf";
			Content content = Request.Post("http://es-comp.venraas.private:9200/" + indexName + "/_doc")
					.connectTimeout(Constant.TIMEOUT_INDEX_MILLIS).socketTimeout(Constant.TIMEOUT_INDEX_MILLIS)
					.setHeader("Authorization", authHeader).bodyString(updateJson, ContentType.APPLICATION_JSON)
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

	private JsonObject _search_conf(String codeName) {

		///////////////////////////////////////////////////////////////

//		SearchResponse resp = null;
//
//		String indexName = String.format("%s_hermes", codeName);
//
//		QueryBuilder qb = QueryBuilders.matchAllQuery();
//
//		resp = _apo.esClient().prepareSearch(indexName)
//                .setTypes(TYPE_NAME)
//                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//                .setQuery(qb)	                	                
//                .addSort(EnumConf.update_dt.name(), SortOrder.DESC)
//                .setSize(1)
//                .execute()
//                .actionGet(Constant.TIMEOUT_SEARCH_MILLIS);
//
//		return resp;

//		///////////////////////////////////////////////////////////////////////

		JsonObject resp = null;

		try {

			String username = "elastic";
			String password = System.getenv("ES_COMP_PWD");
			String auth = username + ":" + password;
			String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
			String authHeader = "Basic " + encodedAuth;

			String post_query = "{ \"query\": {\"match_all\": {}},"
					+ "\"sort\": [{\"update_dt\": {\"order\": \"desc\"}}]," + "\"size\": 1}";

			String indexName = codeName + "_hermes_conf";
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
