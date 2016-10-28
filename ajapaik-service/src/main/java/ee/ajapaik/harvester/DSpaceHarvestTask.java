package ee.ajapaik.harvester;

import ee.ajapaik.model.search.InstitutionType;
import ee.ajapaik.model.search.Record;
import ee.ajapaik.util.IOHandler;
import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.MetadataType;
import org.openarchives.oai._2.RecordType;
import org.openarchives.oai._2_0.oai_dc.OaiDcType;
import org.purl.dc.elements._1.ElementType;
import org.purl.dc.elements._1.SimpleLiteral;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;

import static ee.ajapaik.model.search.InstitutionType.DSPACE;
import static java.util.Collections.singletonList;

public class DSpaceHarvestTask extends HarvestTask {
	
	@Override
	@SuppressWarnings("unchecked")
	protected Record mapRecord(RecordType recordType) {
		HeaderType header = recordType.getHeader();
		MetadataType metadata = recordType.getMetadata();
		
		List<String> institutions = new ArrayList<String>();
		institutions.add(infoSystem.getName());
		
		Record rec = new Record();
		rec.setId(header.getIdentifier());
		rec.setInstitutions(institutions);

		rec.setDeleted(metadata == null);

		rec.setInstitutionType(InstitutionType.DSPACE);
		
		if (!rec.isDeleted()) {
			OaiDcType any = ((JAXBElement<OaiDcType>) metadata.getAny()).getValue();
			List<JAXBElement<ElementType>> data = any.getTitleOrCreatorOrSubject();
			
			String type = getSingleValue(data, "type");
			if("image".equalsIgnoreCase(type)) {
				rec.setTitle(getSingleValue(data, "title"));
				rec.setCreators(getValue(data, "creator"));
				rec.setUrlToRecord(getValues(data, "identifier").get(1));
				rec.setDates(singletonList(getValues(data, "date").get(2)));
				rec.setTypes(singletonList(type));
				rec.setDescription(getSingleValue(data, "description"));
				rec.setIdentifyingNumber(getSingleValue(data, "identifier"));
				rec.setProviderHomepageUrl(infoSystem.getHomepageUrl());
				rec.setProviderName(infoSystem.getName());
				rec.setInstitutionType(DSPACE);

				List<String> identifiers = getValues(data, "identifier");
				int mediaOrder = 0;
				for (String identifier : identifiers) {
					if (!identifier.contains(".tif.") && identifier.contains("http") && identifier.contains(".jpg") && !identifier.contains(".jpg.jpg")) {
						Record clone = rec.clone();
						Integer mediaId = Integer.valueOf(rec.getId().split(":")[2].split("/")[1]);
						clone.setId(rec.getId() + "_" + mediaId);
						clone.setImageUrl(identifier);
						clone.setCachedThumbnailUrl(IOHandler.saveThumbnail(getThumbnailUrl(identifiers, identifier), repository, taskCode));
						clone.setMediaId(mediaId);
						clone.setMediaOrder(mediaOrder++);
						save(clone, recordType.getHeader().getSetSpec());
					}
				}
			}
		}
		return null;
	}

	private String getThumbnailUrl(List<String> identifiers, String identifier) {
		String thumbnailUrl = null;
		String[] split = identifier.split("/");
		String fileName = split[split.length - 1];
		for (String identifierForThumbnail : identifiers) {
            if (identifierForThumbnail.contains("/" + fileName + ".jpg")) {
                thumbnailUrl = identifierForThumbnail;
                break;
            }
        }
		return thumbnailUrl;
	}

	private String getSingleValue(List<JAXBElement<ElementType>> data, String key) {
		List<String> result = getValue(data, key);
		if(result != null && result.size() > 0) {
			return result.get(0);
		}
		return null;
	}

	private List<String> getValue(List<JAXBElement<ElementType>> data, String key) {
		for (JAXBElement<ElementType> jaxbElement : data) {
			String name = jaxbElement.getName().getLocalPart();
			List<String> content = getContent(jaxbElement);
			
			if(key.equals(name)) {
				return content;
			}
		}
		return null;
	}

	private List<String> getValues(List<JAXBElement<ElementType>> data, String key) {
		List<String> result = new ArrayList<String>();
		for (JAXBElement<ElementType> jaxbElement : data) {
			String name = jaxbElement.getName().getLocalPart();
			List<String> content = getContent(jaxbElement);

			if(key.equals(name)) {
				result.addAll(content);
			}
		}
		return result;
	}

	private List<String> getContent(JAXBElement<ElementType> jaxbElement) {
		Object value = jaxbElement.getValue();
		
		return ((SimpleLiteral) value).getContent();
	}
}