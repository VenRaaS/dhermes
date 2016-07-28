package org.venraas.hermes;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.venraas.hermes.apollo.hermes.Param2recomderClient;
import org.venraas.hermes.apollo.mappings.EnumParam2recomder;
import org.venraas.hermes.apollo.raas.CompanyClient;
import org.venraas.hermes.common.EnumOptionBase;
import org.venraas.hermes.common.option.RecOption;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


@RestController
@RequestMapping("/api")
public class HermesController {
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(HermesController.class);
	
	@CrossOrigin
	@RequestMapping(value = "/goods/rank", method = RequestMethod.GET)
	public Object get_goods_rank_GET(@RequestParam Map<String, Object> paramMap) {		
									
		String clientID = String.format("%s_%s_%s", 
				paramMap.get(EnumOptionBase.token.name()), 
				paramMap.get(EnumOptionBase.ven_guid.name()), 
				paramMap.get(EnumOptionBase.ven_session.name()));

		CompanyClient comClient = new CompanyClient();
		String token = (String)paramMap.get(EnumOptionBase.token.name());
		String codeName = comClient.getCodeName(token);
						
		GroupRoute gr = new GroupRoute();
		String grpKey = gr.routing(codeName, clientID);
		
		Map<String, Object> mapping = null;
		
		Param2recomderClient p2r = new Param2recomderClient();
		List<Map<String, Object>> maps = p2r.getGroupMapping(codeName, grpKey);		
		for (Map<String, Object> m : maps) {			
			boolean matchAllKeys = true;
			
			List<String> fields = (List<String>) m.get(EnumParam2recomder.keys2recomder.name());						
			for (String f : fields) {
				String inputV = (String) paramMap.get(f);
				String regV = (String) m.get(f);
				
				if (null == inputV || null == regV || 
					inputV.isEmpty() || regV.isEmpty() || 
					!inputV.equals(regV)) 
				{
					matchAllKeys = false;
					break;
				}
			}
			
			if (matchAllKeys) {
				mapping = m;
				break;
			} 
		}
 
		return mapping;
	}
	
	@CrossOrigin
	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public Map<String, Object> hello(@RequestParam Map<String, Object> m)
	{		
		return m;
	}
	
	@CrossOrigin
	@RequestMapping(value = "/hello", method = RequestMethod.POST)	
	public Map<String, Object> hello(@RequestBody String jsonStr)
	{
		Gson gson = new Gson();
		Type type = new TypeToken<Map<String, Object>>(){}.getType();
		Map<String, Object> m = gson.fromJson(jsonStr, type);

//		Map<String, String> m = gson.fromJson(s, Map);

		return m;
	}
	

	
	

}
