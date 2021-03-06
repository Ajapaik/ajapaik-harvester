package ee.ajapaik.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

public class MediaHandler extends DefaultHandler {
	
	private List<String> medias = new ArrayList<String>();
	
	public List<String> getMedias() {
		return medias;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		//rdf:_1 rdf:resource="http://opendata.rahvusarhiiv.tietotest.ee/media/64f9c950a894"/>
		if(qName.startsWith("rdf:_")) {
			medias.add(attributes.getValue("rdf:resource"));
		} else if (qName.startsWith("rdf:Description")) {
			medias.add(attributes.getValue("rdf:about"));
		}
	}
}
