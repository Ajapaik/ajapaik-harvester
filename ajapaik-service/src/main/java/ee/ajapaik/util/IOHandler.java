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
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.fluent.Request;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import ee.ajapaik.db.Repository;

import static org.apache.http.client.fluent.Request.Get;

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
	

	public static String saveThumbnail(String thumbnailUrl, Map<String, String> headers, Repository repository, String taskCode) {
		try {
			URL url = new URL(thumbnailUrl);
			
			InputStream is = openStream(url, headers);
			
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
	
	public static String saveThumbnail(String thumbnailUrl, Repository repository, String taskCode) {
		return saveThumbnail(thumbnailUrl, null, repository, taskCode);
	}
	
	public static InputStream openStream(URL url) {
		return openStream(url, null);
	}

	public static InputStream openStream(URL url, Map<String, String> headers) {
		try {
			if(url != null) {
				logger.debug("About to make query for url: " + url);
				Request request = Get(url.toURI()).socketTimeout(5000).connectTimeout(5000).addHeader(new BasicHeader("Accept-Encoding", "gzip,deflate"));

				if (headers != null) {
					for (Entry<String, String> entry : headers.entrySet()) {
						request.addHeader(new BasicHeader(entry.getKey(), entry.getValue()));
					}
				}

				return request.execute().returnContent().asStream();
			}
		} catch (Exception e) {
			logger.error("Error opening stream data", e);
		}
		return null;
	}
}
