package ee.ajapaik.model;

import java.io.Serializable;

public class MediaView implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private Long objectId;
	private String identifier;
	private String title;
	private String media;
	private Long taskId;

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
}
