package ee.ajapaik.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import ee.ajapaik.db.Repository;
import ee.ajapaik.model.search.Record;
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
	
	public static void saveThumbnail(Record rec, String thumbnailUrl, Repository repository, String taskCode) {
		try {
			URL url = new URL(thumbnailUrl);
			
			InputStream is = openStream(url);
			
			if(is != null) {
				byte[] data = IOUtils.toByteArray(is);
				is.close();
				
				String key = Digester.digestToString(data);
				
				repository.saveImage(key, data, taskCode);
				
				rec.setCachedThumbnailUrl(key);
			}
		} catch (MalformedURLException e) {
			logger.error("Error parsing url: " + thumbnailUrl, e);
		} catch (IOException e) {
			logger.error("Error reading stream", e);
		}
	}
	
	public static InputStream openStream(URL url) {
		try {
			if(url != null) {
				logger.debug("About to make query for url: " + url);
				
				BaseHttpClient bc = HttpClientFactory.getInstance().getClient(url);
				
				HttpGet get = new HttpGet(url.getFile());
				get.addHeader(new BasicHeader("Accept-Encoding", "gzip,deflate"));
				
				HttpResponse result = bc.getHttpClient().execute(get);
				
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