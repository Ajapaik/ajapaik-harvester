package ee.ajapaik.index;

import ee.ajapaik.db.RecordHandler;
import ee.ajapaik.db.Repository;
import ee.ajapaik.model.search.Record;
import ee.ajapaik.model.search.RecordView;
import ee.ajapaik.model.search.SortableField;
import ee.ajapaik.util.Holder;
import ee.ajapaik.util.Tracer;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static ee.ajapaik.index.IndexedFields.*;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author <a href="mailto:kaido@quest.ee?subject=Indexer">Kaido Kalda</a>
 */
public class Indexer implements InitializingBean {

	private static final Logger logger = Logger.getLogger(Indexer.class);
	public static final DateFormat DATE_CREATED_FORMAT = new SimpleDateFormat("yyyyMMdd");

	private static final String ACTIVE = "active";
	private static final String AVAILABLE = "available";
	
	private String indexDirectory;
	private Repository repository;

	private IndexWriter writer;
	
	private StandardAnalyzer analyzer;
	private List<IndexSearcher> searchers = new ArrayList<IndexSearcher>();
	private long updateTimestamp = System.currentTimeMillis();
	private String muisRepoHash = "fa40b27ef128c8304fc069ed226de8a4";

	public long getUpdateTimestamp() {
		return updateTimestamp;
	}

	public Indexer() {
		Set<String> stopwords = new HashSet<String>();
		stopwords.add("match_all");
		
		this.analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT, stopwords);

	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setIndexDirectory(String indexDirectory) {
		this.indexDirectory = indexDirectory;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		for( String path : new String[]{ AVAILABLE, ACTIVE } ) {
			File location = new File(indexDirectory, path);
			if (!location.exists()) {
				logger.debug("Directory '" + location.getAbsolutePath() + "' not exists. Creating...");
				location.mkdirs();
			}			
		}

		this.writer = new IndexWriter(
				FSDirectory.open(new File(indexDirectory, ACTIVE)), 
				analyzer, 
				MaxFieldLength.UNLIMITED);
		
		try {
			closeSearcher(openSearcher());
		} catch (IOException e) {
			logger.warn("No index found, indexing! Caused by error: ", e);
			index();			
		}
	}

	public void closeSearcher(IndexSearcher searcher) {
		if(searcher != null) {
			synchronized (searchers) {
				
				logger.debug("Closing searcher! Opened searchers: " + searchers.size());
				
				searchers.remove(searcher);
				searchers.notifyAll();
				
				try {
					searcher.close();
				} catch (IOException e) {
					logger.error("Error closing searcher.", e);
				}
			}
		}
	}

	public IndexSearcher openSearcher() throws CorruptIndexException, IOException {
		synchronized (searchers) {
			logger.debug("Opening searcher! Opened searchers: " + searchers.size());
			
			File directory = new File(indexDirectory, AVAILABLE);
			IndexSearcher searcher = new IndexSearcher(FSDirectory.open(directory), false);
			
			searchers.add(searcher);
			
			return searcher;
		}
	}

	private Document getDocument(Record rec, String repositoryCode) {
		Document doc = new Document();
		
		String[] idSplit = rec.getId().split("\\:");
		
		addField(doc, ID, rec.getId(), Field.Index.NOT_ANALYZED);
		addField(doc, ID_NUMBER, idSplit[idSplit.length - 1]);
		addField(doc, CODE, repositoryCode, Field.Index.NOT_ANALYZED);
		addField(doc, WHAT, rec.getTitle());
		addField(doc, DESCRIPTION, rec.getDescription());
		addField(doc, NUMBER, rec.getIdentifyingNumber());
		addField(doc, WHERE, rec.getPlaces());
		addField(doc, YEAR, rec.getDates());
		addField(doc, WHO, rec.getCreators());
		addField(doc, INSTITUTION_TYPE, rec.getInstitutionType());
		addField(doc, COLLECTION, rec.getCollections());
		
		RecordView recordView = rec.getRecordView();
		if(rec.getDates() != null) {
			recordView.setDate(rec.getDates().toString());
		}
		
		addField(doc, FULL_SEARCH, recordView.getFullSearchData());
		addField(doc, SET_SPEC, rec.getSetSpec().get(0), Index.NOT_ANALYZED, Store.YES);
		addField(doc, DATE_CREATED, DATE_CREATED_FORMAT.format(rec.getDateCreated()), Index.NOT_ANALYZED, Store.YES);
		
		if(rec.getCachedThumbnailUrl() == null)
			addField(doc, DIGITAL, "false");
		else
			addField(doc, DIGITAL, "true");
		
		// fields for sorting
		List<String> dates = rec.getDates();
		if(dates != null && dates.size() > 0) {
			addField(doc, SortableField.TIME_SORT, dates.get(0), Field.Index.NOT_ANALYZED);
		}
		
		List<String> types = rec.getTypes();
		if(types != null && types.size() > 0) {
			addField(doc, SortableField.TYPE_SORT, types.get(0), Field.Index.NOT_ANALYZED);
		}
		
	
		addField(doc, SortableField.NAME_SORT, rec.getTitle(), Field.Index.NOT_ANALYZED);
		
		List<String> institutions = rec.getInstitutions();
		if(institutions != null && institutions.size() > 0) {
			addField(doc, FROM, institutions);
			addField(doc, SortableField.INSTITUTION_SORT, institutions.get(0), Field.Index.NOT_ANALYZED);
		}
		
		doc.add(new Field(RECORD_VIEW.name(), recordView.serialize(), Store.YES, Index.NO));
		
		return doc;
	}

	private void addField(Document doc, IndexedFields indexedFields, Object data, Field.Index index) {
		addField(doc, indexedFields, data, index, Field.Store.NO);
	}
	
	private void addField(Document doc, IndexedFields indexedFields, Object data, Field.Index index, Field.Store store) {
		if (data != null)
			doc.add(new Field(indexedFields.name(), data.toString(), store, index, Field.TermVector.YES));
	}

	private void addField(Document doc, IndexedFields indexedFields, Object data) {
		addField(doc,indexedFields, data, Field.Index.ANALYZED);
	}

	private void addField(Document doc, SortableField sortableField, Object data, Field.Index index) {
		if (data != null)
			doc.add(new Field(sortableField.name(), data.toString().toLowerCase(), Field.Store.YES, index, Field.TermVector.YES));
	}


	public List<Document> getDocuments(String[] ids) {
		Query query = buildQuery(ID, ids);
		return search(query, SortableField.RELEVANCE, ids.length).getResult();
	}

	private Query buildQuery(IndexedFields field, String[] values) {
		BooleanQuery booleanQuery = new BooleanQuery();
		for (String string : values) {
			booleanQuery.add(new TermQuery(new Term(field.name(), string)), Occur.SHOULD);
		}
		return booleanQuery;
	}

	public Result search(String pharse, SortableField sort, int maxResult) throws Exception {
		QueryParser qp = new QueryParser(Version.LUCENE_CURRENT, 
			WHAT.name(), 
			analyzer);
		Query q ;
		if(pharse == null || pharse.length()==0)
			q = new MatchAllDocsQuery();
		else 
			q = qp.parse(pharse);
		return search(q, sort, maxResult);
	}


	public Result search(Query query, SortableField sort, int maxResult) {
		List<Document> result = new ArrayList<Document>();
		
		TopDocs docs;
		IndexSearcher searcher = null;
		try {
			logger.debug(query.toString());
			
			searcher = openSearcher();
			if(sort == null || SortableField.RELEVANCE.equals(sort))
				docs = searcher.search(query, maxResult);
			else
				docs = searcher.search(query, null, maxResult, new Sort(new SortField(sort.name(), SortField.STRING)));
			
			for (int i = 0; i < docs.scoreDocs.length; i++) {
				result.add(searcher.doc(docs.scoreDocs[i].doc));
			}
			
			return new Result(result, docs.totalHits);
		} catch (Exception e) {
			throw new RuntimeException("Failed to search index", e);
		} finally {
			closeSearcher(searcher);
		}
	}

	public TopDocs search(IndexSearcher searcher, Date from, Date until, String set, String digital, String id) {
		BooleanQuery booleanQuery = new BooleanQuery();
		if(id != null)
			booleanQuery.add(new BooleanClause(new TermQuery(new Term("ID", id)), Occur.MUST));
		
		if(digital != null)
			booleanQuery.add(new BooleanClause(new TermQuery(new Term("DIGITAL", digital)), Occur.MUST));
		
		if(set != null && set.length() > 0)
			booleanQuery.add(new BooleanClause(new TermQuery(new Term("SET_SPEC", set)), Occur.MUST));
		
		if(from != null || until != null) {
			if(from != null && until != null) {
				booleanQuery.add(new BooleanClause(new TermRangeQuery(
						"DATE_CREATED", 
						DATE_CREATED_FORMAT.format(from), 
						DATE_CREATED_FORMAT.format(until), true, true), Occur.MUST));
			} else if(from != null) {
				booleanQuery.add(new BooleanClause(new TermQuery(new Term(
						"DATE_CREATED", 
						DATE_CREATED_FORMAT.format(from))), Occur.MUST));
			} else {
				booleanQuery.add(new BooleanClause(new TermQuery(new Term(
						"DATE_CREATED", 
						DATE_CREATED_FORMAT.format(until))), Occur.MUST));
			}
		} 
		
		try {
			return searcher.search(booleanQuery.getClauses().length > 0 ? booleanQuery : new MatchAllDocsQuery(), 1000000);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void index() {
		long start = System.currentTimeMillis();
		
		logger.info("Initializing database indexing @ " + new Date());

		final Holder<Integer> totalCount = new Holder<Integer>();
		final Map<String, Integer> digitalCount = new HashMap<String, Integer>();

		repository.iterateAllRecordsForIndexing(new RecordHandler() {
			
			@Override
			public void handleRecord(Record rec, String code) {
				if(rec != null) {
					
					// FIXME: HACK. Clean up database of wrong muis id format
					if(code.equals(muisRepoHash) && isBlank(rec.getImageUrl())) {
						logger.debug("Deleting record without image from Muis Repo. Record id = " + rec.getId() + ", urlToRecord = " + rec.getUrlToRecord());
						repository.deleteRecord(rec.getId(), muisRepoHash);
						return;
					}
					
					totalCount.setValue(totalCount.getValue() != null ? totalCount.getValue() + 1 : 1);
					
					if(totalCount.getValue() % 1000 == 0) {
						logger.debug("Commiting index @ record: " + totalCount);
						try {
							writer.commit();
						} catch (Exception e) {
							logger.error("Commiting index failed", e);
						}
					}
					
					if(rec.getCachedThumbnailUrl() != null) {
						Integer value = digitalCount.get(code);
						
						digitalCount.put(code, (value != null ? value + 1 : 0));
						
						if(rec.getCachedThumbnailUrl().equals("d41d8cd98f00b204e9800998ecf8427e")) {
							logger.warn("Detected no thumbnail data for record: " + rec.getId() + ". Media url: " + rec.getImageUrl());
						}
					}
					
					try {
						writer.addDocument(getDocument(rec, code));
					} catch (Exception e) {
						logger.error("Add document failed", e);
					} 
				}
			}
		});

		logger.debug("Indexing finished @ " + new Date() + ", took: " + (System.currentTimeMillis() - start) + " ms. Metadata count: " + totalCount + ". Media count: " + digitalCount);
		
		start = System.currentTimeMillis();
		
		try {
			writer.commit();
			writer.forceMerge(1, true);
		} catch (Exception e) {
			logger.error("Index merging failed", e);
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
				logger.error("Error closing index writer", e);
			}
		}
		
		logger.debug("Index merging finished @ " + new Date() + ", took: " + (System.currentTimeMillis() - start) + " ms");
		
		start = System.currentTimeMillis();
		
		try {
			synchronized (searchers) {
				logger.debug("Rotating index! Opened searchers: " + searchers.size());
				
				Tracer.trace();
				
				while(searchers.size() > 0) {
					searchers.wait();
				}
				
				rotateIndex();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		this.updateTimestamp = System.currentTimeMillis();
		
		logger.debug("Index made available @ " + new Date() + ", took: " + (updateTimestamp - start) + " ms");
	}

	private void rotateIndex() throws CorruptIndexException, IOException {
		File searcherDirectory = new File(indexDirectory, AVAILABLE);
		for (File file : searcherDirectory.listFiles()) {
			file.delete();
		}
		
		File writerDirectory = new File(indexDirectory, ACTIVE);
		for (File file : writerDirectory.listFiles()) {
			file.renameTo(new File(searcherDirectory, file.getName()));
		}
		
		this.writer = new IndexWriter(
				FSDirectory.open(writerDirectory), 
				analyzer, 
				MaxFieldLength.UNLIMITED);
	}
}
