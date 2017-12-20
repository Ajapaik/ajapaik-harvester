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
				rec.setTypes(singletonList(type));
				rec.setDescription(getSingleValue(data, "description"));
				rec.setIdentifyingNumber(getSingleValue(data, "identifier"));
				rec.setProviderHomepageUrl(infoSystem.getHomepageUrl());
				rec.setProviderName(infoSystem.getName());
				rec.setInstitutionType(DSPACE);
				rec.setCollectionType(infoSystem.getCollectionType());
                addDatesAndCoordinates(rec, data);

                List<String> images = getImages(data);
				int mediaOrder = 0;
				for (String image : images) {
					if (!isThumbnail(image)) {
						Record clone = rec.clone();
						String mediaId = image.split("/")[6];
						clone.setId(rec.getId() + "_" + mediaId);
						clone.setImageUrl(image);
						clone.setCachedThumbnailUrl(IOHandler.saveThumbnail(getThumbnailUrl(images, image), repository, taskCode));
						clone.setMediaId(mediaId);
						clone.setMediaOrder(mediaOrder++);
						save(clone, recordType.getHeader().getSetSpec());
					}
				}
			}
		}
		return null;
	}

    private void addDatesAndCoordinates(Record rec, List<JAXBElement<ElementType>> data) {
        List<String> dates = new ArrayList<String>();
        dates.add(getValues(data, "date").get(2));
        List<String> coverage = getValues(data, "coverage");
        for (String param : coverage) {
            String[] latAndLong = param.split(",");
            if (isCoordinate(latAndLong)) {
                rec.setLatitude(latAndLong[0]);
                rec.setLongitude(latAndLong[1]);
                continue;
            }
            if (param.startsWith("[") && param.endsWith("]")) {
                param = param.substring(1, param.length()-1);
            }
            dates.add(param);
        }
        rec.setDates(dates);
    }

    private boolean isCoordinate(String[] latAndLong) {
        return latAndLong.length == 2 && latAndLong[0].contains(".") && latAndLong[1].contains(".");
    }

	private boolean isThumbnail(String image) {
		return image.endsWith(".jpg.jpg");
	}

	private String getThumbnailUrl(List<String> identifiers, String identifier) {
		String[] split = identifier.split("/");
		String thumbnailUrlEnding = "/" + split[split.length - 1] + ".jpg";
		for (String identifierForThumbnail : identifiers) {
            if (identifierForThumbnail.endsWith(thumbnailUrlEnding)) {
                return identifierForThumbnail;
            }
        }
		return null;
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

	private List<String> getImages(List<JAXBElement<ElementType>> data) {
		List<String> result = new ArrayList<String>();
		for (JAXBElement<ElementType> jaxbElement : data) {
			List<String> contents = getContent(jaxbElement);
			if (contents.size() < 1) continue;
			String content = contents.get(0);

			if("identifier".equals(jaxbElement.getName().getLocalPart()) && isImage(content)) {
				result.add(content);
			}
		}
		return result;
	}

	private boolean isImage(String content) {
		return !content.contains(".tif") && content.startsWith("http") && content.endsWith(".jpg");
	}

	private List<String> getContent(JAXBElement<ElementType> jaxbElement) {
		Object value = jaxbElement.getValue();
		
		return ((SimpleLiteral) value).getContent();
	}
}