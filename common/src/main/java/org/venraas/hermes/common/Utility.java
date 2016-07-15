package org.venraas.hermes.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utility {
	private static final Logger VEN_LOGGER = LoggerFactory.getLogger(Utility.class);

	static public String stackTrace2string(Exception ex) {

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);

		return sw.toString();
	}
}
