package ee.ajapaik.axis.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import ee.ajapaik.cache.PhotoCache;
import ee.ajapaik.dao.AjapaikDao;
import ee.ajapaik.model.MediaView;
import ee.ajapaik.model.Photo;
import ee.ajapaik.platform.AjapaikClient;

public class ProposalTask extends QuartzJobBean {

	private static final Logger logger = Logger.getLogger(ProposalTask.class);
	
	private ProposalServiceClient proposalServiceClient;
	private AjapaikDao ajapaikDao;
	private PhotoCache photoCache;
	private AjapaikClient ajapaikClient;
	
	public void setAjapaikClient(AjapaikClient ajapaikClient) {
		this.ajapaikClient = ajapaikClient;
	}

	public void setPhotoCache(PhotoCache photoCache) {
		this.photoCache = photoCache;
	}

	public void setProposalServiceClient(ProposalServiceClient proposalServiceClient) {
		this.proposalServiceClient = proposalServiceClient;
	}

	public void setAjapaikDao(AjapaikDao ajapaikDao) {
		this.ajapaikDao = ajapaikDao;
	}
	
	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		try {
			List<MediaView> mediaViews = ajapaikDao.getMediaViewsForProposal();
			List<Photo> photos = photoCache.getData();
			for (MediaView mediaView : mediaViews) {
				Photo photo = getPhoto(photos, mediaView.getIdentifier());
				if(photo != null) {
					if(!mediaView.getLinkProposed()) {
						Integer photoId = photo.getId();
						try {
							proposalServiceClient.proposePermalink(mediaView, composePermalink(photoId));

							ajapaikDao.updateLinkProposed(mediaView.getId());
						} catch (Exception e) {
							logger.error("Error while proposed permalink for " + mediaView, e);
						}
					}
					
					if(!mediaView.getLocationProposed()) {
						if(hasLocation(photo) && photo.getConfidence() >= 0.6) {
							try {
								proposalServiceClient.proposeLocation(mediaView, photo.getLat(), photo.getLon(), photo.getAzimuth(), photo.getConfidence());
								
								ajapaikDao.updateLocationProposed(mediaView.getId());
							} catch (Exception e) {
								logger.error("Error while proposed Location for: " + mediaView, e);
							}
						} else {
							logger.debug("Insufficent data for location proposal: " + photo);
						}
					}
				}
			}
		} catch(Exception e) {
			logger.error("Proposal task throw unhandeled error", e);
		}
	}

	private Photo getPhoto(List<Photo> photos, String identifier) {
		for (Photo photo : photos) {
			if(identifier.equals(photo.getSourceKey())) {
				return photo;
			}
		}
		return null;
	}

	private boolean hasLocation(Photo photo) {
		String lat = photo.getLat();
		String lon = photo.getLon();
		
		return (lat != null && !lat.equals("") && !lat.equals("0.0")) 
				&& (lon != null && !lon.equals("") && !lon.equals("0.0"));
	}

	private String composePermalink(Integer photoId) {
		StringBuilder builder = new StringBuilder();
		builder.append(ajapaikClient.getProtocol()).append("://");
		builder.append(ajapaikClient.getHost()).append("/");
		builder.append("foto/").append(photoId);

		return builder.toString();
	}
}