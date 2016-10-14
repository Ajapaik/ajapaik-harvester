package ee.ajapaik.harvester;

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

import ee.ajapaik.model.search.Record;

public class KivikeHarvestTask extends HarvestTask {

	private int count = 0;
	private Map<String, List<String>> map = new HashMap<String, List<String>>();
	
	@SuppressWarnings("unchecked")
	@Override
	protected Record mapRecord(RecordType recordType) {
		Record rec = new Record();
		HeaderType header = recordType.getHeader();
		rec.setId(header.getIdentifier());
		
		logger.debug("identifier: " + rec.getId() + ", count: " + count);
		
		List<String> institutions = new ArrayList<String>();
		institutions.add(infoSystem.getName());
		
		rec.setInstitutions(institutions);
		
		MetadataType metadata = recordType.getMetadata();
		rec.setDeleted(metadata == null);
		if (!rec.isDeleted()) {
			OaiDcType any = ((JAXBElement<OaiDcType>) metadata.getAny()).getValue();
			
			List<JAXBElement<ElementType>> data = any.getTitleOrCreatorOrSubject();
			for (JAXBElement<ElementType> jaxbElement : data) {
				String name = jaxbElement.getName().getLocalPart();
				List<String> content = getContent(jaxbElement);
				
//				logger.debug("name: " + name + ", content: " + content);
				
				if(!map.containsKey(name)) {
					map.put(name, content);
					
					logger.debug("structure updated: " + map);
				}
			}
		}
		
		count++;
		
		return null;
	}

	private List<String> getContent(JAXBElement<ElementType> jaxbElement) {
		Object value = jaxbElement.getValue();
		return ((SimpleLiteral)value).getContent();
	}

}
