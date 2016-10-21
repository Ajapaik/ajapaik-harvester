package ee.ajapaik.schedule;

import ee.ajapaik.model.InfoSystem;
import ee.ajapaik.persist.SerializingPersister;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdScheduler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.quartz.JobDetailBean;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * @author <a href="mailto:kaido@quest.ee?subject=Scheduler">Kaido Kalda</a>
 */
public class Scheduler implements BeanFactoryAware, InitializingBean {
	private static final String JOB_MAP_INFO_SYSTEM = "infoSystem";
	private static final String JOB_MAP_HARVEST_JOB_LISTENER = "harvestJobListener";
	
	public static final String JOB_PROPOSAL_NAME = "proposal";
	public static final String JOB_INDEXER_NAME = "indexer";
	public static final String JOB_AIS_NAME = "ais";
	
	private static final Logger logger = Logger.getLogger(Scheduler.class);
	
	
	private HarvestJobListener harvestJobListener = new HarvestJobListener();
	
	private BeanFactory beanFactory;
	private Map<String, Properties> configuredInfoSystems;
	private StdScheduler schedulerFactory;
	private SerializingPersister persister;
	private String indexerCronExpression;
	private String proposalCronExpression;
	
	public void setProposalCronExpression(String proposalCronExpression) {
		this.proposalCronExpression = proposalCronExpression;
	}

	public HarvestJobListener getHarvestJobListener() {
		return harvestJobListener;
	}

	public void setIndexerCronExpression(String indexerCronExpression) {
		this.indexerCronExpression = indexerCronExpression;
	}

	public void setPersister(SerializingPersister persister) {
		this.persister = persister;
	}

	public void setSchedulerFactory(StdScheduler schedulerFactory) {
		this.schedulerFactory = schedulerFactory;
	}

	public void setConfiguredInfoSystems(Map<String, Properties> configuredInfoSystems) {
		this.configuredInfoSystems = configuredInfoSystems;
	}

	public List<InfoSystem> getInfoSystems() {
		return persister.load(InfoSystem.class);
	}

	public void updateInfoSystem(InfoSystem infoSystem) {
		updateConfiguration(infoSystem);
	}
	
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		List<InfoSystem> savedConf = getInfoSystems();
		for (Entry<String, Properties> entry : configuredInfoSystems.entrySet()) {
			Properties value = entry.getValue();
			
			InfoSystem is = new InfoSystem(entry.getKey());
			is.setAddress(value.getProperty("address"));
			is.setUseSet(value.getProperty("useSet"));
			is.setMapper(value.getProperty("mapper"));
			is.setHomepageUrl(value.getProperty("homepageUrl"));
			is.setEmail(value.getProperty("email"));
			is.setSchedule(value.getProperty("schedule"));
			is.setIgnoreSet(value.getProperty("ignoreSet"));
			is.setDisableSets(Boolean.valueOf(value.getProperty("disableSets")));
			is.setMetadataPrefix(String.valueOf(value.getProperty("metadataPrefix")));
			
			if(savedConf != null && savedConf.contains(is)) {
				InfoSystem infoSystem = savedConf.get(savedConf.indexOf(is));
				is.setLastHarvestTime(infoSystem.getLastHarvestTime());
			}
			
			persister.save(is);
		}
		
		startSchedulerFactory();
	}

	private void updateConfiguration(InfoSystem infoSystem) {
		for (InfoSystem is : getInfoSystems()) {
			if(is.equals(infoSystem)) {
				is.setSchedule(infoSystem.getSchedule());				
				persister.save(is);
			}
		}
		scheduleHarvest();
	}

	private void scheduleHarvest() {
		for (InfoSystem infoSystem : getInfoSystems()) {
			try {
				schedulerFactory.unscheduleJob(infoSystem.getName(), null);
				
				String schedule = infoSystem.getSchedule();
				if(schedule != null) { 
					JobDetailBean job = (JobDetailBean) beanFactory.getBean("harvesterJob");
					job.setJobClass(Class.forName(infoSystem.getMapper()));
					job.setName(infoSystem.getName());
					job.getJobDataMap().put(JOB_MAP_INFO_SYSTEM, infoSystem);
					
					CronTrigger cronTrigger = getCronTrigger(infoSystem);
					if(cronTrigger != null)
						schedulerFactory.scheduleJob(job, cronTrigger);
				}
			} catch (SchedulerException e) {
				logger.error("Scheduler failed to schedule harvest job: ", e);
			} catch (ClassNotFoundException e) {
				logger.error("No mapper found for harvesting: ", e);
			} catch (ParseException e) {
				logger.error("Failed to parse cron expression: ", e);
			}
		}
	}

	private CronTrigger getCronTrigger(InfoSystem infoSystem) throws ParseException {
		String schedule = infoSystem.getSchedule();
		
		CronTrigger cronTrigger = new CronTrigger(infoSystem.getName());
		cronTrigger.setCronExpression(schedule);
		return cronTrigger;
	}

	private void startSchedulerFactory() {
		try {
			schedulerFactory.start();
			schedulerFactory.addGlobalJobListener(harvestJobListener);
			
			scheduleIndexing();
			scheduleHarvest();
			scheduleProposal();
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
	}

	public void scheduleAIS(Long taskId) {
		String name = Scheduler.JOB_AIS_NAME + "_" + taskId;
		
		SimpleTrigger trigger = new SimpleTrigger(name);
		trigger.setJobName(name);
		
		try {
			JobDetailBean job = (JobDetailBean) beanFactory.getBean("aisJob");
			job.setName(name);
			job.getJobDataMap().put("taskId", taskId);

			schedulerFactory.scheduleJob(job, trigger);
		} catch (Exception e) {
			logger.error("Scheduler failed to schedule AIS job: ", e);
		}
	}

	private void scheduleIndexing() {
		CronTrigger cronTrigger = new CronTrigger(JOB_INDEXER_NAME, null);
		try {
			cronTrigger.setCronExpression(indexerCronExpression);
			scheduleIndexing(cronTrigger);
		} catch (ParseException e) {
			logger.error("Failed to parse cron expression: ", e);
		}
	}
	
	private void scheduleProposal() {
		CronTrigger cronTrigger = new CronTrigger(JOB_PROPOSAL_NAME, null);
		try {
			cronTrigger.setCronExpression(proposalCronExpression);

			scheduleProposal(cronTrigger);
		} catch (ParseException e) {
			logger.error("Failed to parse cron expression: ", e);
		}
	}
	
	public void scheduleProposal(Trigger trigger) {
		JobDetailBean job = (JobDetailBean) beanFactory.getBean("proposalJob");
		try {
			if(schedulerFactory.getJobDetail(JOB_PROPOSAL_NAME, null) != null) {
				Trigger oldTrigger = schedulerFactory.getTrigger(JOB_PROPOSAL_NAME, null);
				schedulerFactory.rescheduleJob(JOB_PROPOSAL_NAME, null, trigger);
				Thread.sleep(1000);		
				schedulerFactory.rescheduleJob(JOB_PROPOSAL_NAME, null, oldTrigger);
			} else {
				schedulerFactory.scheduleJob(job, trigger);
			}
		} catch (Exception e) {
			logger.error("Scheduler failed to schedule proposal job: ", e);
		}
	}
	
	public void scheduleIndexing(Trigger trigger) {
		logger.debug("Started scheduling indexing");
		JobDetailBean job = (JobDetailBean) beanFactory.getBean("indexerJob");
		job.getJobDataMap().put(JOB_MAP_HARVEST_JOB_LISTENER, harvestJobListener);
		try {
			if(schedulerFactory.getJobDetail(JOB_INDEXER_NAME, null) != null) {
				Trigger oldTrigger = schedulerFactory.getTrigger(JOB_INDEXER_NAME, null);
				schedulerFactory.rescheduleJob(JOB_INDEXER_NAME, null, trigger); 		// reschedule index with new trigger
				Thread.sleep(1000);														// Wait trigger to fire
				schedulerFactory.rescheduleJob(JOB_INDEXER_NAME, null, oldTrigger);		// reschedule index with old trigger again
			} else {
				schedulerFactory.scheduleJob(job, trigger);
			}
		} catch (Exception e) {
			logger.error("Scheduler failed to schedule indexer job: ", e);
		}
		logger.debug("Finished scheduling indexing");
	}
	
	public class HarvestJobListener implements JobListener {
		private ThreadLocal<String> name = new ThreadLocal<String>();
		
		public boolean hasActiveHarvestJobs() {
			boolean result = false;
			
			for (Object object : schedulerFactory.getCurrentlyExecutingJobs()) {
				JobExecutionContext ctx = (JobExecutionContext) object;
				
				result |= (!JOB_INDEXER_NAME.equals(ctx.getJobDetail().getName()));
			}
			
			return result;
		}
		
		public boolean hasActiveIndexerJobs() {
			for (Object object : schedulerFactory.getCurrentlyExecutingJobs()) {
				JobExecutionContext ctx = (JobExecutionContext) object;
				
				if(JOB_INDEXER_NAME.equals(ctx.getJobDetail().getName()))
					return true;
			}
			return false;
		}
		
		@Override
		public void jobWasExecuted(JobExecutionContext context, JobExecutionException arg1) {
			JobDetail jobDetail = context.getJobDetail();
			if(jobDetail.getJobDataMap().containsKey(JOB_MAP_INFO_SYSTEM)) {
				InfoSystem is = (InfoSystem) context.getJobDetail().getJobDataMap().get(JOB_MAP_INFO_SYSTEM);
				is.setRunning(Boolean.FALSE);
				
				persister.save(is);
			}
			
			logger.info("Setting job '" + jobDetail.getName() + "' state to STOPPED!");
			
			synchronized (harvestJobListener) {
				harvestJobListener.notifyAll(); // Notifies IndexerTask to start scheduled operation
			}
			
			Thread.currentThread().setName(name.get());
		}
		
		@Override
		public void jobToBeExecuted(JobExecutionContext context) {
			logger.debug("Job to be executed!");
			Thread currentThread = Thread.currentThread();
			name.set(currentThread.getName());
			logger.debug("name set");
			
			synchronized (harvestJobListener) {
				logger.debug("Syncronized");
				while(harvestJobListener.hasActiveIndexerJobs()) {
					logger.debug("Active indexer jobs");
					try {
						harvestJobListener.wait();
					} catch (InterruptedException e) {
						logger.error("HarvestTask was interrupted: ", e);
					}
				}
			}
			logger.debug("No more active indexer jobs");
			
			JobDetail jobDetail = context.getJobDetail();
			logger.debug("Jobdetail = " + jobDetail.getName());
			if(jobDetail.getJobDataMap().containsKey(JOB_MAP_INFO_SYSTEM)) {
				logger.debug("JobDetail contains key");
				InfoSystem is = (InfoSystem) jobDetail.getJobDataMap().get(JOB_MAP_INFO_SYSTEM);
				logger.debug("Infosystem found! is = " + is.getName());
				is.setRunning(Boolean.TRUE);
				logger.debug("Is is set running");
				persister.save(is);
				logger.debug("IS Saved");
			}
			
			logger.info("Setting job '" + jobDetail.getName() + "' state to STARTED!");

			currentThread.setName(jobDetail.getName());
		}

		@Override
		public void jobExecutionVetoed(JobExecutionContext arg0) {
		}
		
		@Override
		public String getName() {
			return "globalJobListener";
		}
	}
}
