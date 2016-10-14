package ee.ajapaik.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Proposal implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long objectId;
	private String objectPuri;
	private Long taskId;
	private String notes;

	private BigDecimal lat;
	private BigDecimal lon;
	private BigDecimal azi;
	private String url;

	public Long getObjectId() {
		return objectId;
	}

	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}

	public String getObjectPuri() {
		return objectPuri;
	}

	public void setObjectPuri(String objectPuri) {
		this.objectPuri = objectPuri;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public BigDecimal getLat() {
		return lat;
	}

	public void setLat(BigDecimal lat) {
		this.lat = lat;
	}

	public BigDecimal getLon() {
		return lon;
	}

	public void setLon(BigDecimal lon) {
		this.lon = lon;
	}

	public BigDecimal getAzi() {
		return azi;
	}

	public void setAzi(BigDecimal azi) {
		this.azi = azi;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
