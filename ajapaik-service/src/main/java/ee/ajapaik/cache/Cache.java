package ee.ajapaik.cache;

import ee.ajapaik.platform.AjapaikClient;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class Cache<T> implements InitializingBean, DisposableBean {
	
	private static final Logger logger = Logger.getLogger(Cache.class);
	
	private Timer timer = new Timer();
	private List<ChangeListener<T>> listeners = new ArrayList<ChangeListener<T>>();
	private List<T> data = new ArrayList<T>();
	private long period;
	
	private TimerTask task;
	protected AjapaikClient ajapaikClient;
	
	public void setPeriod(long period) {
		this.period = period;
	}

	public void setAjapaikClient(AjapaikClient ajapaikClient) {
		this.ajapaikClient = ajapaikClient;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		task = new TimerTask() {
			
			@Override
			public void run() {
				synchronized (data) {
					try {
						data = loadData();
						
						for (ChangeListener<T> listener : listeners) {
							listener.notify(data);
						}
					} catch (Exception e) {
						logger.error("Error updating cache: " + e.getCause().getMessage());
					}			
				}
			}
		};
		
		timer.schedule(task, 0, period);
	}

	public List<T> getData() {
		synchronized (data) {
			return data;
		}
	}
	
	public void addChangeListener(ChangeListener<T> listener) {
		listeners.add(listener);
	}
	
	public void reload() {
		task.run();
	}

	@Override
	public void destroy() throws Exception {
		timer.cancel();
	}
	
	protected abstract List<T> loadData() throws Exception;
}
