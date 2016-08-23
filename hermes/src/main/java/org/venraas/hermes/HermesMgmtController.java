package org.venraas.hermes;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.venraas.hermes.apollo.hermes.ConfManager;
import org.venraas.hermes.apollo.raas.CompanyManager;
import org.venraas.hermes.common.EnumOptionBase;
import org.venraas.hermes.common.EnumResetInterval;


/** 
 * usage:
 *     /hermes/mgmt/set_traffic_pct_normal?token=NVHlz4elol1&pct=0.66
 */
@RestController
@RequestMapping("/mgmt")
public class HermesMgmtController {
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(HermesMgmtController.class);
	
	@CrossOrigin
	@RequestMapping(value = "/set_traffic_pct_normal", method = RequestMethod.GET)
	public String set_traffic_pct_normal_GET(String token, double pct) {		
		String msg = "";

		CompanyManager comMgr = CompanyManager.getInstance();
		String codeName = comMgr.getCodeName(token);
				
		if (codeName.isEmpty()) {
			msg = String.format("warning, invalid token \"%s\" !", token);
			VEN_LOGGER.warn(msg);
		}
		else if (pct < 0.0 || 1.0 < pct) {
			msg = String.format("warning, invalid input \"pct\" whose value should be ranged within [0.0, 1.0] !");
			VEN_LOGGER.warn(msg);
		}
		else {
			ConfManager confMgr = ConfManager.getInstance();
			confMgr.set_traffic_percent_normal(codeName, pct);
			msg = String.format("ok, %s's traffic percentage of normal channel is %s", codeName, pct);
			VEN_LOGGER.info(msg);
		}
		
		return msg;
	}
	
	@CrossOrigin
	@RequestMapping(value = "/set_routing_reset_interval", method = RequestMethod.GET)
	public String set_routing_reset_interval_GET(String token, String interval) {		
		String msg = "";
		
		CompanyManager comMgr = CompanyManager.getInstance();
		String codeName = comMgr.getCodeName(token);
		
		try {
			EnumResetInterval enumInt = EnumResetInterval.valueOf(interval);
			
			if (codeName.isEmpty()) {
				msg = String.format("warning, invalid token \"%s\" !", token);
				VEN_LOGGER.warn(msg);
			}
			else {
				ConfManager confMgr = ConfManager.getInstance();
				confMgr.set_routing_reset_interval(codeName, enumInt);
				msg = String.format("ok, %s's routing reset interval is %s", codeName, enumInt.name());
				VEN_LOGGER.info(msg);
			}
		} catch (IllegalArgumentException ex) {
			msg = String.format("warning, invalid input \"interval\": %s", ex.getMessage());
		} catch (Exception ex) {
			msg = ex.getMessage();
		}		
		
		return msg;
	}

	
	
}
