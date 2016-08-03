package org.venraas.hermes.apollo;

import java.net.InetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.common.Utility;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

public class Apollo {
	private EnumClient _clientType = null;

	static Client _transport_client = null;
	static Client _node_client = null;

	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(Apollo.class);
	
	
	private Apollo(EnumClient clientType) { 
		_clientType = clientType;
	}
	
	public Client esClient()
	{
		Client c = _transport_client;
		return c;
	}
	
	public static Apollo getInstance() {		
		if (null == _transport_client) {
			synchronized (Apollo.class) {
				if (null == _transport_client) {
					_transport_client = _init_transport();
				}
			}
		}				
		
		return new Apollo(EnumClient.Transport);
	}
 
	private static Client _init_transport() {
		//TODO load setting from configuration file
		
		Client client = null;
		
		try
		{			
	        Settings settings = Settings.settingsBuilder()
	        		.put("client.transport.sniff", true)
	            	.put("cluster.name", "hermes-cluster")
	            	.build();	       	        
	        	        
	        String hostname = InetAddress.getLocalHost().getHostName();
	        client = TransportClient.builder().settings(settings).build()
					.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostname), 9300));
	        
						
		} catch(Exception ex) {
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
			VEN_LOGGER.error(ex.getMessage());
		}
   
        return client;
	}
	

}
