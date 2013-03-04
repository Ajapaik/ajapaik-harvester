package ee.ajapaik.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.purl.dc.elements._1.SimpleLiteral;

public class JaxbUtil {
	public String getValue(eu.europeana.schemas.ese.Record eseRec, String... v){
		 
		List<String> values =getValues(eseRec, v);
		if(values.size()>0)
			return values.get(0);
		else
			return "";
		
	}
	
	public List<String> getValues(eu.europeana.schemas.ese.Record eseRec, String... v){
		List<String> rv = new ArrayList<String>(); 
		for(JAXBElement<?> jaxbel:eseRec.getContent()){		
			for(String val:v){
				if(val.equals(jaxbel.getName().getLocalPart())){
					if(jaxbel.getValue() instanceof SimpleLiteral){
						SimpleLiteral sl = (SimpleLiteral)jaxbel.getValue();
						rv.addAll(sl.getContent());
					}else if(jaxbel.getValue() instanceof String){
						rv.add((String)jaxbel.getValue());
					}else{
						throw new RuntimeException("Unknown element value class :"+jaxbel.getValue().getClass().getCanonicalName() );
					}
				}
			}
		}
		return rv;
	}
}
