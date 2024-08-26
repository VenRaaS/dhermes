package org.venraas.hermes.apollo.raas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.apollo.mappings.Com_pkgs;
import org.venraas.hermes.common.Constant;
import org.venraas.hermes.common.Utility;
import org.venraas.hermes.context.Config;
import java.net.InetAddress;
import java.util.Base64;

import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.conn.ConnectTimeoutException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class CompanyClient {

    private static final String VENRAAS_INDEX_NAME = "venraas";
    
    static private final String TYPE_NAME = "com_pkgs"; 
    
    private static final Logger VEN_LOGGER = LoggerFactory.getLogger(CompanyClient.class);    
       
    
    public CompanyClient() { 
    	
    }

    public String getCodeName(String token) throws Exception {
        VEN_LOGGER.info("caching getCodeName({})", token);
        
        String codeName = "";
        
        if (null == token || token.isEmpty()) return codeName;

        try {            

    		String username = "elastic";
    		String password = System.getenv("ES_COMP_PWD");
    		String auth = username + ":" + password;
    		String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
    		String authHeader = "Basic " + encodedAuth;

            String qUri = "http://es-comp.venraas.private:9200/venraas_com_pkgs/_search?q=*&sort=update_dt:desc&size=1";

    		Content content = Request.Get(qUri)
    				.connectTimeout((int)Constant.TIMEOUT_SEARCH_MILLIS)
    				.socketTimeout(Constant.TIMEOUT_SEARCH_MILLIS)
    				.setHeader("Authorization", authHeader)
    				.execute()
    				.returnContent();
    		String jsonStr = content.asString();

            JsonParser jp = new JsonParser();
            JsonObject obj = jp.parse(jsonStr).getAsJsonObject();
            JsonArray comps = obj.getAsJsonObject("hits")
                    .getAsJsonArray("hits")
                    .get(0)                    
                    .getAsJsonObject()
                    .getAsJsonObject("_source")
                    .getAsJsonArray(Com_pkgs.companies);

           for (JsonElement v : comps) {
               JsonObject c = (JsonObject)v;
               JsonElement je = c.get(Com_pkgs.token);
               if (null == je) 
            	   continue;

               String tok = je.getAsString();               
               if (0 == tok.compareToIgnoreCase(token)) {
                   codeName = c.get(Com_pkgs.code_name).getAsString();
                   break;
               }
           }
        }
        catch (ConnectTimeoutException ex) {
        	throw new ConnectTimeoutException("propagates timeout exception to reuse catch value");        	
        }
        catch (Exception ex) {        	
            VEN_LOGGER.error(Utility.stackTrace2string(ex));
            VEN_LOGGER.error(ex.getMessage());
        }              
        
        return codeName;
    }


}
