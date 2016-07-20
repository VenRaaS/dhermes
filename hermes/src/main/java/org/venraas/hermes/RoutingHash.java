package org.venraas.hermes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class RoutingHash {
	
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(RoutingHash.class);
	
	RoutingHash(){}
	
	public long hash(String putString, int putInt) {
		
		HashFunction hf = Hashing.murmur3_128();
		HashCode hc = hf.newHasher()
		       .putString(putString, Charsets.US_ASCII)
		       .putInt(putInt)
		       .hash();
		Long l = hc.asLong();
		
		return l;
	}
	
	
	
	
}
