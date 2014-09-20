package ee.ajapaik.platform;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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

import ee.ajapaik.model.City;
import ee.ajapaik.model.Photo;
import ee.ajapaik.model.Source;
import ee.ajapaik.model.search.RecordView;

public class AjapaikClient extends BaseHttpClient {
	
	private ObjectMapper mapper = new ObjectMapper();
	

	public Source createSource(String institution) throws Exception {
		MultipartEntity entity = new MultipartEntity();
		entity.addPart("name", getStringBody(institution));
		entity.addPart("description", getStringBody(institution));
		
		HttpPost request = new HttpPost("/api/cities/");
		request.setEntity(entity);
		
		HttpResponse response = httpClient.execute(request);
		
		Source result = mapper.readValue(response.getEntity().getContent(), Source.class);
		
		request.reset();
		
		logger.debug("POST returned: " + result);
		
		return result;
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
	

	public List<Source> listSources() throws Exception {
		HttpGet request = new HttpGet("/api/sources/?format=json");
		HttpResponse response = httpClient.execute(request);
		
		List<Source> result = mapper.readValue(response.getEntity().getContent(), collectionType(Source.class));
		
		request.reset();
		
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

	public void postImages(Integer cityId, Integer sourceId, String imageName, byte[] imageData, RecordView recordView) throws Exception {
		MultipartEntity entity = new MultipartEntity();
		entity.addPart("image", new ByteArrayBody(imageData, "image/jpeg", imageName));
		entity.addPart("source", getStringBody(sourceId));
		entity.addPart("date_text", getStringBody(recordView.getDate()));
		entity.addPart("description", getStringBody(recordView.getTitle() + ": " + recordView.getDescription()));
		entity.addPart("source_key", getStringBody(recordView.getIdentifyingNumber()));
		entity.addPart("source_url", getStringBody(recordView.getUrlToRecord()));
		entity.addPart("city", getStringBody(cityId));
		
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
}
