package org.venraas.hermes;

import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.apollo.hermes.ConfClient;
import org.venraas.hermes.apollo.hermes.ConfManager;
import org.venraas.hermes.apollo.hermes.Param2recomderClient;
import org.venraas.hermes.apollo.hermes.Param2recomderManager;
import org.venraas.hermes.common.Constant;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class GroupRoute {
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(GroupRoute.class);
			
	public GroupRoute(){}

	public int absHash(String putString, int putInt) {
		return Math.abs(hash(putString, putInt));
	}

	public int hash(String putString, int putInt) {
		
		HashFunction hf = Hashing.murmur3_128();
		
		HashCode hc = hf.newHasher()
		       .putString(putString, Charsets.US_ASCII)
		       .putInt(putInt)
		       .hash();
		
		int l = hc.asInt();
		
		return l;
	}
	
	public String routing(String codeName, String clientID) {
		
		String groupKey = Constant.NORMAL_GROUP_KEY;
		
		if (null == codeName || codeName.isEmpty() || null == clientID || clientID.isEmpty())
			return groupKey;

		Param2recomderManager p2rMgr = Param2recomderManager.getInstance(); 
		List<String> grps = p2rMgr.getDistinctGroups(codeName);	
		
		int num_testGrps = grps.size() - 1;
		if (0 < num_testGrps) {
			
			ConfManager confMgr = ConfManager.getInstance();
			double pctNormal = confMgr.get_traffic_percent_normal(codeName);
			
			//-- balance number of testing channel $hash_i, remains for normal channel
			int num_normHashIdx = (int) (pctNormal * Constant.MAX_NUM_GROUPS);
			int num_testHashIdx = (Constant.MAX_NUM_GROUPS - num_normHashIdx) / num_testGrps;
			num_normHashIdx = Constant.MAX_NUM_GROUPS - (num_testHashIdx * num_testGrps);

			Calendar c = Calendar.getInstance();
			int resetInterval = confMgr.get_routing_reset_interval(codeName);
			int t = c.get(resetInterval);
			int h = absHash(clientID, t);
			int hash = (h % Constant.MAX_NUM_GROUPS) + 1;

			int num_regGrps = grps.size();
			for (int i = 0; i < num_regGrps; ++i) {				
				String grpKey = grps.get(i).trim();				

				if (grpKey.equalsIgnoreCase(Constant.NORMAL_GROUP_KEY)) 
					hash = hash - num_normHashIdx;									
				else 
					hash = hash - num_testHashIdx;
				
				if (hash <= 0) {
					groupKey = grpKey;
					break;
				}					
			}
		}
		else {
			VEN_LOGGER.warn("none of Testing Group");
			VEN_LOGGER.warn("check ES type of hermes_{}/param2recomder", codeName);			
		}		

		return groupKey;		
	}
	
	
	
	
}
