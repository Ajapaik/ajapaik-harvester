package ee.ajapaik.index;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

import ee.ajapaik.db.Repository;
import ee.ajapaik.model.search.Record;
import ee.ajapaik.util.IOHandler;
import ee.ajapaik.xml.MediaHandler;

public class MediaUpdater {
	
	private static final Logger logger = Logger.getLogger(MediaUpdater.class);

	public static void updateMediaInfo(Repository repository, String taskCode, Record rec) {
		if(rec.getMediaId() == null && rec.getMediaOrder() == null) {
			if(rec.getId().contains("_")) {
				String[] idSplit = rec.getId().split("_");
				String muisId = idSplit[0].split(":")[2];
				
				rec.setMediaId(Integer.valueOf(idSplit[1]));
				
				logger.debug("Update record: " + rec.getId() + ". mediaId: " + idSplit[1]);
			
				List<String> medias = getMedias(muisId);
				for (int i = 0; i < medias.size(); i++) {
					String url = medias.get(i);
					Integer mediaId = getMediaId(url);
					
					if(rec.getMediaId().equals(mediaId)) {
						rec.setMediaOrder(i);
						
						logger.debug("Update record: " + rec.getId() + ". mediaOrder: " + i);
						
						break;
					}
				}
				
				repository.saveSingleRecord(rec.getId(), rec, taskCode);
			} else {
				repository.deleteRecord(rec.getId(), taskCode);
	
				String muisId = rec.getId().split(":")[2];
				
				List<String> medias = getMedias(muisId);
				if(medias != null && medias.size() > 0) {
					Integer mediaId = getMediaId(medias.get(0));
					
					logger.debug("Update record: " + rec.getId());
					
					rec.setId(rec.getId() + "_" + mediaId);
					rec.setMediaId(mediaId);
					rec.setMediaOrder(0);
					
					logger.debug("Updated - id: " + rec.getId() + ", mediaId: " + mediaId + ", mediaOrder: " + 0);
				}
				
				repository.saveSingleRecord(rec.getId(), rec, taskCode);
			}
		}
	}

	private static List<String> getMedias(String muisId) {
		List<String> medias = null;
		try {
			medias = parseMediaList("http://www.muis.ee/rdf/media-list/" + muisId);
		} catch (Exception e) {
			logger.warn("MediaList returned error", e);
		}
		return medias;
	}

	private static Integer getMediaId(String url) {
		String[] urlSplit = url.split("\\/");
		String mediaId = urlSplit[urlSplit.length - 1];

		if (mediaId != null) {
			try {
				return Integer.valueOf(mediaId);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	private static List<String> parseMediaList(String about) throws Exception {
		MediaHandler mediaHandler = new MediaHandler();
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(getSource(about), mediaHandler);

		return mediaHandler.getMedias();
	}

	private static InputSource getSource(String url) throws Exception {
		InputStream is = IOHandler.openStream(new URL(url));
		Reader reader = new InputStreamReader(is, "UTF-8");

		InputSource isrc = new InputSource(reader);
		isrc.setEncoding("UTF-8");

		return isrc;
	}
}
