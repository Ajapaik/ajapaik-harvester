package ee.ajapaik.service;

import java.util.Collection;
import java.util.List;

import ee.ajapaik.model.InfoSystem;
import ee.ajapaik.model.search.RecordView;
import ee.ajapaik.model.search.Search;
import ee.ajapaik.model.search.SearchResults;

public interface AjapaikService {

	List<InfoSystem> getInfoSystems();

	void updateInfoSystem(InfoSystem infoSystem);

	SearchResults search(Search search) throws Exception;

	List<String> getAllIds();

	RecordView[] getRecords(String... ids);

	void index();

	boolean isIndexing();

	boolean isIndexUpdated(long lastTimestamp);

	Collection<String> getAllSets();
}