package org.venraas.hermes;

import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.venraas.hermes.common.option.RecOption;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;


@RestController
@RequestMapping("/api")
public class HermesController {
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(HermesController.class);
	
	@CrossOrigin
	@RequestMapping(value = "/goods/rank", method = RequestMethod.GET)
	public Object get_goods_rank_GET(RecOption opt) {		
		Calendar c = Calendar.getInstance();
//TODO... check conf/
		int h = c.get(Calendar.HOUR_OF_DAY);
		
		String clientID = String.format("%s_%s_%s", opt.getToken(), opt.getVen_guid(), opt.getVen_session());
		HashFunction hf = Hashing.murmur3_128();
		HashCode hc = hf.newHasher()
		       .putString(clientID, Charsets.US_ASCII)
		       .putInt(h)
		       .hash();
		Long l = hc.asLong();
//TODO... numGrps		
		Long grp_i= Math.abs(l % 5);		
 
		return grp_i;
	}
	
	@CrossOrigin
	@RequestMapping(value = "/hello", method = RequestMethod.GET)
	public Object hello()
	{
		return "hello world";
	}
	

}
