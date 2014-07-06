package ee.ajapaik.harvester;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.xml.sax.SAXException;

import ee.ajapaik.axis.service.ProposalServiceClient;
import ee.ajapaik.axis.service.TaskServiceClient;
import ee.ajapaik.dao.AjapaikDao;
import ee.ajapaik.db.Repository;
import ee.ajapaik.index.Indexer;
import ee.ajapaik.model.search.InstitutionType;
import ee.ajapaik.model.search.Record;
import ee.ajapaik.platform.HttpClientFactory;
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
	private ProposalServiceClient proposalServiceClient;
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

	public void setProposalServiceClient(ProposalServiceClient proposalServiceClient) {
		this.proposalServiceClient = proposalServiceClient;
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
		parser.parse(IOHandler.openStream(new URL(about + "/medialist")), mediaHandler);

		return mediaHandler.getMedias();
	}

	private Meta parsePuri(String puri) throws Exception {
		MetaHandler metaHandler = new MetaHandler();
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(IOHandler.openStream(new URL(puri + "?rdf")), metaHandler);
		
		return metaHandler.getMeta();
	}
}

