package ee.ajapaik.serialize;


import com.sleepycat.je.DatabaseEntry;

import ee.ajapaik.model.search.Record;

/**
 * @author <a href="mailto:kaido@urania.ee?subject=Serializer">Kaido Kalda</a>
 */
public interface Serializer {
	
	DatabaseEntry serializeKey(String key);

	DatabaseEntry serializeRecord(Record record);
	
	String deserializeKey(DatabaseEntry key);

	Record deserializeRecord(DatabaseEntry entry);
}
