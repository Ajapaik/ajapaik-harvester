package ee.ajapaik.index;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.ajapaik.schedule.Scheduler.HarvestJobListener;

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
		synchronized (harvestJobListener) {
			while(harvestJobListener.hasActiveHarvestJobs()) {	// Loop until all harvest jobs are completed
				try {
					harvestJobListener.wait();										// Wait harvest job to finish
				} catch (InterruptedException e) {
					logger.error("IndexerTask was interrupted: ", e);
				}
			}
			
			indexer.index();
			
			harvestJobListener.notifyAll();
		}
	}
}
