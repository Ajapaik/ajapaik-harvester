package ee.ajapaik.platform;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import ee.ajapaik.db.Repository;
import ee.ajapaik.model.City;
import ee.ajapaik.model.Photo;
import ee.ajapaik.model.search.RecordView;
import ee.ajapaik.util.IOHandler;

public class AjapaikClient extends BaseHttpClient {
	
	interface DataCallback {
		void notify(String name, byte[] data);
	}

	private ObjectMapper mapper = new ObjectMapper();
	private Repository repository;
	
	public void setRepository(Repository repository) {
		this.repository = repository;
	}
	
	public City createCity(City city) throws Exception {
		MultipartEntity entity = new MultipartEntity();
		entity.addPart("name", getStringBody(city.getName()));
		entity.addPart("lat", getStringBody(city.getLat()));
		entity.addPart("lon", getStringBody(city.getLon()));
		
		HttpPost request = new HttpPost("/api/cities/");
		request.setEntity(entity);
		
		HttpResponse response = httpClient.execute(request);
		
		City result = mapper.readValue(response.getEntity().getContent(), City.class);
		
		request.reset();
		
		logger.debug("POST returned: " + result);
		
		return result;
	}
	

	public List<Photo> listPhotos() throws Exception {
		HttpGet request = new HttpGet("/api/photos/?format=json");
		HttpResponse response = httpClient.execute(request);
		
		List<Photo> result = mapper.readValue(response.getEntity().getContent(), collectionType(Photo.class));
		
		request.reset();
		
		return result;
	}

	public List<City> listCities() throws Exception {
		HttpGet request = new HttpGet("/api/cities/?format=json");
		HttpResponse response = httpClient.execute(request);
		
		List<City> result = mapper.readValue(response.getEntity().getContent(), collectionType(City.class));
		
		request.reset();
		
		return result;
	}

	public void postImages(Integer city, RecordView... recordViews) throws Exception {
		for (RecordView recordView : recordViews) {
			
			final MultipartEntity entity = new MultipartEntity();
			getImageData(recordView, new DataCallback() {
				
				@Override
				public void notify(String name, byte[] data) {
					entity.addPart("image", new ByteArrayBody(data, "image/jpeg", name)); 
				}
				
			});
			
			// FIXME: parse source from REST
			entity.addPart("source", getStringBody("56"));
			entity.addPart("date_text", getStringBody(recordView.getDate()));
			entity.addPart("description", getStringBody(recordView.getTitle() + ": " + recordView.getDescription()));
			entity.addPart("source_key", getStringBody(recordView.getIdentifyingNumber()));
			entity.addPart("source_url", getStringBody(recordView.getUrlToRecord()));
			entity.addPart("city", getStringBody(city));
			
			HttpPost request = new HttpPost("/api/photos/");
			request.setEntity(entity);
			
			try {
				HttpResponse response = httpClient.execute(request);
				
				String result = parseResponse(response);
				
				request.reset();
				
				logger.debug("POST returned: " + result);
			} catch (Exception e) {
				logger.error("Error while executing request", e);
			}
		}
	}

	private CollectionType collectionType(Class<?> clazz) {
		return mapper.getTypeFactory().constructCollectionType(List.class, clazz);
	}
	
	private StringBody getStringBody(Object data) throws UnsupportedEncodingException {
		return new StringBody((data != null ? data.toString() : ""), Charset.forName("UTF-8"));
	}

	private String parseResponse(HttpResponse response) throws Exception {
		HttpEntity entity = response.getEntity();
		if(entity != null) {
			InputStream is = entity.getContent();
			
			byte[] data = IOUtils.toByteArray(is);
			
			return new String(data, "UTF-8");
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
		
		// XXX: muis hack
		if(query.contains("portaal/")) {
			query = query.replace("portaal/", "");
		}
		
		URL url = new URL(query);
		InputStream is = IOHandler.openStream(url);
		if(is != null) {
			c.notify(getFileName(query), IOUtils.toByteArray(is));
			
			is.close();
			
			return;
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
