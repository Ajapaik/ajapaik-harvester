package ee.ajapaik.axis.service;

import static org.apache.axis2.transport.http.HTTPConstants.CACHED_HTTP_CLIENT;
import static org.apache.axis2.transport.http.HTTPConstants.CHUNKED;
import static org.apache.axis2.transport.http.HTTPConstants.MC_ACCEPT_GZIP;
import static org.apache.axis2.transport.http.HTTPConstants.MC_GZIP_REQUEST;
import static org.apache.axis2.transport.http.HTTPConstants.REUSE_HTTP_CLIENT;
import static org.apache.axis2.transport.http.HTTPConstants.USER_AGENT;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.MessageContextListener;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.http.impl.httpclient4.HTTPClient4TransportSender;
import org.apache.http.params.CoreProtocolPNames;

import ee.ajapaik.platform.BaseHttpClient;

/**
 * Abstract base class to all SOAP clients
 * 
 * @author <a href="mailto:kaido@quest.ee?subject=AbstractSOAPClient">Kaido Kalda</a>
 */
public abstract class AbstractSOAPClient<T extends Stub> extends BaseHttpClient {

	protected T service;
	protected String endpoint;
	protected boolean gzipRequest;
	protected boolean acceptGzip;
	protected boolean chunked;
	
	public void setService(T service) {
		this.service = service;
	}

	public void setGzipRequest(boolean gzipRequest) {
		this.gzipRequest = gzipRequest;
	}

	public void setAcceptGzip(boolean acceptGzip) {
		this.acceptGzip = acceptGzip;
	}

	public void setChunked(boolean chunked) {
		this.chunked = chunked;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	private void configureEndpoint() {
		if(this.endpoint != null) {
			try {
				URL url = new URL(this.endpoint);
				
				this.host = url.getHost();
				this.protocol = url.getProtocol();
				this.port = url.getPort() != -1 ? 
						url.getPort() : 
						(protocol.equals("http") ? 80 : 443);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		configureEndpoint();
		
		super.afterPropertiesSet();
		
		ConfigurationContext context = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
				
		this.service = getService(context, endpoint);
		configureService(context);
	}

	private void configureService(ConfigurationContext context) throws Exception {
		Options options = service._getServiceClient().getOptions();
		options.setProperty(REUSE_HTTP_CLIENT, Boolean.TRUE);
		options.setProperty(CACHED_HTTP_CLIENT, httpClient);
		// GZIP requests
		options.setProperty(MC_GZIP_REQUEST, Boolean.valueOf(gzipRequest));
		// Allow GZIPPED responses
		options.setProperty(MC_ACCEPT_GZIP, Boolean.valueOf(acceptGzip));
		// Use chunked transfer
		options.setProperty(CHUNKED, Boolean.valueOf(chunked));
		options.setProperty(USER_AGENT, httpClient.getParams().getParameter(CoreProtocolPNames.USER_AGENT));

		// Override Axis default timeout and let client to set it
		options.setTimeOutInMilliSeconds(0);
		
		TransportOutDescription transport = new TransportOutDescription(protocol);
		HTTPClient4TransportSender sender = new HTTPClient4TransportSender();
		sender.init(context, transport);
		transport.setSender(sender);
		
		context.getAxisConfiguration().addTransportOut(transport);	
		
		if(logger.isDebugEnabled()) {
			for (AxisService entry : context.getAxisConfiguration().getServices().values()) {
				entry.addMessageContextListener(new MessageContextListener() {
					
					@Override
					public void attachServiceContextEvent(ServiceContext sc, MessageContext mc) {
						SOAPEnvelope envelope = mc.getEnvelope();
						if(envelope != null)
							logger.debug("SOAP OUT: " + envelope);
					}
					
					@Override
					public void attachEnvelopeEvent(MessageContext mc) {
						SOAPEnvelope envelope = mc.getEnvelope();
						if(envelope != null)
							logger.debug("SOAP IN: " + envelope);
					}
				});
			}
		}
	}

	protected abstract T getService(ConfigurationContext context, String endpoint) throws AxisFault;
}