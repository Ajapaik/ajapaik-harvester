package ee.ajapaik.util;

import org.apache.log4j.Logger;

public class Tracer {
	
	private static final Logger logger = Logger.getLogger(Tracer.class);
	
	public static void trace() {
		try {
			throw new Exception();
		} catch (Exception e) {
			logger.trace("Tracing stack:", e);
		}
	}
}
