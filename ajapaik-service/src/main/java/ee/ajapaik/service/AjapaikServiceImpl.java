package ee.ajapaik.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.quartz.SimpleTrigger;
import org.springframework.util.StringUtils;

import ee.ajapaik.db.Repository;
import ee.ajapaik.index.IndexedFields;
import ee.ajapaik.index.Indexer;
import ee.ajapaik.index.Result;
import ee.ajapaik.model.InfoSystem;
import ee.ajapaik.model.search.InstitutionType;
import ee.ajapaik.model.search.RecordView;
import ee.ajapaik.model.search.Search;
import ee.ajapaik.model.search.SearchResults;
import ee.ajapaik.schedule.Scheduler;

/**
 * @author <a href="mailto:kaido@urania.ee?subject=BamService">Kaido Kalda</a>
 */
public class AjapaikServiceImpl implements AjapaikService {

	private Scheduler scheduler;
	private Indexer indexer;
	private Repository repository;

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


	@Override
	public SearchResults search(Search search) throws Exception {
		long start = System.nanoTime();
		SearchResults result = new SearchResults();
		
		if(!StringUtils.hasText(search.getSearchPhrase()))
			return result;
			
//		fillResult(search, InstitutionType.MUSEUM, result);
//		fillResult(search, InstitutionType.LIBRARY, result);
//		fillResult(search, InstitutionType.ARCHIVE, result);
		fillResult(search, null, result);

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
	
	private void fillResult(Search search, InstitutionType type, SearchResults result) throws Exception {
		String phrase =search.getSearchPhrase();
		StringBuilder query;
		if("+FULL_SEARCH:(+match +all )".equals(phrase.trim()))
			query = new StringBuilder();
		else
			query = new StringBuilder(phrase);
		
		if(type != null)
			query.append(" +INSTITUTION_TYPE:").append(type.name());
		if(search.isDigital())
			query.append(" +DIGITAL:true");
		
		Result r = indexer.search(query.toString(), search.getSortBy(), Integer.MAX_VALUE);
		
		List<Document> documents = r.getResult();
		if(type != null) {
			switch (type) {
			case ARCHIVE:
				result.setArchiveIds(getRecordIds(documents));
				result.setTotalArchiveIds(r.getTotalHits());
				break;
			case MUSEUM:
				result.setMuseumIds(getRecordIds(documents));
				result.setTotalMuseumIds(r.getTotalHits());			
				break;
			case LIBRARY:
				result.setLibraryIds(getRecordIds(documents));
				result.setTotalLibraryIds(r.getTotalHits());
				break;
	
			}
		} else {
			result.setIds(getRecordIds(documents));
			result.setFirstRecordViews(getRecordViews(documents, 
					(search.getPageSize() > documents.size() ? documents.size() : search.getPageSize())));
		}
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
}