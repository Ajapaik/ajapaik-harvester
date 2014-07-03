package ee.ajapaik.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import ee.ajapaik.platform.BaseHttpClient;
import ee.ajapaik.platform.HttpClientFactory;

/**
 * @author <a href="mailto:kaido@quest.ee?subject=FileCache">Kaido Kalda</a>
 */
public class FileCache {
	
	private static final Logger logger = Logger.getLogger(FileCache.class);
	private String fileStore;

	public void setFileStore(String fileStore) {
		this.fileStore = fileStore;
	}
	


	public InputStream getImage(String name) {
		try {
			return new FileInputStream(new File(fileStore, name));
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
	public InputStream openStream(URL url) {
		try {
			if(url != null) {
				logger.debug("About to make query for url: " + url);
				
				BaseHttpClient bc = HttpClientFactory.getInstance().getClient(url);
				
				HttpGet get = new HttpGet(url.getFile());
				get.addHeader(new BasicHeader("Accept-Encoding", "gzip,deflate"));
				
				HttpResponse result = bc.getHttpClient().execute(get);
				HttpEntity entity = result.getEntity();
				
				if(entity != null) {
					if (result.getStatusLine().getStatusCode() != 404) {
						return entity.getContent();
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error getting image data", e);
		}
		return null;
	}
}