package ee.ajapaik.service;

import ee.ajapaik.model.City;
import ee.ajapaik.model.InfoSystem;
import ee.ajapaik.model.MediaView;
import ee.ajapaik.model.TaskView;
import ee.ajapaik.model.search.RecordView;
import ee.ajapaik.model.search.Search;
import ee.ajapaik.model.search.SearchResults;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
	
	List<MediaView> getMediaViews(Long taskId) throws Exception;
	
	List<TaskView> getTasks();
	
	void scheduleTask(Long taskId);
	
	void removeTask(Long taskId);
	
	void postImages(Integer cityId, String... ids) throws Exception;
	
	List<City> listCities() throws Exception;
	
	City createCity(City city) throws Exception;
	
	void scheduleProposal();

	Map<String, List<String>> getFailedSets();

	void initCustomHarvester(InfoSystem infoSystem);
}