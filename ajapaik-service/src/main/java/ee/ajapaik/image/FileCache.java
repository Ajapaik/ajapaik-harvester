package ee.ajapaik.image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import ee.ajapaik.platform.BaseHttpClient;
import ee.ajapaik.platform.PlatformFactory;
import ee.ajapaik.util.Digester;
import ee.ajapaik.util.Digester.DataCallback;

/**
 * @author Kaido
 */
public class FileCache {
	
	private static final Logger logger = Logger.getLogger(FileCache.class);
	private String fileStore;

	public void setFileStore(String fileStore) {
		this.fileStore = fileStore;
	}

	public String cacheImage(byte[] data) {
		String fileName = Digester.digestToString(data);
		File file = new File(fileStore, fileName);
		if(!file.exists()) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				fos.write(data);				
			} catch (Exception e) {
				logger.error("Error while reading data" , e);
				return null;
			} finally {
				try {
					if(fos != null)
						fos.close();
				} catch (IOException e) {
					logger.error("Error closing streams", e);
				}
			}
		}
		
		return fileName;
	}
	
	public String cacheImage(URL url) {
		if(url == null)
			return null;
		
		final InputStream is = openStream(url);
		if(is == null)
			return null;
		
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		String name;
		FileOutputStream fos = null;
		try {		
			name = Digester.digestToString(new DataCallback() {
				@Override
				public int getByte() {
					int b = -1;
					try {
						if(is != null) {
							if((b = is.read()) != -1)
								baos.write(b);
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					return b;
				}
			});
			
			File directory = new File(fileStore);
			if(!directory.exists()) {
				logger.debug("Location '" + directory.getAbsolutePath() + "' not exists. Creating...");
				
				directory.mkdirs();
			}
			
			File file = new File(fileStore, name);
			if(!file.exists()) {
				fos = new FileOutputStream(file);
				baos.writeTo(fos);
			}
		} catch (IOException e) {
			logger.error("Error while reading data" , e);
			return null;
		} finally {
			try {
				is.close();
				baos.close();
				
				if(fos != null)
					fos.close();
			} catch (Exception e) {
				logger.error("Error closing streams", e);
			}
		}
		
		return name;
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
				
				BaseHttpClient bc = PlatformFactory.getInstance().getClient(url);
				
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