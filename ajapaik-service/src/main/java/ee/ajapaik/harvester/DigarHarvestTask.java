package ee.ajapaik.harvester;

import java.util.ArrayList;
import java.util.List;

import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.RecordType;
import org.openarchives.oai._2.StatusType;

import ee.ajapaik.model.search.InstitutionType;
import ee.ajapaik.model.search.Record;
import ee.ajapaik.util.IOHandler;
import ee.ajapaik.util.JaxbUtil;

public class DigarHarvestTask extends HarvestTask {
	
	@Override
	protected Record mapRecord(RecordType jaxbRecord) {
		Record rec = new Record();
		HeaderType header = jaxbRecord.getHeader();
		StatusType status = header.getStatus();
		JaxbUtil jaxbUtil = new JaxbUtil();
		
		rec.setDeleted(status!=null && "deleted".equalsIgnoreCase(status.value()));
		rec.setId(header.getIdentifier());
		List<String> collection = new ArrayList<String>();
		collection.add("DIGAR");
		rec.setCollections(collection);
		eu.europeana.schemas.ese.Metadata eseMetadata  = (eu.europeana.schemas.ese.Metadata)jaxbRecord.getMetadata().getAny();		
		eu.europeana.schemas.ese.Record eseRec = eseMetadata.getRecord().get(0);

		rec.setCreators(jaxbUtil.getValues(eseRec, "creator","publisher"));
		rec.setTitle(jaxbUtil.getValue(eseRec, "title"));
		rec.setDescription(jaxbUtil.getValue(eseRec, "description"));
		rec.setDates(jaxbUtil.getValues(eseRec, "date"));
		
		List<String> types = jaxbUtil.getValues(eseRec, "type");
		for(int i = 0; i<types.size(); i++){
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
		rec.setUrlToRecord(jaxbUtil.getValue(eseRec, "isShownAt"));
		
		List<String> institutions = jaxbUtil.getValues(eseRec, "institution");
		institutions.add(infoSystem.getName());
		
		rec.setInstitutions(institutions);
		rec.appendDescription(jaxbUtil.getValues(eseRec, "subject"));
		
		String thumbnailUrl = jaxbUtil.getValue(eseRec, "object");
		if(thumbnailUrl != null) {
			rec.setImageUrl(thumbnailUrl);
			rec.setCachedThumbnailUrl(IOHandler.saveThumbnail(thumbnailUrl, repository, taskCode));
		}
		
		rec.setInstitutionType(InstitutionType.LIBRARY);
		
		return rec;
	}
}
