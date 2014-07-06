package ee.ajapaik.harvester;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.MetadataType;
import org.openarchives.oai._2.RecordType;

import ee.ajapaik.model.search.InstitutionType;
import ee.ajapaik.model.search.Record;
import ee.ajapaik.util.IOHandler;
import ee.ajapaik.util.JaxbUtil;

public class MuisHarvestTask extends HarvestTask {

	@Override
	protected Record mapRecord(RecordType jaxbRecord) {
		Record rec = new Record();
		HeaderType header = jaxbRecord.getHeader();
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

		MetadataType metadata = jaxbRecord.getMetadata();
		JaxbUtil jaxbUtil = new JaxbUtil();

		rec.setDeleted(metadata == null);
		if (!rec.isDeleted()) {
			eu.europeana.schemas.ese.Record eseRec = (eu.europeana.schemas.ese.Record) jaxbRecord
					.getMetadata().getAny();
			
			rec.setIdentifyingNumber(jaxbUtil.getValue(eseRec, "identifier"));
			rec.setCreators(jaxbUtil.getValues(eseRec, "creator", "publisher"));
			rec.setTitle(jaxbUtil.getValue(eseRec, "title"));
			String description = jaxbUtil.getValue(eseRec, "description");
			rec.setDescription(description);
			rec.setProviderName(jaxbUtil.getValue(eseRec, "provider"));
			rec.setUrlToRecord(fixMuisUrl(jaxbUtil.getValue(eseRec, "isShownAt")));
			
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

			String thumbnailUrl = fixMuisUrl(jaxbUtil.getValue(eseRec, "object"));
			if(thumbnailUrl != null) {
				rec.setImageUrl(getImageUrl(thumbnailUrl));
				rec.setCachedThumbnailUrl(IOHandler.saveThumbnail(thumbnailUrl, repository, taskCode));
			}
			rec.setInstitutionType(InstitutionType.MUSEUM);
		} else {
			rec.setTitle("Deleted");
		}
		return rec;

	}
	
	private String getImageUrl(String thumbnailUrl) {
		if(thumbnailUrl != null && thumbnailUrl.length() > 0)
			return thumbnailUrl.replaceFirst("museaalThumbnail", "museaalImage");
		
		return null;
	}
	
	private String fixMuisUrl(String url) {
		if(url != null && url.length() > 0){
			return url.replaceFirst("http://muis.ee/", "http://www.muis.ee/portaal/");
		}
		
		return null;
	}
}
