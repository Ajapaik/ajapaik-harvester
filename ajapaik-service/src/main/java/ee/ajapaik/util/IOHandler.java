package ee.ajapaik.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import ee.ajapaik.db.Repository;
import ee.ajapaik.platform.BaseHttpClient;
import ee.ajapaik.platform.HttpClientFactory;

public class IOHandler {

	protected static final Logger logger = Logger.getLogger(IOHandler.class);
	
	public static InputStream getInputStream(HttpResponse response) throws Exception {
		StatusLine statusLine = response.getStatusLine();
		if(statusLine.getStatusCode() != 200) {
			response.getEntity().getContent().close();
			
			throw new Exception(statusLine.getReasonPhrase());
		}
		
		 Header[] headers = response.getHeaders("Content-Encoding");
		 if(headers != null && headers.length > 0) {
			 Header contentEncoding = headers[0];
			 if("gzip".equalsIgnoreCase(contentEncoding.getValue())) {
				return new GZIPInputStream(response.getEntity().getContent());
			 }
		 }
		 return response.getEntity().getContent();
	}
	

	public static String saveThumbnail(String thumbnailUrl, Map<String, String> headers, Repository repository, String taskCode, RedirectStrategy strategy) {
		try {
			URL url = new URL(thumbnailUrl);
			
			InputStream is = openStream(url, strategy, headers);
			
			if(is != null) {
				byte[] data = IOUtils.toByteArray(is);
				is.close();
				
				String key = Digester.digestToString(data);
				
				repository.saveImage(key, data, taskCode);
				
				return key;
			}
		} catch (MalformedURLException e) {
			logger.error("Error parsing url: " + thumbnailUrl, e);
		} catch (IOException e) {
			logger.error("Error reading stream", e);
		}
		
		return null;	
	}
	
	public static String saveThumbnail(String thumbnailUrl, Repository repository, String taskCode, RedirectStrategy strategy) {
		return saveThumbnail(thumbnailUrl, null, repository, taskCode, strategy);
	}
	
	public static String saveThumbnail(String thumbnailUrl, Repository repository, String taskCode) {
		return saveThumbnail(thumbnailUrl, repository, taskCode, null);
	}
	
	public static InputStream openStream(URL url, RedirectStrategy strategy) {
		return openStream(url, strategy, null);
	}
	
	public static InputStream openStream(URL url) {
		return openStream(url, null, null);
	}

	public static InputStream openStream(URL url, RedirectStrategy strategy, Map<String, String> headers) {
		try {
			if(url != null) {
				logger.debug("About to make query for url: " + url);
				
				BaseHttpClient bc = HttpClientFactory.getInstance().getClient(url);
				
				HttpClient httpClient = bc.getHttpClient();
				DefaultHttpClient defaultHttpClient = (DefaultHttpClient) httpClient;
				if(strategy != null) {
					
					defaultHttpClient.setRedirectStrategy(strategy);
				}

				defaultHttpClient.addRequestInterceptor(new HttpRequestInterceptor() {
					
					@Override
					public void process(HttpRequest req, HttpContext arg1) throws HttpException, IOException {
						logger.debug("Process request: " + req);
						
						String host = req.getHeaders("Host")[0].getValue();
						
						logger.debug("Host headr: " + host);
						
						if(host.contains(":")) {
							logger.debug("Header has port! Splitting: " + host.split(":")[0]);
							
							req.setHeader("Host", host.split(":")[0]);
						}
					}
				});
				
				HttpGet get = new HttpGet(url.getFile());
				get.addHeader(new BasicHeader("Accept-Encoding", "gzip,deflate"));
//				get.addHeader(new BasicHeader("Host", url.getHost()));
				
				if(headers != null) {
					for(Entry<String, String> entry : headers.entrySet()) {
						get.addHeader(new BasicHeader(entry.getKey(), entry.getValue()));		
					}
				}
				
				HttpResponse result = httpClient.execute(get);
				
				HttpEntity entity = result.getEntity();
				
				if(entity != null) {
					return getInputStream(result);
				}
			}
		} catch (Exception e) {
			logger.error("Error opening stream data", e);
		}
		return null;
	}
}
