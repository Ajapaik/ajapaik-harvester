package ee.ajapaik.harvester;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.xml.sax.InputSource;

import ee.ajapaik.axis.service.TaskServiceClient;
import ee.ajapaik.dao.AjapaikDao;
import ee.ajapaik.db.Repository;
import ee.ajapaik.index.Indexer;
import ee.ajapaik.model.search.InstitutionType;
import ee.ajapaik.model.search.Record;
import ee.ajapaik.util.Digester;
import ee.ajapaik.util.IOHandler;
import ee.ajapaik.xml.MediaHandler;
import ee.ajapaik.xml.MetaHandler;
import ee.ajapaik.xml.model.Meta;
import ee.ajapaik.xml.model.Task;

public class AISHarvestTask extends QuartzJobBean {
	
	private static Logger logger = Logger.getLogger(AISHarvestTask.class);

	private AjapaikDao ajapaikDao;
	private Repository repository;
	private Indexer indexer;
	private TaskServiceClient taskServiceClient;
	private Long taskId;
	
	public void setIndexer(Indexer indexer) {
		this.indexer = indexer;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public void setAjapaikDao(AjapaikDao ajapaikDao) {
		this.ajapaikDao = ajapaikDao;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}
	
	public void setTaskServiceClient(TaskServiceClient taskServiceClient) {
		this.taskServiceClient = taskServiceClient;
	}

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		String taskCode = Digester.digestToString(context.getJobDetail().getName());
		
		if(!ajapaikDao.hasTask(taskId)) {
			
			ajapaikDao.taskStarted(taskId);
			
			try {
				List<Task> list = taskServiceClient.getTaskList(taskId);
				
				logger.info("Tasks: " + list);
				
				boolean hasMedia = false;
				
				for (Task task : list) {
					List<String> puris = task.getObjectPuris();
					
					logger.info("PURIS: " + puris);
					
					for (String puri : puris) {
						Meta meta = parsePuri(puri);
						
						logger.info("Meta: " + meta);

						if(meta != null) {
							List<String> medias = parseMediaList(meta.getAbout());
							
							logger.info("Medias: " + medias);
							
							for (int i = 0; i < medias.size(); i++) {
								String media = medias.get(i);
								
								String id = meta.getIdentifier() + (medias.size() > 1 ? ("_" + i) : "");
								String thumbnailUrl = IOHandler.saveThumbnail(media, repository, taskCode);
								
								Record record = new Record();
								record.setId(id);
								record.setIdentifyingNumber(record.getId());
								record.setInstitutions(Arrays.asList(meta.getPublisher()));
								record.setTitle(meta.getTitle());
								record.setProviderName("AIS");
								record.setSetSpec(Arrays.asList("AIS"));
								record.setDateCreated(new Date());
								record.setInstitutionType(InstitutionType.AIS);
								record.setUrlToRecord(media);
								record.setCachedThumbnailUrl(thumbnailUrl);
								
								repository.saveSingleRecord(id, record, taskCode);
								
								ajapaikDao.saveMedia(id, task, meta, thumbnailUrl);
								
								hasMedia = true;
							}
						}
					}
				}
				
				if(hasMedia) {
					indexer.index();
				}
			} catch (Exception e) {
				throw new JobExecutionException("Failed to complete job", e);
			} finally {
				ajapaikDao.taskFinished(taskId);
				
				logger.info("AIS job complete");	
			}
		}
	}

	private List<String> parseMediaList(String about) throws Exception {
		MediaHandler mediaHandler = new MediaHandler();
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(getSource(about + "/medialist"), mediaHandler);

		return mediaHandler.getMedias();
	}

	private Meta parsePuri(String puri) throws Exception {
		InputSource is = getSource(puri + "?rdf");

		if(is != null) {
			MetaHandler metaHandler = new MetaHandler();
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(is, metaHandler);
			
			return metaHandler.getMeta();
		} else {
			return null;
		}
	}
	
	private InputSource getSource(String url) throws Exception {
		InputStream is = IOHandler.openStream(new URL(url));
		Reader reader = new InputStreamReader(is, "UTF-8");
		 
		InputSource isrc = new InputSource(reader);
		isrc.setEncoding("UTF-8");
		
		return isrc;
	}
}

