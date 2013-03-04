package ee.ajapaik.db;

import ee.ajapaik.model.search.Record;

public interface RecordHandler {
	void handleRecord(Record rec, String code);
}
