package org.venraas.hermes.common;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptionUtility {
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(OptionUtility.class);
	
	public OptionUtility() {}
	
	public Map<String, Object> cp_cc2ven(Map<String, Object> inParamMap) {
		
		try {
			String ven_guid = (String) inParamMap.get(EnumOptionBase.ven_guid.name());
			String cc_guid = (String) inParamMap.get(EnumOptionBase.cc_guid.name());
			ven_guid = (null != ven_guid) ? ven_guid.trim() : ven_guid;
			cc_guid = (null != cc_guid) ? cc_guid.trim() : cc_guid;
			
			if (null == ven_guid || ven_guid.isEmpty()) {
				if (null != cc_guid && ! cc_guid.isEmpty()) {
					inParamMap.put(EnumOptionBase.ven_guid.name(), cc_guid);
				}
			}
			
			String ven_session = (String) inParamMap.get(EnumOptionBase.ven_session.name());
			String cc_session = (String) inParamMap.get(EnumOptionBase.cc_session.name());
			ven_session = (null != ven_session) ? ven_session.trim() : ven_session;
			cc_session = (null != cc_session) ? cc_session.trim() : cc_session;
			
			if (null == ven_session || ven_session.isEmpty()) {
				if (null != cc_session && ! cc_session.isEmpty()) {
					inParamMap.put(EnumOptionBase.ven_session.name(), cc_session);
				}
			}
		}
		catch (Exception ex) {
			VEN_LOGGER.error(ex.getMessage());
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
		}
		
		return inParamMap;
	}
	

}
