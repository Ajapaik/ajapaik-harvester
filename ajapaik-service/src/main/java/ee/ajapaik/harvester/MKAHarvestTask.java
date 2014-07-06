package ee.ajapaik.harvester;

import java.util.ArrayList;
import java.util.List;

import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.MetadataType;
import org.openarchives.oai._2.RecordType;

import ee.ajapaik.model.search.InstitutionType;
import ee.ajapaik.model.search.Record;
import ee.ajapaik.util.IOHandler;
import ee.ajapaik.util.JaxbUtil;

public class MKAHarvestTask extends HarvestTask {

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
			eu.europeana.schemas.ese.Record eseRec = (eu.europeana.schemas.ese.Record) jaxbRecord.getMetadata().getAny();
			
			rec.setCreators(jaxbUtil.getValues(eseRec, "creator", "publisher"));
			rec.setTitle(jaxbUtil.getValue(eseRec, "title"));

			rec.setDescription(jaxbUtil.getValue(eseRec, "description"));
			rec.setProviderName(jaxbUtil.getValue(eseRec, "provider"));
			rec.setUrlToRecord(jaxbUtil.getValue(eseRec, "isShownAt"));

			rec.setTypes(jaxbUtil.getValues(eseRec, "type"));
			rec.setProviderHomepageUrl(infoSystem.getHomepageUrl());
			rec.setProviderName(infoSystem.getName());

			String imageUrl = jaxbUtil.getValue(eseRec, "object");
			
			if(imageUrl != null && imageUrl.length() > 0) {
				rec.setImageUrl(imageUrl);

				String thumbnailUrl = getThumbnailUrl(imageUrl);
				rec.setCachedThumbnailUrl(IOHandler.saveThumbnail(thumbnailUrl, repository, taskCode));
			}
			
			rec.setInstitutionType(InstitutionType.ARCHIVE);
		} else {
			rec.setTitle("Deleted");
		}
		return rec;

	}
	
	private String getThumbnailUrl(String url) {
		return url.replaceFirst("regular", "thumb110");
	}
}
