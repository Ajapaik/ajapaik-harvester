package ee.ajapaik.harvester;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.MetadataType;
import org.openarchives.oai._2.RecordType;
import org.openarchives.oai._2_0.oai_dc.OaiDcType;
import org.purl.dc.elements._1.ElementType;
import org.purl.dc.elements._1.SimpleLiteral;

import com.fasterxml.jackson.databind.ObjectMapper;

import ee.ajapaik.model.Page;
import ee.ajapaik.model.Pages;
import ee.ajapaik.model.search.Record;
import ee.ajapaik.model.search.InstitutionType;
import ee.ajapaik.util.IOHandler;

public class ETERAHarvestTask extends HarvestTask {
	
	private static final ObjectMapper MAPPER = new ObjectMapper();

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

		rec.setInstitutionType(InstitutionType.LIBRARY);
		
		if (!rec.isDeleted()) {
			OaiDcType any = ((JAXBElement<OaiDcType>) metadata.getAny()).getValue();
			List<JAXBElement<ElementType>> data = any.getTitleOrCreatorOrSubject();
			
			String type = getSingleValue(data, "type");
			if("Foto".equals(type)) {
				rec.setDates(getValue(data, "date"));
				rec.setTitle(getSingleValue(data, "title"));
				rec.setCreators(getValue(data, "creator"));
				rec.setUrlToRecord(getSingleValue(data, "relation"));
				
				String id = rec.getId().split(":")[2];
				try {
					URL url = new URL("http://www.etera.ee/api/item/" + id + "/pages");
					
					Map<String, String> headers = new HashMap<String, String>();
					headers.put("Authorization", "Bearer vmt6o23sgq15mltlm462lhaoc1");
					
					List<Page> pages = MAPPER.readValue(IOHandler.openStream(url, null, headers), Pages.class).getPages();
					
					int mediaOrder = 0;
					for (Page page : pages) {
						Record cloned = rec.clone();
						Integer mediaId = page.getAttributes().getId();
						
						cloned.setId(rec.getId() + "_" + mediaId);
						cloned.setCachedThumbnailUrl(IOHandler.saveThumbnail("http://www.etera.ee/api/page/" + mediaId + "/thumbnail", headers, repository, taskCode, null));
						cloned.setImageUrl("http://www.etera.ee/api/page/" + mediaId + "/imageview?size=xxl");
						
						cloned.setMediaId(mediaId);
						cloned.setMediaOrder(mediaOrder++);
						
						save(cloned, institutions);
					}
					return null;
				} catch (Exception e) {
					logger.error("Unable map data", e);
				}
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

	private List<String> getContent(JAXBElement<ElementType> jaxbElement) {
		Object value = jaxbElement.getValue();
		
		return ((SimpleLiteral) value).getContent();
	}
}