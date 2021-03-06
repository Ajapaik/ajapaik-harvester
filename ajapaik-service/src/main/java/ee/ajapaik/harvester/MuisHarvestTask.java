package ee.ajapaik.harvester;

import ee.ajapaik.model.search.InstitutionType;
import ee.ajapaik.model.search.Record;
import ee.ajapaik.util.IOHandler;
import ee.ajapaik.util.JaxbUtil;
import ee.ajapaik.xml.MediaHandler;
import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.MetadataType;
import org.openarchives.oai._2.RecordType;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class MuisHarvestTask extends HarvestTask {

	@Override
	protected Record mapRecord(RecordType recordType) {
		Record rec = new Record();
		HeaderType header = recordType.getHeader();
		rec.setId(header.getIdentifier());

		List<String> collections = new ArrayList<String>();
		List<String> setSpec = header.getSetSpec();
		for (String collection : setSpec) {
			try {
				String[] collectionString = super.getSets().get(collection).split("\\|");

				String string = collectionString[0];
				if(collectionString.length > 1) {
					 string = string + ": " + collectionString[1];
				}
				collections.add(string);
				
				List<String> institutions = new ArrayList<String>();
				institutions.add(collectionString[0]);
				institutions.add(infoSystem.getName());
				
				rec.setInstitutions(institutions);
			} catch (Exception e) {
				logger.error("Unknown setSpec name: " + collection);
				return null;
			}
		}
		rec.setCollections(collections);

		MetadataType metadata = recordType.getMetadata();
		JaxbUtil jaxbUtil = new JaxbUtil();

		rec.setDeleted(metadata == null);
		if (!rec.isDeleted()) {
			eu.europeana.schemas.ese.Record eseRec = (eu.europeana.schemas.ese.Record) recordType
					.getMetadata().getAny();
			
			rec.setIdentifyingNumber(jaxbUtil.getValue(eseRec, "identifier"));
			rec.setCreators(jaxbUtil.getValues(eseRec, "creator", "publisher"));
			rec.setTitle(jaxbUtil.getValue(eseRec, "title"));
			String description = jaxbUtil.getValue(eseRec, "description");
			rec.setDescription(description);
			rec.setProviderName(jaxbUtil.getValue(eseRec, "provider"));
			rec.setUrlToRecord(jaxbUtil.getValue(eseRec, "isShownAt"));
			
			List<String> dates = jaxbUtil.getValues(eseRec, "date");
			Pattern pat = Pattern.compile("\\d+");
			Matcher mat = pat.matcher(description);
			while (mat.find()) {
				if (mat.group().length() == 4)
					dates.add(mat.group());
			}
			rec.setDates(dates);
			
			List<String> types = jaxbUtil.getValues(eseRec, "type");
			for (int i = 0; i < types.size(); i++) {
				String type = types.get(i);
				type = type.replace("IMAGE", "pilt");
				type = type.replace("SOUND", "heli");
				type = type.replace("VIDEO", "video");
				type = type.replace("TEXT", "tekst");
				types.set(i, type);
			}
			rec.setTypes(types);
			rec.setProviderHomepageUrl(infoSystem.getHomepageUrl());
			rec.setProviderName(infoSystem.getName());
			rec.setInstitutionType(InstitutionType.MUSEUM);
			rec.setCollectionType(infoSystem.getCollectionType());

			String thumbnailUrl = jaxbUtil.getValue(eseRec, "object");
			if(isNotBlank(thumbnailUrl)) {
				String[] split = rec.getId().split(":");
				List<String> medias = null;
				try {
					medias = parseMediaList("http://www.muis.ee/rdf/media-list/" + split[2]);
				} catch (Exception e) {
					logger.warn("MediaList returned error", e);
				}

				if (medias != null && medias.size() > 0) {
					for (int i = 0; i < medias.size(); i++) {
						String url = medias.get(i);
						Integer mediaId = getMediaId(medias.get(i));

						Record cloned = rec.clone();
						cloned.setId(rec.getId() + "_" + mediaId);
						cloned.setMediaId(mediaId);
						cloned.setMediaOrder(i);
						cloned.setImageUrl(url);
						save(cloned, header.getSetSpec());
					}
					return null;
				} else {
					rec.setImageUrl(getImageUrl(thumbnailUrl));
				}
			}
		} else {
			rec.setTitle("Deleted");
		}
		
		return rec;
	}

	Integer getMediaId(String url) {
		String[] urlSplit = url.split("\\/");
		return urlSplit[urlSplit.length - 1].hashCode();
	}
	
	private List<String> parseMediaList(String about) throws Exception {
		MediaHandler mediaHandler = new MediaHandler();
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(getSource(about), mediaHandler);

		return mediaHandler.getMedias();
	}
	
	private InputSource getSource(String url) throws Exception {
		InputStream is = IOHandler.openStream(new URL(url));
		Reader reader = new InputStreamReader(is, "UTF-8");
		 
		InputSource isrc = new InputSource(reader);
		isrc.setEncoding("UTF-8");
		
		return isrc;
	}
	
	private String getImageUrl(String thumbnailUrl) {
		if(thumbnailUrl != null && thumbnailUrl.length() > 0)
			return thumbnailUrl.replaceFirst("museaalThumbnail", "museaalImage");
		
		return null;
	}
}
