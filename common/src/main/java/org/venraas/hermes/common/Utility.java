package org.venraas.hermes.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Utility {
	
	//-- ListenableFuture - https://github.com/google/guava/wiki/ListenableFutureExplained
	public static final ListeningExecutorService CacheRefreshLES;
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(Utility.class);
	
	
	static {
		ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("cacheRefresherThread - %d").setDaemon(true).build();
		ExecutorService es = Executors.newSingleThreadExecutor(threadFactory);
		CacheRefreshLES = MoreExecutors.listeningDecorator(es);
		VEN_LOGGER.info("cache refresher thread is creaded");
	}

	static public String stackTrace2string(Exception ex) {

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);

		return sw.toString();
	}
	
	static public String now() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    	Date date = new Date();
    	
		return dateFormat.format(date);		
	}
	
	static public long duration_sec (Date beg, Date end) {
		long secs = TimeUnit.MILLISECONDS.toSeconds(end.getTime() - beg.getTime());
		return secs;		
	}

	static public <T> T json2instance(String jsonStr, Class<T> classOfT) throws JsonSyntaxException {
		Gson g = new Gson();
    	T obj = g.fromJson(jsonStr, classOfT);
    	return obj;		
	}


}
