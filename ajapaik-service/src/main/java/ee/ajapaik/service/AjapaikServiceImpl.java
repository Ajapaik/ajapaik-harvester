package ee.ajapaik.service;

import ee.ajapaik.cache.CityCache;
import ee.ajapaik.cache.SourceCache;
import ee.ajapaik.dao.AjapaikDao;
import ee.ajapaik.db.Repository;
import ee.ajapaik.index.IndexedFields;
import ee.ajapaik.index.Indexer;
import ee.ajapaik.index.Result;
import ee.ajapaik.model.*;
import ee.ajapaik.model.search.RecordView;
import ee.ajapaik.model.search.Search;
import ee.ajapaik.model.search.SearchResults;
import ee.ajapaik.platform.AjapaikClient;
import ee.ajapaik.schedule.Scheduler;
import ee.ajapaik.util.IOHandler;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.quartz.SimpleTrigger;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:kaido@quest.ee?subject=AjapaikServiceImpl">Kaido Kalda</a>
 */
public class AjapaikServiceImpl implements AjapaikService {
	
	private static final Logger logger = Logger.getLogger(AjapaikServiceImpl.class);
	
	interface DataCallback {
		void notify(String name, byte[] data);
	}

	private Scheduler scheduler;
	private Indexer indexer;
	private Repository repository;
	private AjapaikDao ajapaikDao;
	private AjapaikClient ajapaikClient;
	private SourceCache sourceCache;
	private CityCache cityCache;
	
	public void setCityCache(CityCache cityCache) {
		this.cityCache = cityCache;
	}

	public void setSourceCache(SourceCache sourceCache) {
		this.sourceCache = sourceCache;
	}

	public void setAjapaikDao(AjapaikDao ajapaikDao) {
		this.ajapaikDao = ajapaikDao;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}
	
	public void setIndexer(Indexer indexer) {
		this.indexer = indexer;
	}

	public void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public List<InfoSystem> getInfoSystems() {
		return scheduler.getInfoSystems();
	}

	@Override
	public void updateInfoSystem(InfoSystem infoSystem) {
		scheduler.updateInfoSystem(infoSystem);
	}

	public void setAjapaikClient(AjapaikClient ajapaikClient) {
		this.ajapaikClient = ajapaikClient;
	}

	@Override
	public SearchResults search(Search search) throws Exception {
		long start = System.nanoTime();
		SearchResults result = new SearchResults();
		
		if(!StringUtils.hasText(search.getSearchPhrase()))
			return result;
			
		fillResult(search, result);

		result.setSearchTime(((double)(System.nanoTime() - start)) / 1000000.0);
		return result;
	}
	
	@Override
	public List<String> getAllIds() {
		List<String> result = new ArrayList<String>();
		
		IndexSearcher searcher = null;
		Document document = null;
		int index = 0;
		try {
			searcher = indexer.openSearcher();
			while((document = searcher.doc(index++)) != null) {
				RecordView recordView = getRecordView(document);
				String id = recordView.getId();
				
				byte[] bytes = id.getBytes(); // bypass system optimation while splitting strings
				result.add(new String(bytes));
			}
		} catch (Exception e) {
			// Ignored in any case
		} finally {
			indexer.closeSearcher(searcher);
		}
		return result;
	}
	
	@Override
	public RecordView[] getRecords(String... ids) {
		if(ids==null || ids.length==0)
			return new RecordView[0];
		
		List<RecordView> result = new ArrayList<RecordView>();
		for (Document document : indexer.getDocuments(ids)) {
			result.add(getRecordView(document));
		}
		return result.toArray(new RecordView[result.size()]);
	}
	
	private void fillResult(Search search, SearchResults result) throws Exception {
		String phrase = search.getSearchPhrase();
		StringBuilder query;
		if("+FULL_SEARCH:(+match +all )".equals(phrase.trim()))
			query = new StringBuilder();
		else
			query = new StringBuilder(phrase);

		if(search.isDigital())
			query.append(" +DIGITAL:true");
		
		Result r = indexer.search(query.toString(), search.getSortBy(), search.getMaxResult());
		
		List<Document> documents = r.getResult();
		result.setIds(getRecordIds(documents));
		result.setFirstRecordViews(getRecordViews(documents, 
				(search.getPageSize() > documents.size() ? documents.size() : search.getPageSize())));
	}

	private RecordView[] getRecordViews(List<Document> documents, int count) {
		RecordView[] result = new RecordView[count];
		for (int i = 0; i < count; i++) {
			result[i] = getRecordView(documents.get(i));
		}
		return result;
	}

	private String[] getRecordIds(List<Document> documents) {
		List<String> result = new ArrayList<String>();
		for (Document document : documents) {
			result.add(getRecordView(document).getId());
		}
		return result.toArray(new String[result.size()]);
	}

	private RecordView getRecordView(Document document) {
		return new RecordView(document.get(IndexedFields.RECORD_VIEW.name()));
	}

	@Override
	public void index() {
		logger.info("Indexing triggered");
		SimpleTrigger trigger = new SimpleTrigger(Scheduler.JOB_INDEXER_NAME);
		trigger.setJobName(Scheduler.JOB_INDEXER_NAME);
		
		scheduler.scheduleIndexing(trigger);
	}

	@Override
	public boolean isIndexing() {
		return scheduler.getHarvestJobListener().hasActiveIndexerJobs();
	}
	
	@Override
	public boolean isIndexUpdated(long lastTimestamp) {
		return indexer.getUpdateTimestamp() > lastTimestamp;
	}

	@Override
	public Collection<String> getAllSets() {
		return repository.queryAllSets().values();
	}

	@Override
	public List<MediaView> getMediaViews(Long taskId) throws Exception {
		return ajapaikDao.getMediaViews(taskId);
	}

	@Override
	public void scheduleTask(Long taskId) {
		scheduler.scheduleAIS(taskId);
	}

	@Override
	public List<TaskView> getTasks() {
		return ajapaikDao.getTasks();
	}

	@Override
	public void removeTask(Long taskId) {
		ajapaikDao.removeTask(taskId);
	}

	@Override
	public void postImages(final Integer cityId, String... ids) throws Exception {
		RecordView[] records = getRecords(ids);
		for (final RecordView recordView : records) {
			getImageData(recordView, new DataCallback() {
				
				@Override
				public void notify(String name, byte[] data) {
					try {
						ajapaikClient.postImages(cityId, getSourceId(recordView), name, data, recordView);
					} catch (Exception e) {
						logger.error("Error posting photo", e);
					}					
				}
			});
		}
		
		scheduleProposal();
	}
	

	@Override
	public void scheduleProposal() {
		
		// Make proposal
		SimpleTrigger trigger = new SimpleTrigger(Scheduler.JOB_PROPOSAL_NAME);
		trigger.setJobName(Scheduler.JOB_PROPOSAL_NAME);
		
		scheduler.scheduleProposal(trigger);
	}

	@Override
	public List<City> listCities() throws Exception {
		return cityCache.getData();
	}

	@Override
	public City createCity(City city) throws Exception {
		City result = ajapaikClient.createCity(city);
		
		cityCache.reload();
		
		return result;
	}

	private Integer getSourceId(RecordView recordView) throws Exception {
		String institution = null;
		if(recordView.getInstitution().contains(",")) {
			institution = recordView.getInstitution().split(",")[0];
		} else {
			institution = recordView.getInstitution();
		}
		
		List<Source> data = sourceCache.getData();
		for (Source source : data) {
			if(source.getDescription().equals(institution)) {
				return source.getId();
			}
		}
		
		Source source = ajapaikClient.createSource(institution);
		
		sourceCache.reload();
		
		return source.getId();
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