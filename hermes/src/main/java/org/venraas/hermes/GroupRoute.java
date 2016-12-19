package org.venraas.hermes;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.apollo.hermes.ConfManager;
import org.venraas.hermes.apollo.hermes.JumperManager;
import org.venraas.hermes.apollo.hermes.Param2recomderManager;
import org.venraas.hermes.common.Constant;
import org.venraas.hermes.common.EnumResetInterval;

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
	
	public RoutingGroup routing(String codeName, String clientID, String uid) {
		
		RoutingGroup rGrp = new RoutingGroup();				
		
		if (null == codeName || codeName.isEmpty() || 
			null == clientID || clientID.isEmpty()) 
		{
			VEN_LOGGER.warn("invalid codename {} or clientID {}", codeName, clientID);
			return rGrp;
		}		
							
		Param2recomderManager p2rMgr = new Param2recomderManager(); 
		List<String> grps = p2rMgr.getDistinctGroups(codeName);
		
		//-- get jumping group, if a valid jumper
		String jumpGrpKey = "";
		if (! uid.isEmpty()) {
			JumperManager jumperMgr = new JumperManager();
			jumpGrpKey = jumperMgr.get_group_key(codeName, uid);
			
			if (! jumpGrpKey.isEmpty()) {				
				Set<String> grpSet = new HashSet<String>(grps);
				
				if (! grpSet.contains(jumpGrpKey)) {
					jumpGrpKey = "";
					VEN_LOGGER.warn("{} (jumper) can't jump to group: {}, the group isn't existing", uid, jumpGrpKey);
				}
			}
		}
		
		int num_nonNormalGrps = (grps.contains(Constant.NORMAL_GROUP_KEY)) ? grps.size() - 1 : grps.size() ;
		if (0 < num_nonNormalGrps) {
			ConfManager confMgr = new ConfManager();
			double pctNormal = confMgr.get_traffic_percent_normal(codeName);
			
			//-- balance number of hashing indices between testing channels ($num_testHashIdx), and remains for normal channel
			int num_normHashIdx = (int) (pctNormal * Constant.MAX_NUM_GROUPS);
			int num_testHashIdx = (Constant.MAX_NUM_GROUPS - num_normHashIdx) / num_nonNormalGrps;
			num_normHashIdx = Constant.MAX_NUM_GROUPS - (num_testHashIdx * num_nonNormalGrps);
			
			//-- cast traffic percentage to String
			String normPCT = String.valueOf((double)num_normHashIdx/(double)Constant.MAX_NUM_GROUPS);
			String testPCT = String.valueOf((double)num_testHashIdx/(double)Constant.MAX_NUM_GROUPS);
			
			//-- A jumper
			if (! jumpGrpKey.isEmpty()) {
				rGrp.setGroup_key(jumpGrpKey);				
				
				if (jumpGrpKey.equalsIgnoreCase(Constant.NORMAL_GROUP_KEY))
				{
					rGrp.setTraffic_type(Constant.TRAFFIC_TYPE_NORMAL);
					rGrp.setTraffic_pct(normPCT);
				}
				else {
					rGrp.setTraffic_type(Constant.TRAFFIC_TYPE_TEST);
					rGrp.setTraffic_pct(testPCT);				
				}
			}
			//-- Not a jumper, i.e. normal client
			else {
				Calendar c = Calendar.getInstance();
				EnumResetInterval enumInt = confMgr.get_routing_reset_interval(codeName);
				int t_interval = c.get(enumInt.get_enumCode());
				int h = absHash(clientID, t_interval);
				int hash = (h % Constant.MAX_NUM_GROUPS) + 1;

				for (String grpKey : grps) {	
					if (grpKey.equalsIgnoreCase(Constant.NORMAL_GROUP_KEY)) {
						rGrp.setTraffic_type(Constant.TRAFFIC_TYPE_NORMAL);
						rGrp.setTraffic_pct(normPCT); 
						
						hash = hash - num_normHashIdx;
					}
					else { 
						rGrp.setTraffic_type(Constant.TRAFFIC_TYPE_TEST);
						rGrp.setTraffic_pct(testPCT);
						
						hash = hash - num_testHashIdx;
					}
					
					if (hash <= 0) {					
						rGrp.setGroup_key(grpKey);					
						break;
					}					
				}
			}
		}
		else {
			VEN_LOGGER.warn("none of Testing Group");
			VEN_LOGGER.warn("check registered Groups in terms of ES type of hermes_{}/param2recomder", codeName);			
		}
		

		return rGrp;		
	}
	
	
	
	
}
