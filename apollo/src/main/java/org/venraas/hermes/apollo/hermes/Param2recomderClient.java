package org.venraas.hermes.apollo.hermes;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.apollo.Apollo;
import org.venraas.hermes.apollo.mappings.EnumParam2recomder;
import org.venraas.hermes.common.Constant;
import org.venraas.hermes.common.EnumTrafficType;
import org.venraas.hermes.common.Utility;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


public class Param2recomderClient {

	final String TYPE_NAME = "param2recomder";
			
	private Apollo _apo = null;

	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(Param2recomderClient.class);
	
	
	public Param2recomderClient() {
		if (null == _apo) {
			_apo = Apollo.getInstance();
		}
	}
	
	public List<String> getDistinctGroups (String codeName) {
		
		VEN_LOGGER.info("caching getDistinctGroups({})", codeName);

		List<String> grps = new ArrayList<String> (20);
		
		if (codeName == null || codeName.isEmpty())	return grps;
						
		String indexName = String.format("%s%s", codeName, Constant.HERMES_INDEX_SUFFIX);
		String aggName = "group_key_count";
		
		//-- availability = 1 
		QueryBuilder qb = 
				QueryBuilders.boolQuery().filter(
						QueryBuilders.termQuery(EnumParam2recomder.availability.name(), 1)
				);
		
		//-- Terms Aggregation, 
		//   https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/_bucket_aggregations.html#java-aggs-bucket-terms
		AggregationBuilder ab = AggregationBuilders.terms(aggName).field(EnumParam2recomder.group_key.name());

		SearchRequestBuilder searchReq = 		
			_apo.esClient()
			.prepareSearch(indexName)
			.setTypes(TYPE_NAME)
			.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
			.setQuery(qb)
			.addAggregation(ab)				
			.setSize(0);

		SearchResponse resp = searchReq.execute().actionGet(Constant.TIMEOUT_SEARCH_MILLIS);			
		
		if (0 < resp.getHits().getTotalHits()) {
			Terms agg = resp.getAggregations().get(aggName);
			for (Terms.Bucket b : agg.getBuckets()) {
				String k = b.getKeyAsString();
//					long c = b.getDocCount();
				grps.add(k);
			}
		}
		
		return grps;
	}

	public List<Map<String, Object>> getGroupMapping (String codeName, String grpKey) {
		
		VEN_LOGGER.info("caching getGroupMapping({},{})", codeName, grpKey);
		
		List<Map<String, Object>> mappings = new ArrayList<Map<String, Object>>(20);		

		if (codeName == null || codeName.isEmpty() || null == grpKey || grpKey.isEmpty()) return mappings;			

		String indexName = String.format("%s%s", codeName, Constant.HERMES_INDEX_SUFFIX);
		
		SearchResponse resp = _query_group (indexName, grpKey);
					
		if (0 < resp.getHits().getTotalHits()) {
			SearchHit[] hits = resp.getHits().getHits();
			Gson gson = new Gson();
			
			for (SearchHit h : hits) {				
				String json = h.getSourceAsString();			
				Type type = new TypeToken<Map<String, Object>>(){}.getType();					
				Map<String, Object> m = gson.fromJson(json, type);
				
				if (null != m) mappings.add(m);
			}				
		}
		
		try 
		{
			//-- Descending with size of in_keys2recomder field for Maximum matching according to in_keys
			Collections.sort(mappings, new Comparator<Map<String, Object> > () {
				public int compare(Map<String, Object> l, Map<String, Object> r) {
					List<String> l_inKeys = (List<String>) l.getOrDefault(EnumParam2recomder.in_keys2recomder.name(), new ArrayList<String>());
					List<String> r_inKeys = (List<String>) r.getOrDefault(EnumParam2recomder.in_keys2recomder.name(), new ArrayList<String>());				
					return r_inKeys.size() - l_inKeys.size();
				}
			});			
		} catch (Exception ex) {
			VEN_LOGGER.error(ex.getMessage());
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
		}
		
		return mappings;
	}
		
	public List<Map<String, String>> getGroupMapping_inKeys2recomder (String codeName, String grpKey) {
		
		VEN_LOGGER.info("caching {},{})", codeName, grpKey);
		
		List<Map<String, String>> mappings = new ArrayList<Map<String, String>>(20);

		if (codeName == null || codeName.isEmpty() || null == grpKey || grpKey.isEmpty()) return mappings;
		
		try {
			List<Map<String, Object>> grpMappings = getGroupMapping(codeName, grpKey);
			
			for (Map<String, Object> m : grpMappings) {				
				List<String> regFields = (List<String>) m.get(EnumParam2recomder.in_keys2recomder.name());
				
				HashMap<String, String> k2r = new HashMap<String, String>(); 
				for (String regF : regFields) {					
					String regV = (String) m.get(regF);
					k2r.put(regF, regV);
				}
				
				mappings.add(k2r);
			}		
		} catch (Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		}
		
		return mappings;
	}
	
	/**
	 * Gets all mappings with Json hierarchical structure, i.e. ${traffic_type}/${group_key}/${mappings} 
	 * 
	 * @param codeName
	 * @return
	 */
	public Map<String, Map<String, List<Object>>> getAllMappings (String codeName) {
		
		VEN_LOGGER.info("getAllMappings({})", codeName);
		
		//--  ${traffic_type}/${group_key}/${mappings} 
		Map<String, Map<String, List<Object>>> trafficMaps = new HashMap<String, Map<String, List<Object>>> ();
		trafficMaps.put(EnumTrafficType.Normal.toString(), new HashMap<String, List<Object>>());
		trafficMaps.put(EnumTrafficType.Test.toString(), new HashMap<String, List<Object>>());		
		
		//--  ${group_key}/${mappings}
		Map<String, List<Object>> grpMaps = new HashMap<String, List<Object>> ();

		if (codeName == null || codeName.isEmpty()) return trafficMaps;			

		String indexName = String.format("%s%s", codeName, Constant.HERMES_INDEX_SUFFIX);

		try {
			
			QueryBuilder qb = 
					QueryBuilders.boolQuery().filter(
						QueryBuilders.termQuery(EnumParam2recomder.availability.name(), 1)
					);
			
			SearchRequestBuilder searchReq = 
					_apo.esClient()
					.prepareSearch(indexName)
					.setTypes(TYPE_NAME)
					.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
					.setQuery(qb)
					.addSort(SortBuilders.fieldSort(EnumParam2recomder.traffic_type.name()).order(SortOrder.ASC))
					.addSort(SortBuilders.fieldSort(EnumParam2recomder.group_key.name()))
					.addSort(SortBuilders.fieldSort(EnumParam2recomder.update_dt.name()).order(SortOrder.DESC))
					.setSize(Constant.DEFAULT_QUERY_SIZE);
					;

			SearchResponse resp = searchReq.execute().actionGet(Constant.TIMEOUT_SEARCH_MILLIS);

			if (0 < resp.getHits().getTotalHits()) {
				SearchHit[] hits = resp.getHits().getHits();
				Gson gson = new Gson();
				
				for (SearchHit h : hits) {				
					String json = h.getSourceAsString();
					String docId = h.getId();
					Type type = new TypeToken<Map<String, Object>>(){}.getType();					
					Map<String, Object> m = gson.fromJson(json, type);
					
					if (null != m) {
						m.put("_id", docId);
						String grpKey = (String) m.getOrDefault(EnumParam2recomder.group_key.name(), "");
						
						//--  ${group_key}/${mappings}
						if (grpMaps.containsKey(grpKey)) {
							grpMaps.get(grpKey).add(m);	
						} else {
							List<Object> l = new ArrayList<Object>(20);
							l.add(m);
							grpMaps.put(grpKey, l);
						}
					}
				}
			}
			
			for (String grpK : grpMaps.keySet()) {
				List<Object> mappings = grpMaps.get(grpK);
				
				if ( grpK.equalsIgnoreCase(EnumTrafficType.Normal.toString()) ) {
					trafficMaps.get(EnumTrafficType.Normal.toString()).put(grpK, mappings);
				}
				else {
					trafficMaps.get(EnumTrafficType.Test.toString()).put(grpK, mappings);
				}
			}
		} catch (Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		}
		
		return trafficMaps;
	}
	
	public String indexMapping (String codeName, String mappingJson) {
				
		VEN_LOGGER.info("indexMapping({},{})", codeName, mappingJson);
		
		String docID = "";
		
		if (codeName == null || codeName.isEmpty()) return docID;		

		try {
			
			String indexName = String.format("%s_hermes", codeName);
			IndexResponse indexResp = _index_doc(indexName, mappingJson);
			docID = indexResp.getId();
			
        	VEN_LOGGER.info("a mapping of {}/{} is created: {}", indexName, TYPE_NAME, mappingJson);
		} catch (Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		}
		
		return docID;
	}
	
	/**
	 * Remove all mapping from the specified $grpKey with flag clean, i.e. availability = 0
	 * 
	 * @param codeName
	 * @param grpKey
	 * @return
	 */
	public List<String> rm_group (String codeName, String grpKey) {
		
		List<String> updated_mids = new ArrayList<String>(20);
		
		if (null == codeName || codeName.isEmpty() || 
			null == grpKey || grpKey.isEmpty()) 
			return updated_mids;
		
		try 
		{					
			String indexName = String.format("%s_hermes", codeName);
			
			//-- query the mapping id(s), _id, in terms of the specified $grpKey
			SearchResponse resp = _query_group (indexName, grpKey);
			
			if (0 < resp.getHits().getTotalHits()) {
				SearchHit[] hits = resp.getHits().getHits();				
				
				for (SearchHit h : hits) {				
					String mid = h.getId();					
					
					String id = rm_mapping(codeName, mid);
					updated_mids.add(id);
				}
			}			
		} catch (Exception ex) {			
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());		
		}
		
		return updated_mids;
	}
	
	/**
	 * Remove an input specified mapping with flag cleaning, i.e. availability = 0
	 * 
	 * @param codeName
	 * @param mid The mapping id which represent by docId in ES client, i.e. _id  
	 * @return
	 */
	public String rm_mapping (String codeName, String mid) {
		
		String msg = "";
		
		if (null == codeName || codeName.isEmpty() || 
			null == mid || mid.isEmpty()) 
			return msg;
		
		try 
		{
			String indexName = String.format("%s_hermes", codeName);
			
			Map<String, Object> m = new HashMap<String, Object>();
			m.put(EnumParam2recomder.availability.name(), 0);
			m.put(EnumParam2recomder.update_dt.name(), Utility.now());			
			Gson g = new Gson();
			String updateJson = g.toJson(m); 
			
			UpdateRequest updateRequest = 
					new UpdateRequest(indexName, TYPE_NAME, mid)
			        .doc(updateJson);						
			
			UpdateResponse resp = _apo.esClient()
					.update(updateRequest)
					.get(Constant.TIMEOUT_INDEX_MILLIS, TimeUnit.MILLISECONDS);
			
			msg = resp.getId();
			
		} catch (Exception ex) {			
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
			msg = ex.getMessage();
		}
		
		return msg;
	}
		
	
	
	private IndexResponse _index_doc (String indexName, String jsonBody) {				
    	IndexResponse indexResp = _apo.esClient().prepareIndex(indexName, TYPE_NAME)
        		.setSource(jsonBody)
        		.execute()
        		.actionGet(Constant.TIMEOUT_INDEX_MILLIS);
    	
    	return indexResp;
	}
	
	private SearchResponse _query_group (String indexName, String grpKey) {
		
		QueryBuilder qb = 
				QueryBuilders.boolQuery().filter(
					QueryBuilders.termQuery(EnumParam2recomder.availability.name(), 1)
				).filter(
					QueryBuilders.termQuery(EnumParam2recomder.group_key.name(), grpKey)
				);
		
		SearchRequestBuilder searchReq = 
				_apo.esClient()
				.prepareSearch(indexName)
				.setTypes(TYPE_NAME)
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(qb)
				.setSize(Constant.DEFAULT_QUERY_SIZE)
				;

		SearchResponse resp = searchReq.execute().actionGet(Constant.TIMEOUT_SEARCH_MILLIS);
		
		return resp;
	}

	
}
