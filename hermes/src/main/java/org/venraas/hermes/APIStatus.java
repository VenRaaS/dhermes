package org.venraas.hermes;

import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.venraas.hermes.common.Utility;
import org.venraas.hermes.context.Config;

import com.google.common.io.CharStreams;


class APIStatus {
	
	String apiURL = "";
	
    boolean suspending = false;
	
	Date connFailBeg_dt = new Date();
	
	Date suspendBeg_dt = new Date();
	
	AtomicInteger cnt_connFail = new AtomicInteger(0);
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(APIStatus.class);
	
	
	public APIStatus() {}
	
	public APIStatus(String url) {
		apiURL = url;
	}
	
	public boolean isSuspending() {
		
		if (suspending) {
			
			synchronized (this) {
				
				if (suspending) {								
					long suspendPeriod = getSuspendingDurationSec();					
					Config conf = Config.getInstance();
					
					if (conf.getConn_fail_resume_interval() < suspendPeriod) {
						VEN_LOGGER.info("try to resume API: {} after suspending in {} seconds.", apiURL, suspendPeriod);
						
						try {
							URL url = new URL(apiURL);								
							String result = CharStreams.toString(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));								
							clearSuspending();
							
							VEN_LOGGER.info("resume API: {} successfully after suspending in {} seconds.", apiURL, suspendPeriod);
						} catch (Exception ex) {								
							setSuspending();
							VEN_LOGGER.info("resume API: {} fail.", apiURL);
							
							VEN_LOGGER.error(ex.getMessage());
							VEN_LOGGER.error(Utility.stackTrace2string(ex));
						}	
					}
				}
			}
		}
		
		return suspending;
	}		

	public void setSuspending() {			
		synchronized (this) {					
			suspending = true;
			suspendBeg_dt = new Date();
			
		}			
	}
	
	public void clearSuspending() {			
		synchronized (this) {				
			suspending = false;
			cnt_connFail.set(0);				
		}
	}
	
	public long getSuspendingDurationSec() {
		synchronized (this) {
			return Utility.duration_sec(suspendBeg_dt, new Date());
		}
	}
	
	public int failIncrementAndGet() {
		int failCnt = cnt_connFail.get();
		
		if (! suspending) {
			synchronized (this) {
				if (! suspending) {
					failCnt = cnt_connFail.incrementAndGet();
					if (1 == failCnt) connFailBeg_dt = new Date();						
				}
			}
		}
		
		return failCnt;
	}
	
	public void clearConnFailCnt() {
		synchronized (this) {
			cnt_connFail.set(0);
		}			
	}
	
	public long getConnFailDurationSec() {
		synchronized (this) {
			long dursec = Utility.duration_sec(connFailBeg_dt, new Date());
			return dursec;
		}
	}
	
	
}
