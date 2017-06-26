package org.venraas.hermes;

import java.util.concurrent.TimeUnit;

import org.apache.http.conn.HttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.common.Constant;
import org.venraas.hermes.common.Utility;


public class ConnectionEvictionMonitor extends Thread {
	
    private final HttpClientConnectionManager connMgr;
    
    private volatile boolean shutdown = false;
    
    private static final Logger VEN_LOGGER = LoggerFactory.getLogger(ConnectionEvictionMonitor.class);
    
    
    public ConnectionEvictionMonitor(HttpClientConnectionManager connMgr) {
        super();
        this.connMgr = connMgr;
    }
        
    @Override
    public void run() {
        try {
            while (! shutdown) {
                synchronized (this) {
                    wait(Constant.CONNECTION_WAIT_TO_EVICATION_MS);

                    // Close expired connections
                    connMgr.closeExpiredConnections();

                    // Optionally, close connections
                    // that have been idle longer than 30 sec
                    connMgr.closeIdleConnections(Constant.NUM_TIMEUNIT_30, TimeUnit.SECONDS);
                    
                    VEN_LOGGER.info("eviction of connections");
                }
            }
        } catch (InterruptedException ex) {            
			VEN_LOGGER.error(ex.getMessage());
			VEN_LOGGER.error(Utility.stackTrace2string(ex));
        }
    }
    
    public void shutdown() {
        shutdown = true;
        
        synchronized (this) {
            notifyAll();
        }
    }


}
