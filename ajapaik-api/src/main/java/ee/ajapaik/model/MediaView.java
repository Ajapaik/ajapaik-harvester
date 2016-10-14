package ee.ajapaik.model;

import java.io.Serializable;

public class MediaView implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String puri;
	private Long objectId;
	private String identifier;
	private String title;
	private String media;
	private Long taskId;
	private Boolean linkProposed;
	private Boolean locationProposed;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getObjectId() {
		return objectId;
	}

	public void setObjectId(Long objectId) {
		this.objectId = objectId;
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

	public String getMedia() {
		return media;
	}

	public void setMedia(String media) {
		this.media = media;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public Boolean getLinkProposed() {
		return linkProposed;
	}

	public void setLinkProposed(Boolean linkProposed) {
		this.linkProposed = linkProposed;
	}

	public Boolean getLocationProposed() {
		return locationProposed;
	}

	public void setLocationProposed(Boolean locationProposed) {
		this.locationProposed = locationProposed;
	}

	public String getPuri() {
		return puri;
	}

	public void setPuri(String puri) {
		this.puri = puri;
	}

	@Override
	public String toString() {
		return "MediaView [id=" + id + ", puri=" + puri + ", objectId="
				+ objectId + ", identifier=" + identifier + ", title=" + title
				+ ", media=" + media + ", taskId=" + taskId + ", linkProposed="
				+ linkProposed + ", locationProposed=" + locationProposed + "]";
	}
}
