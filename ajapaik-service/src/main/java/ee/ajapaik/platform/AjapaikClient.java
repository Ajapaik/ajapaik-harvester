package ee.ajapaik.platform;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicHeader;

import ee.ajapaik.db.Repository;
import ee.ajapaik.model.search.RecordView;

public class AjapaikClient extends BaseHttpClient {
	
	interface DataCallback {
		void notify(String name, byte[] data);
	}

	private Repository repository;
	
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void postImages(RecordView... recordViews) throws Exception {
		for (RecordView recordView : recordViews) {
			
			logger.debug("About to execute POST on path '" + baseUrl + "'");
			
			final MultipartEntity entity = new MultipartEntity();
			getImageData(recordView, new DataCallback() {
				
				@Override
				public void notify(String name, byte[] data) {
					entity.addPart("image", new ByteArrayBody(data, "image/jpeg", name)); 
				}
				
			});
			
			String institution = recordView.getInstitution();
			if(institution.contains(",")) {
				entity.addPart("institution", getStringBody(institution.split(",")[0]));
			} else {
				entity.addPart("institution", getStringBody(institution));
			}
			
			entity.addPart("date", getStringBody(recordView.getDate()));
			entity.addPart("description", getStringBody(recordView.getDescription()));
			entity.addPart("title", getStringBody(recordView.getTitle()));
			entity.addPart("number", getStringBody(recordView.getIdentifyingNumber()));
			entity.addPart("url", getStringBody(recordView.getUrlToRecord()));
			entity.addPart("place", getStringBody("Valimimoodul"));
			
			HttpPost request = new HttpPost(baseUrl);
			request.setEntity(entity);
			
			try {
				HttpResponse response = httpClient.execute(request);
				
				String result = parseResponse(response);
				
				logger.debug("POST returned: " + result);
			} catch (Exception e) {
				logger.error("Error while executing request", e);
			}
		}
	}

	private StringBody getStringBody(String data) throws UnsupportedEncodingException {
		return new StringBody((data != null ? data : ""), Charset.forName("UTF-8"));
	}

	private String parseResponse(HttpResponse response) throws Exception {
		HttpEntity entity = response.getEntity();
		if(entity != null) {
			if (response.getStatusLine().getStatusCode() == 200) {
				byte[] data = IOUtils.toByteArray(entity.getContent());
				
				return new String(data, "UTF-8");
			}
		}
		
		return null;
	}

	private void getImageData(RecordView recordView, DataCallback dataCallback) {
		try {
			if(recordView.getImageUrl() != null && !recordView.getImageUrl().equals("null")) {
				grabImage(recordView.getImageUrl(), dataCallback);
			} else if(recordView.getCachedThumbnailUrl() != null) {
				byte[] data = repository.queryImage(recordView.getCachedThumbnailUrl());
				if (data != null) {
					dataCallback.notify(recordView.getCachedThumbnailUrl() + ".jpg", data);
				}
			}
		} catch (Exception e) {
			logger.error("Error getting data", e);
		}
	}

	private void grabImage(String query, DataCallback c) throws Exception {
		URL url = new URL(query);
		BaseHttpClient client = HttpClientFactory.getInstance().getClient(url);
		
		logger.debug("Getting data from url: " + url);
		
		HttpGet get = new HttpGet(url.getFile());
		get.addHeader(new BasicHeader("Accept-Encoding", "gzip,deflate"));
		
		HttpResponse result = client.getHttpClient().execute(get);
		HttpEntity entity = result.getEntity();
		
		if(entity != null) {
			if (result.getStatusLine().getStatusCode() == 200) {
				
				String fileName = getFileName(query);
				
				c.notify(fileName, IOUtils.toByteArray(entity.getContent()));
				
				return;
			}
		}
		
		c.notify("", null);
	}
	
	private String getFileName(String query) {
		String[] split = query.split("/");
		String lastPart = split[split.length - 1];
		
		String fileName;
		if(lastPart.contains("=")) {
			split = lastPart.split("=");
			fileName = split[split.length - 1];
		} else {
			fileName = lastPart;
		}
		
		if(!fileName.contains(".jpg")) {
			fileName += ".jpg";
		}
		
		return fileName;
	}
}
