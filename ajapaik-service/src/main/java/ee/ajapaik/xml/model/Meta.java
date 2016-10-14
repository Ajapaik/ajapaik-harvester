package ee.ajapaik.xml.model;

public class Meta {
	
	private String about;
	private String publisher;
	private String identifier;
	private String title;

	public String getAbout() {
		return about;
	}

	public void setAbout(String about) {
		this.about = about;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return "Meta [about=" + about + ", publisher=" + publisher
				+ ", identifier=" + identifier + ", title=" + title + "]";
	}
}
