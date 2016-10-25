package ee.ajapaik.index;

import ee.ajapaik.schedule.Scheduler.HarvestJobListener;
import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class IndexerTask extends QuartzJobBean {
	
	private static Logger logger = Logger.getLogger(IndexerTask.class);

	private HarvestJobListener harvestJobListener;
	private Indexer indexer;
	
	public void setIndexer(Indexer indexer) {
		this.indexer = indexer;
	}

	public void setHarvestJobListener(HarvestJobListener harvestJobListener) {
		this.harvestJobListener = harvestJobListener;
	}

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		logger.debug("Execute Indexer task");
		synchronized (harvestJobListener) {
			while(harvestJobListener.hasActiveHarvestJobs()) {	// Loop until all harvest jobs are completed
				logger.debug("Active harvest jobs in progress");
				try {
					harvestJobListener.wait();										// Wait harvest job to finish
				} catch (InterruptedException e) {
					logger.error("IndexerTask was interrupted: ", e);
				}
			}
			logger.debug("No harvest jobs in progress, starting indexing");
			
			indexer.index();
			
			harvestJobListener.notifyAll();
		}
	}
}
