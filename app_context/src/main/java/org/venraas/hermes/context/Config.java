package org.venraas.hermes.context;


public class Config {	
	
	int conn_timeout;
	
	int conn_fail_cond_interval;
	
	int conn_fail_cond_count;
	
	int conn_fail_resume_interval;
	
	String conn_failover_url;
		
	static Config _conf = new Config();
	
	
	private Config() { }

	static public Config getInstance() {		
		return _conf;
	}

	public int getConn_timeout() {
		return conn_timeout;
	}
	
	public void setConn_timeout(int ms) {
		this.conn_timeout = ms;
	}

	public int getConn_fail_cond_interval() {
		return conn_fail_cond_interval;
	}

	public void setConn_fail_cond_interval(int conn_fail_cond_interval) {
		this.conn_fail_cond_interval = conn_fail_cond_interval;
	}

	public int getConn_fail_cond_count() {
		return conn_fail_cond_count;
	}

	public void setConn_fail_cond_count(int conn_fail_cond_count) {
		this.conn_fail_cond_count = conn_fail_cond_count;
	}

	public String getConn_failover_url() {
		return conn_failover_url;
	}

	public void setConn_failover_url(String conn_failover_url) {
		this.conn_failover_url = conn_failover_url;
	}
	
	public int getConn_fail_resume_interval() {
		return conn_fail_resume_interval;
	}

	public void setConn_fail_resume_interval(int conn_fail_resume_interval) {
		this.conn_fail_resume_interval = conn_fail_resume_interval;
	}

//	
}
