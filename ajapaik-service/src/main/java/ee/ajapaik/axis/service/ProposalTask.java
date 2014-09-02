package ee.ajapaik.axis.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.ajapaik.dao.AjapaikDao;
import ee.ajapaik.model.Location;
import ee.ajapaik.model.MediaView;

public class ProposalTask extends QuartzJobBean {

	private static final Logger logger = Logger.getLogger(ProposalTask.class);
	
	private ProposalServiceClient proposalServiceClient;
	private AjapaikDao ajapaikDao;
	
	public void setProposalServiceClient(ProposalServiceClient proposalServiceClient) {
		this.proposalServiceClient = proposalServiceClient;
	}

	public void setAjapaikDao(AjapaikDao ajapaikDao) {
		this.ajapaikDao = ajapaikDao;
	}
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		try {
			List<MediaView> list = ajapaikDao.getMediaViewsForProposal();
			for (MediaView mediaView : list) {
				if(!mediaView.getLinkProposed()) {
					String link = ajapaikDao.getPermalink(mediaView.getIdentifier());
					if(link != null) {
						try {
							proposalServiceClient.proposePermalink(mediaView, "http://staging.ajapaik.ee/foto/" + link);

							ajapaikDao.updateLinkProposed(mediaView.getId());
						} catch (Exception e) {
							logger.error("Error while proposed URL '" + link + "' for " + mediaView, e);
						}
					}
				}
				
				if(!mediaView.getLocationProposed()) {
					Location location = ajapaikDao.getLocation(mediaView.getIdentifier());
					if(location != null) {
						try {
							proposalServiceClient.proposeLocation(mediaView, location);
							
							ajapaikDao.updateLocationProposed(mediaView.getId());
						} catch (Exception e) {
							logger.error("Error while proposed Location '" + location + "' for " + mediaView, e);
						}
					}
				}
			}
		} catch(Exception e) {
			logger.error("Proposal task throw unhandeled error", e);
		}
	}
}