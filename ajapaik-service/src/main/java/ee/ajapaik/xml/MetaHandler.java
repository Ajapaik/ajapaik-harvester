package ee.ajapaik.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ee.ajapaik.xml.model.Meta;

public class MetaHandler extends DefaultHandler {
	
	private Meta meta = new Meta();
	
	private String value;
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if(qName.equals("rdf:Description")) {
			meta.setAbout(attributes.getValue("rdf:about"));
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName.equals("dc:publisher")) {
			meta.setPublisher(value); 
		} else if(qName.equals("dc:identifier")) {
			meta.setIdentifier(value);
		} else if(qName.equals("dc:title")) {
			meta.setTitle(value);
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		this.value = new String(ch, start, length);
	}

	public Meta getMeta() {
		return meta;
	}	
}
