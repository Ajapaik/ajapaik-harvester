package ee.ajapaik.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import ee.ajapaik.model.Photo;
import ee.ajapaik.platform.AjapaikClient;

public class PhotoCache implements InitializingBean, DisposableBean {
	
	private static final Logger logger = Logger.getLogger(PhotoCache.class);
	
	private Timer timer = new Timer();
	private List<ChangeListener<Photo>> listeners = new ArrayList<ChangeListener<Photo>>();
	private List<Photo> photos = new ArrayList<Photo>();
	
	private AjapaikClient ajapaikClient;

	public void setAjapaikClient(AjapaikClient ajapaikClient) {
		this.ajapaikClient = ajapaikClient;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				synchronized (photos) {
					try {
						photos = ajapaikClient.listPhotos();
						
						for (ChangeListener<Photo> listener : listeners) {
							listener.notify(photos);
						}
					} catch (Exception e) {
						logger.error("Error updating Photo cache", e);
					}			
				}
			}
		}, 0, 86400000L);
	}

	public List<Photo> getPhotos() {
		synchronized (photos) {
			return photos;
		}
	}
	
	public void addChangeListener(ChangeListener<Photo> listener) {
		listeners.add(listener);
	}

	@Override
	public void destroy() throws Exception {
		timer.cancel();
	}
}
