package ee.ajapaik.model;


public class Location {

	private String notes;
	private String lat;
	private String lon;

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	@Override
	public String toString() {
		return "Location [notes=" + notes + ", lat=" + lat + ", lon=" + lon
				+ "]";
	}

}
