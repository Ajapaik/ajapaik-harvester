package ee.ajapaik.platform;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

/**
 * Base class for all the clients
 * 
 * @author <a href="mailto:kaido@quest.ee?subject=AbstractClient">Kaido Kalda</a>
 */
public abstract class BaseClient implements InitializingBean {

	protected final Logger logger = Logger.getLogger(getClass());
	
	protected int timeout;
	protected String host;
	protected int port;
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getTimeout() {
		return timeout;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
}