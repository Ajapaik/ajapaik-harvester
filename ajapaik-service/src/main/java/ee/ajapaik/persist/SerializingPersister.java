package ee.ajapaik.persist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import ee.ajapaik.model.InfoSystem;
import ee.ajapaik.util.Digester;

public class SerializingPersister implements InitializingBean {
	
	private static Logger logger = Logger.getLogger(SerializingPersister.class);
	
	protected Map<String, List<Object>> map = new HashMap<String, List<Object>>();
	private String location;
	private String fileName;
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public synchronized void save(String key, String data) {
		List<Object> list = map.get(key);
		
		if(list == null) {
			list = new ArrayList<Object>();
			map.put(key, list);
		} else {
			if(list.contains(data))
				list.remove(data);
		}
		
		list.add(data);
		
		persist();
	}
	
	public synchronized void save(Object o) {
		List<Object> list = map.get(o.getClass().getName());
		
		if(list == null) {
			list = new ArrayList<Object>();
			map.put(o.getClass().getName(), list);
		} else {
			if(list.contains(o))
				list.remove(o);
		}
		
		list.add(o);
		
		persist();
	}

	public <T> List<T> load(Class<T> clazz) {
		return load(clazz.getName());
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T> List<T> load(String key) {
		List<Object> list = map.get(key);
		if(list != null)
			return new ArrayList<T>((List<T>) list);
		
		return null;
	}
	
	public synchronized void delete(Object o) {
		List<Object> list = map.get(o.getClass().getName());
		
		if(list != null) {
			list.remove(o);
		}
		
		persist();		
	}
	
	public synchronized void delete(String key, Object o) {
		List<Object> list = map.get(key);
		
		if(list != null) {
			list.remove(o);
		}
		
		persist();		
	}
	
	private void persist() {
		ObjectOutputStream oos = null;
		try {
			File directory = new File(location);
			if(!directory.exists()) {
				logger.debug("Location '" + directory.getAbsolutePath() + "' not exists. Creating...");
				
				directory.mkdirs();
				
				return;
			}
			
			oos = new ObjectOutputStream(new FileOutputStream(location + "/" + fileName));
			synchronized (map) {
				oos.writeObject(map);
			}
		} catch (IOException e) {
			logger.error("Error while writing object: " + e);
		} finally {
			try {
				oos.close();
			} catch (IOException e) { 
				logger.error("Error while closing stream: " + e); 
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet() throws Exception {
		ObjectInputStream ois = null;
		try {
			File directory = new File(location);
			if(!directory.exists()) {
				logger.debug("Location '" + directory.getAbsolutePath() + "' not exists. Creating...");
				
				directory.mkdirs();
			}
			
			ois = new ObjectInputStream(new FileInputStream(location + "/" + fileName));
			Object object = ois.readObject();
			if( object != null )
				this.map = (Map<String, List<Object>>) object;
		} catch (Exception e) {
			logger.error("Error reading configuration: " + e); 
		} finally {
			try {
				if(ois != null)
					ois.close();
			} catch (IOException e) {
				logger.error("Error while closing stream: " + e); 
			}
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		
		System.out.println(Digester.digestToString("ETERA"));
		
		SerializingPersister sp = new SerializingPersister();
		sp.setFileName("data.ser.ajapaik");
		sp.setLocation("/home/kaido/git/repo/ajapaik/ajapaik-parent");
		
		sp.afterPropertiesSet();
		
		List<Object> x = sp.map.get("ee.ajapaik.model.InfoSystem");
		for (Object object : x) {
			InfoSystem is = (InfoSystem) object;
			if(is.getName().equals("ETERA")) {
				is.setLastHarvestTime(null);
			}
		}
		
		sp.persist();
		
		return;
	}
}
