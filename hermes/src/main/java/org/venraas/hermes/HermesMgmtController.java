package org.venraas.hermes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.venraas.hermes.apollo.hermes.ConfManager;
import org.venraas.hermes.apollo.hermes.JumperManager;
import org.venraas.hermes.apollo.hermes.Param2recomderManager;
import org.venraas.hermes.apollo.mappings.EnumConf;
import org.venraas.hermes.apollo.raas.CompanyManager;
import org.venraas.hermes.common.Constant;
import org.venraas.hermes.common.ConstantMsg;
import org.venraas.hermes.common.EnumResetInterval;
import org.venraas.hermes.common.ValidDocID;
import org.venraas.hermes.common.ValidGroupKey;
import org.venraas.hermes.common.ValidToken;
import org.venraas.hermes.common.ValidUID;


@RestController
@RequestMapping("/mgmt")
public class HermesMgmtController {
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(HermesMgmtController.class);

	/** 
	 * usage:
	 *     /hermes/mgmt/ls_forward_headers?token=${token}     
	 */	
	@CrossOrigin
	@RequestMapping(value = "/ls_forward_headers", method = RequestMethod.GET)
	public String ls_http_forward_headers(@Valid ValidToken vt) {		
		String msg = "";
		
		String token = vt.getToken();
		CompanyManager comMgr = CompanyManager.getInstance();
		String codeName = comMgr.getCodeName(token);
				
		try {
			if (codeName.isEmpty()) {
				msg = String.format(ConstantMsg.INVALID_TOKEN, token);
				throw new IllegalArgumentException(msg);
			}
			
			ConfManager confMgr = ConfManager.getInstance();
			List<String> headers = confMgr.get_http_forward_headers(codeName);
			msg = String.format("ok, \"%s\" : [%s]", EnumConf.http_forward_headers.name(), String.join(",", headers));			
		} catch (Exception ex) {
			msg = ex.getMessage();
			VEN_LOGGER.error(msg);
		}
		
		return msg;
	}
	
	/** 
	 * usage:
	 *     /hermes/mgmt/add_forward_headers?token=${token}&json=["Cookie"]
	 */	
	@CrossOrigin
	@RequestMapping(value = "/add_forward_headers", method = RequestMethod.GET)
	public String add_http_forward_headers(@Valid ValidToken vt, String json) {		
		String msg = "";
		
		String token = vt.getToken();
		CompanyManager comMgr = CompanyManager.getInstance();
		String codeName = comMgr.getCodeName(token);
				
		try {
			if (codeName.isEmpty()) {
				msg = String.format(ConstantMsg.INVALID_TOKEN, token);
				throw new IllegalArgumentException(msg);
			}
			
			ConfManager confMgr = ConfManager.getInstance();
			List<String> updateHeaders = confMgr.add_http_forward_headers(codeName, json);
			msg = (updateHeaders.isEmpty()) 
					? String.format("Invalid, Null or Empty input \"%s\" for \"%s\" setting", json, EnumConf.http_forward_headers.name())
					: String.format("ok, update \"%s\" with [%s]", EnumConf.http_forward_headers.name(), String.join(",", updateHeaders));
			
		} catch (Exception ex) {
			msg = ex.getMessage();
			VEN_LOGGER.error(msg);
		}
		
		return msg;
	}
	
	/** 
	 * usage:
	 *     /hermes/mgmt/set_forward_headers?token=${token}&json=["Referer"]
	 */
	@CrossOrigin
	@RequestMapping(value = "/set_forward_headers", method = RequestMethod.GET)
	public String set_http_forward_headers(@Valid ValidToken vt, String json) {		
		String msg = "";
		
		String token = vt.getToken();
		CompanyManager comMgr = CompanyManager.getInstance();
		String codeName = comMgr.getCodeName(token);
				
		try {
			if (codeName.isEmpty()) {
				msg = String.format(ConstantMsg.INVALID_TOKEN, token);
				throw new IllegalArgumentException(msg);
			}
			
			ConfManager confMgr = ConfManager.getInstance();
			boolean isSuccess = confMgr.set_http_forward_headers(codeName, json);
			msg = (isSuccess) 
					? String.format("ok, update \"%s\" with %s", EnumConf.http_forward_headers.name(), String.join(",", json))
					: String.format("Invalid or Null input \"%s\" for \"%s\" setting", json, EnumConf.http_forward_headers.name());
			
		} catch (Exception ex) {
			msg = ex.getMessage();
			VEN_LOGGER.error(msg);					
		}
		
		return msg;
	}
	
	/** 
	 * usage:
	 *     /hermes/mgmt/set_traffic_pct_normal?token=&pct=0.66
	 */	
	@CrossOrigin
	@RequestMapping(value = "/set_traffic_pct_normal", method = RequestMethod.GET)
	public String set_traffic_pct_normal_GET(@Valid ValidToken vt, double pct) {		
		String msg = "";

		String token = vt.getToken();
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
	public String set_routing_reset_interval_GET(@Valid ValidToken vt, String interval) {		
		String msg = "";
		
		String token = vt.getToken();
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
	
	/** 
	 * usage:
	 *     /hermes/mgmt/set_jumper?token=&uid=u0806449&grpkey=test-1
	 */
	@CrossOrigin
	@RequestMapping(value = "/set_jumper", method = RequestMethod.GET)
	public String set_jumper(@Valid ValidToken vt, @Valid ValidUID vu, @Valid ValidGroupKey vGK) {		
		String msg = "";
		
		String token = vt.getToken();
		CompanyManager comMgr = CompanyManager.getInstance();
		String codeName = comMgr.getCodeName(token);
						
		try {
			if (codeName.isEmpty()) {
				msg = String.format(ConstantMsg.INVALID_TOKEN, token);
				throw new IllegalArgumentException(msg);
			}
			
			String uid = vu.getUid();
			if (null == uid || uid.isEmpty()) {
				msg = String.format(ConstantMsg.INVALID_INPUT_PARAMETER, "uid");
				throw new IllegalArgumentException(msg);
			}
			
			String grpkey = vGK.getGrpkey();
			Param2recomderManager p2rMgr = Param2recomderManager.getInstance();
			List<String> grps = p2rMgr.getDistinctGroups(codeName, true);
			if ( ! grps.contains(grpkey)) {
				msg = String.format("Warning, Invalid group key \"%s\", which isn't an available group! <br>"
						+ "<small>Notice, please wait a moment and try again latter if you did register just now.</small>", grpkey);
				throw new IllegalArgumentException(msg);
			}
			
			JumperManager jumperMgr = JumperManager.getInstance();			
			boolean isSuccess = jumperMgr.set_jumper(codeName, uid, grpkey);
			msg = (isSuccess) ? 
					String.format("Ok, %s is jumping to group %s", uid, grpkey) : 
					String.format("Warning, there's some problem during jumping, please check input parameters !") ;
						
			VEN_LOGGER.info(msg);
		} catch (Exception ex) {
			msg = ex.getMessage();
			VEN_LOGGER.error(msg);
		}
		
		return msg;
	}
	
	/** 
	 * usage:
	 *     /hermes/mgmt/set_jumper_guid?token=&ven_guid=d0200dfe-3592-40fa-a25b-0c1804247fee.api-group-n9t320150813&grpkey=test-1
	 */
	@CrossOrigin
	@RequestMapping(value = "/set_jumper_guid", method = RequestMethod.GET)
	public String set_jumper_guid(@Valid ValidToken vt, @Valid ValidUID vu, @Valid ValidGroupKey vGK) {
		String msg ="";
		
		try {
			String ven_guid = vu.getVen_guid();
			if (null == ven_guid || ven_guid.isEmpty()) {
				msg = String.format(ConstantMsg.INVALID_INPUT_PARAMETER, "ven_guid");
				throw new IllegalArgumentException(msg);
			}
			vu.setUid(ven_guid);
			
			msg = set_jumper(vt, vu, vGK);	
		} catch (Exception ex) {
			msg = ex.getMessage();
			VEN_LOGGER.error(msg);
		}
		
		return msg;				
	}
	
	
	/** 
	 * usage:
	 * 		/hermes/mgmt/register_normal?token=&json=
		    	{
					"rec_pos":"categTop",
				    "rec_code":"ClickStream",
				    "rec_type":"cs",
				    "api_url":[
				        "http://140.96.83.32:8080/cupid/api/goods/rank"
				    ],
				    "in_keys2recomder":[
				        "rec_pos"
				    ],
				    "out_aux_params":[
				        "rec_code",
				        "rec_type"
				    ]
				}
	 *
	 * @param token
	 * @param json
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/register_normal", method = RequestMethod.GET)
	public String register_normal(@Valid ValidToken vt, String json) {		
		String msg = "";
		
		String token = vt.getToken();
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
		
	/**
	 * usage:
	 * 		/hermes/mgmt/register_test?token=&json=
				{
				    "group_key":"test-1",
				    "rec_pos":"categTop",
				    "rec_code":"ClickStream",
				    "rec_type":"cs",
				    "api_url":[
				        "http://140.96.83.32:8080/cupid/api/goods/rank"
				    ],
				    "in_keys2recomder":[
				        "rec_pos"
				    ],
				    "out_aux_params":[
				        "rec_code",
				        "rec_type"
				    ]
				}
	 *
	 * @param token
	 * @param json
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/register_test", method = RequestMethod.GET)
	public String register_test(@Valid ValidToken vt, String json) {		
		String msg = "";
		
		String token = vt.getToken();
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
	
	/**
	 * usage:
	 * 		/hermes/mgmt/ls_grp?token=
     *
	 * @param token
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/ls_grp", method = RequestMethod.GET)
	public Object ls_grp(@Valid ValidToken vt) {
		
		Map<String, Map<String, List<Object>>> mappings = new HashMap<String, Map<String, List<Object>>> ();
		String msg ="";
		
		String token = vt.getToken();
		CompanyManager comMgr = CompanyManager.getInstance();
		String codeName = comMgr.getCodeName(token);
		
		try {
			if (codeName.isEmpty()) {
				msg = String.format(ConstantMsg.INVALID_TOKEN, token);
				throw new IllegalArgumentException(msg);
			}
			
			Param2recomderManager p2rMgr = Param2recomderManager.getInstance();			
			mappings = p2rMgr.ls_grp (codeName);
			
		} catch (Exception ex) {
			msg = ex.getMessage();
			VEN_LOGGER.error(msg);
		}
		
		return (0 < mappings.size()) ? mappings : msg;
	}
	
	/**
	 * usage:
	 * 		/hermes/mgmt/rm_grp?token=&key=${group_key}
     *
	 * @param token
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/rm_grp", method = RequestMethod.DELETE)
	public String rm_group(@Valid ValidToken vt, @Valid ValidGroupKey vGK) {
		String msg ="";
		
		String token = vt.getToken();
		CompanyManager comMgr = CompanyManager.getInstance();
		String codeName = comMgr.getCodeName(token);
		
		try {
			if (codeName.isEmpty()) {
				msg = String.format(ConstantMsg.INVALID_TOKEN, token);
				throw new IllegalArgumentException(msg);
			}
			
			String key = vGK.getGrpkey();
			if (null == key || key.isEmpty()) {
				msg = String.format(ConstantMsg.INVALID_INPUT_PARAMETER, "key");
				throw new IllegalArgumentException(msg);
			}

			Param2recomderManager p2rMgr = Param2recomderManager.getInstance();			
			List<String> update_ids = p2rMgr.rm_group(codeName, key);
			
			msg = (0 < update_ids.size()) ? 
					String.format("ok, all group mapping: \"%s\" are Unavailable now.", key) :
					String.format("warn, group: \"%s\" isn't available!", key);
			
		} catch (Exception ex) {
			msg = ex.getMessage();
			VEN_LOGGER.error(msg);
		}
		
		return msg;
	}
	
	/**
	 * usage:
	 * 		/hermes/mgmt/rm_mapping?token=&mid=${_id}
	 * 
	 * @param token
	 * @param mid
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/rm_mapping", method = RequestMethod.DELETE)
	public String rm_mapping(@Valid ValidToken vt, @Valid ValidDocID vID) {
		String msg ="";
		
		String token = vt.getToken();
		CompanyManager comMgr = CompanyManager.getInstance();
		String codeName = comMgr.getCodeName(token);
		
		try {
			if (codeName.isEmpty()) {
				msg = String.format(ConstantMsg.INVALID_TOKEN, token);
				throw new IllegalArgumentException(msg);
			}
			
			String mid = vID.getMid();
			if (null == mid || mid.isEmpty()) {
				msg = String.format(ConstantMsg.INVALID_INPUT_PARAMETER, "mid");
				throw new IllegalArgumentException(msg);
			}
			
			Param2recomderManager p2rMgr = Param2recomderManager.getInstance();			
			String id = p2rMgr.rm_mapping(codeName, mid);
			
			msg = String.format("ok, mapping:%s has been Unavailable.", id);
			
		} catch (Exception ex) {
			msg = ex.getMessage();
			VEN_LOGGER.error(msg);
		}
		
		return msg;
	}

	
	
	
}
