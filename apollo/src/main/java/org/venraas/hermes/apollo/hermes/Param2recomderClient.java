package org.venraas.hermes.apollo.hermes;

import java.util.ArrayList;
import java.util.List;

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
import org.venraas.hermes.common.Utility;
import org.venraas.hermes.data_entity.Param2recomder;

import com.google.gson.Gson;

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
						
		String indexName = String.format("%s_hermes", codeName);
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
	public Param2recomder getApiURL (String codeName, String grpKey) {
		VEN_LOGGER.info("caching getApiURL({},{})", codeName, grpKey);
		
		Param2recomder c = new Param2recomder();

		if (codeName == null || codeName.isEmpty() || 
			null == grpKey || grpKey.isEmpty())
			return c;
						
		String indexName = String.format("%s_hermes", codeName);

		try {
			SearchRequestBuilder searchReq = 		
				_apo.esClient()
				.prepareSearch(indexName)
				.setTypes(TYPE_NAME)
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.termsQuery(EnumParam2recomder.group_key.name(), grpKey));

			SearchResponse resp = searchReq.execute().actionGet();

			Gson gson = new Gson();
			
			if (0 < resp.getHits().getTotalHits()) {
				SearchHit h = resp.getHits().getAt(0);				
				String json = h.getSourceAsString();
				c = gson.fromJson(json, Param2recomder.class);
			}
			
		} catch (Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		}
		
		return c;
	}

	
}
