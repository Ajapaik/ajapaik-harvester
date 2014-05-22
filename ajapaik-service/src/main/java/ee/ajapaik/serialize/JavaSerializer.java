package ee.ajapaik.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;

import com.sleepycat.je.DatabaseEntry;

import ee.ajapaik.model.search.Record;

/**
 * @author <a href="mailto:kaido@quest.ee?subject=JavaSerializer">Kaido Kalda</a>
 */
public class JavaSerializer implements Serializer {

	private static final Logger logger = Logger.getLogger(JavaSerializer.class);

	/**
	 * XXX: optimize - String.toByteArray()
	 */
	@Override
	public String deserializeKey(DatabaseEntry key) {
		return getData(key);
	}

	@Override
	public Record deserializeRecord(DatabaseEntry entry) {
		return getData(entry);
	}

	/**
	 * XXX: optimize - String.toByteArray()
	 */
	@Override
	public DatabaseEntry serializeKey(String key) {
		return getDatabaseEntry(key);
	}

	@Override
	public DatabaseEntry serializeRecord(Record record) {
		return getDatabaseEntry(record);
	}
	
	private DatabaseEntry getDatabaseEntry(Object o) {
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
		} catch (IOException e) {
			logger.error("Error while writing object: " + e);
		} finally {
			try {
				oos.close();
				baos.close();
			} catch (IOException e) { 
				logger.error("Error while closing stream: " + e); 
			}
		}

		if(logger.isTraceEnabled())
			logger.trace("Saving object: " + new String(baos.toByteArray()));
		
		return new DatabaseEntry(baos.toByteArray());
	}

	@SuppressWarnings("unchecked")
	public <K> K getData(DatabaseEntry data) {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(data.getData()));
			return (K) ois.readObject();
		} catch (IOException e) {
			logger.error("Error while reading object: " + e); 
		} catch (ClassNotFoundException e) {
			logger.error("Class not loaded: " + e); 
		} finally {
			try {
				if(ois != null)
					ois.close();
			} catch (IOException e) {
				logger.error("Error while closing stream: " + e); 
			}
		}
		return null;
	}
}