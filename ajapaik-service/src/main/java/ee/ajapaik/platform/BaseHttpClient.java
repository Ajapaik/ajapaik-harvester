package ee.ajapaik.platform;

import java.io.IOException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

/**
 * Base class for all http clients
 * 
 * @author <a href="mailto:kaido@quest.ee?subject=BaseHttpClient">Kaido Kalda</a>
 */
public class BaseHttpClient extends BaseClient {
	
	protected HttpClient httpClient;
	protected int maxConcurrentConnections;
	protected String baseUrl;
	protected int retryCount;
	protected String protocol;
	protected String userAgent;
	protected String username;
	protected String password;
	protected boolean useBasicAuth;
	
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setMaxConcurrentConnections(int maxConcurrentConnections) {
		this.maxConcurrentConnections = maxConcurrentConnections;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}
	
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	
	public HttpClient getHttpClient() {
		return httpClient;
	}
	
	public void setUseBasicAuth(boolean useBasicAuth) {
		this.useBasicAuth = useBasicAuth;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme(protocol, port, getSocketFactory()));
        
        final HttpHost httpHost = new HttpHost(host, port, protocol);
        
        HttpParams httpClientParams = new BasicHttpParams();
		httpClientParams.setParameter(ClientPNames.DEFAULT_HOST, httpHost);
		// Character encoding sent by "Content-Type" header
        httpClientParams.setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET, "UTF-8");
        // timeout, while making connection
        httpClientParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
        // timeout, while waiting for response
        httpClientParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);
        // User agent sent by "User-Agent" header
        httpClientParams.setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
        
        // Maximum total connections client can handle
        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(schemeRegistry) {
        	@Override
        	public ClientConnectionRequest requestConnection(HttpRoute route, Object state) {
        		ClientConnectionRequest connection = super.requestConnection(route, state);
        		return connection;
        	}
        };
        connectionManager.setMaxTotal(maxConcurrentConnections);
        connectionManager.setDefaultMaxPerRoute(maxConcurrentConnections);

		DefaultHttpClient base = new DefaultHttpClient(connectionManager, httpClientParams);
		
		// Retry handler
		base.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(retryCount, false) {
			@Override
			public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
				return executionCount <= retryCount;
			}
		});
		
		
		// Basic authentication
		if(useBasicAuth) {
			base.getCredentialsProvider().setCredentials(
			        new AuthScope(httpHost), 
			        new UsernamePasswordCredentials(username, password));
		}
		
		base.addRequestInterceptor(new HttpRequestInterceptor() {
			
			@Override
			public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
				if(useBasicAuth) {
					AuthCache authCache = (AuthCache) context.getAttribute(ClientContext.AUTH_CACHE);
					if(authCache == null) {
						authCache = new BasicAuthCache();
						authCache.put(httpHost, new BasicScheme());
						context.setAttribute(ClientContext.AUTH_CACHE, authCache);
					}
				}
				
				beforeRequest(request);
			}
		}, 0);
		base.addResponseInterceptor(new HttpResponseInterceptor() {
			
			@Override
			public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
				
			}
		}, 0);
		
		this.httpClient = base;
	}

	protected void beforeRequest(HttpRequest request) {
	}

	private SchemeSocketFactory getSocketFactory() throws Exception {
		if("http".equals(protocol)) {
			 return PlainSocketFactory.getSocketFactory();
		} else {
			logger.debug("Initializing SSL");
			
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					logger.debug("TrustManager: AcceptedIssuers: accept all");
					return null;
				}
	
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] certs, String authType) {
					logger.debug("TrustManager: ClientTrusted (" + authType
							+ "): trust all");
				}
	
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] certs, String authType) {
					logger.debug("TrustManager: ServerTrusted(" + authType
							+ "): trust all");
				}
			} };
	
			SSLContext httpsContext = SSLContext.getInstance("SSL");
			httpsContext.init(null, trustAllCerts, new SecureRandom());
			
			return new SSLSocketFactory(httpsContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		}
	}
}