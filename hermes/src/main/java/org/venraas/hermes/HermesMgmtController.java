package org.venraas.hermes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.venraas.hermes.apollo.hermes.ConfManager;
import org.venraas.hermes.apollo.raas.CompanyManager;
import org.venraas.hermes.common.Constant;
import org.venraas.hermes.common.ConstantMsg;
import org.venraas.hermes.common.EnumResetInterval;


@RestController
@RequestMapping("/mgmt")
public class HermesMgmtController {
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(HermesMgmtController.class);
	
	/** 
	 * usage:
	 *     /hermes/mgmt/set_traffic_pct_normal?token=&pct=0.66
	 */	
	@CrossOrigin
	@RequestMapping(value = "/set_traffic_pct_normal", method = RequestMethod.GET)
	public String set_traffic_pct_normal_GET(String token, double pct) {		
		String msg = "";

		CompanyManager comMgr = CompanyManager.getInstance();
		String codeName = comMgr.getCodeName(token);
		
		try {
			if (codeName.isEmpty()) {
				msg = String.format(ConstantMsg.INVALID_TOKEN, token);
				throw new IllegalArgumentException(msg);
			}
			else if (pct < 0.0 || 1.0 < pct) {
				msg = String.format("Invalid input \"pct\" whose value should be ranged within [0.0, 1.0] !");
				throw new IllegalArgumentException(msg);
			}
			else {
				ConfManager confMgr = ConfManager.getInstance();
				confMgr.set_traffic_percent_normal(codeName, pct);
				msg = String.format("ok, %s's traffic percentage of normal channel is %s", codeName, pct);
				VEN_LOGGER.info(msg);
			}
		} catch (Exception ex) {
			msg = ex.getMessage();
			VEN_LOGGER.error(msg);
		}
		
		return msg;
	}
	
	/** 
	 * usage:
	 *     /hermes/mgmt/set_routing_reset_interval?token=&interval=HOUR
	 */
	@CrossOrigin
	@RequestMapping(value = "/set_routing_reset_interval", method = RequestMethod.GET)
	public String set_routing_reset_interval_GET(String token, String interval) {		
		String msg = "";
		
		CompanyManager comMgr = CompanyManager.getInstance();
		String codeName = comMgr.getCodeName(token);
						
		try {
			if (codeName.isEmpty()) {
				msg = String.format(ConstantMsg.INVALID_TOKEN, token);
				throw new IllegalArgumentException(msg);
			}
			
			EnumResetInterval enumInt = EnumResetInterval.valueOf(interval);

			ConfManager confMgr = ConfManager.getInstance();
			confMgr.set_routing_reset_interval(codeName, enumInt);
			msg = String.format("ok, %s's routing reset interval is %s", codeName, enumInt.name());
			VEN_LOGGER.info(msg);

		} catch (Exception ex) {
			msg = ex.getMessage();
			VEN_LOGGER.error(msg);
		}
		
		return msg;
	}
	
	@CrossOrigin
	@RequestMapping(value = "/register_normal", method = RequestMethod.GET)
	public String register_normal(String token, String json) {		
		String msg = "";
		
		CompanyManager comMgr = CompanyManager.getInstance();
		String codeName = comMgr.getCodeName(token);
				
		try {
			if (codeName.isEmpty()) {
				msg = String.format(ConstantMsg.INVALID_TOKEN, token);
				throw new IllegalArgumentException(msg);
			}
			
			Param2RestAPI p2api = new Param2RestAPI();
			msg = p2api.regsiterMapping(codeName, Constant.TRAFFIC_TYPE_NORMAL, json);
		} catch (Exception ex) {
			msg = ex.getMessage();
			VEN_LOGGER.error(msg);
		}		
		
		return msg;
	}
	
	@CrossOrigin
	@RequestMapping(value = "/register_test", method = RequestMethod.GET)
	public String register_test(String token, String json) {		
		String msg = "";
		
		CompanyManager comMgr = CompanyManager.getInstance();
		String codeName = comMgr.getCodeName(token);
				
		try {
			if (codeName.isEmpty()) {
				msg = String.format(ConstantMsg.INVALID_TOKEN, token);
				throw new IllegalArgumentException(msg);
			}
			
			Param2RestAPI p2api = new Param2RestAPI();
			msg = p2api.regsiterMapping(codeName, Constant.TRAFFIC_TYPE_TEST, json);
		} catch (Exception ex) {
			msg = ex.getMessage();
			VEN_LOGGER.error(msg);
		}		
		
		return msg;
	}

	
	
}
