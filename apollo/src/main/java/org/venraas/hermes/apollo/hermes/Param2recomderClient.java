package org.venraas.hermes.apollo.hermes;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.apollo.Apollo;
import org.venraas.hermes.apollo.mappings.EnumParam2recomder;
import org.venraas.hermes.common.Constant;
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
	
//	@CacheEvict(value="cache_category", allEntries=true)
	public void reset() { }
	
	public List<String> getDistinctGroups (String codeName) {
		VEN_LOGGER.info("caching getGroups({})", codeName);

		List<String> grps = new ArrayList<String> (20);
		
		if (codeName == null || codeName.isEmpty())	return grps;
						
		String indexName = String.format("%s%s", codeName, Constant.HERMES_INDEX_SUFFIX);
		String aggName = "group_key_count";

		try {
			
			//-- availability = 1 
			QueryBuilder qb = QueryBuilders.boolQuery().filter(QueryBuilders.termQuery(EnumParam2recomder.availability.name(), 1));
			
			//-- Terms Aggregation, https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/_bucket_aggregations.html#java-aggs-bucket-terms
			AggregationBuilder ab = AggregationBuilders.terms(aggName).field(EnumParam2recomder.group_key.name());

			SearchRequestBuilder searchReq = 		
				_apo.esClient()
				.prepareSearch(indexName)
				.setTypes(TYPE_NAME)
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(qb)
				.addAggregation(ab)
				.setSize(0);

			SearchResponse resp = searchReq.execute().actionGet();

			Gson gson = new Gson();
			
			if (0 < resp.getHits().getTotalHits()) {
				Terms agg = resp.getAggregations().get(aggName);
				for (Terms.Bucket b : agg.getBuckets()) {
					String k = b.getKeyAsString();
//					long c = b.getDocCount();
					grps.add(k);
				}
			}
			
		} catch (Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		}
		
		return grps;
	}

//	@Cacheable(value="cache_category", key="{#codeName, #categoryCode}")
	public List<Map<String, Object>> getGroupMapping (String codeName, String grpKey) {
		
		VEN_LOGGER.info("caching getGroupMapping({},{})", codeName, grpKey);
		
		List<Map<String, Object>> mappings = new ArrayList<Map<String, Object>>(20);		

		if (codeName == null || codeName.isEmpty() || null == grpKey || grpKey.isEmpty()) return mappings;			

		String indexName = String.format("%s%s", codeName, Constant.HERMES_INDEX_SUFFIX);

		try {
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
					.setQuery(qb);

			SearchResponse resp = searchReq.execute().actionGet();
						
			if (0 < resp.getHits().getTotalHits()) {
				SearchHit[] hits = resp.getHits().getHits();
				Gson gson = new Gson();
				
				for (SearchHit h : hits) {				
					String json = h.getSourceAsString();			
					Type type = new TypeToken<Map<String, Object>>(){}.getType();					
					Map<String, Object> m = gson.fromJson(json, type);
					mappings.add(m);
				}				
			}
		} catch (Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		}
		
		return mappings;
	}

	
}
