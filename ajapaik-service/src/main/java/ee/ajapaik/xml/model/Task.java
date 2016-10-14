package ee.ajapaik.xml.model;

import java.io.Serializable;
import java.util.List;

public class Task implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long objectId;
	private List<String> objectPuris;
	private Long taskId;
	private String taskObjectType;

	public Long getObjectId() {
		return objectId;
	}

	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}

	public List<String> getObjectPuris() {
		return objectPuris;
	}

	public void setObjectPuris(List<String> objectPuris) {
		this.objectPuris = objectPuris;
	}

	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public String getTaskObjectType() {
		return taskObjectType;
	}

	public void setTaskObjectType(String taskObjectType) {
		this.taskObjectType = taskObjectType;
	}

	@Override
	public String toString() {
		return "Task [objectId=" + objectId + ", objectPuris=" + objectPuris
				+ ", taskId=" + taskId + ", taskObjectType=" + taskObjectType
				+ "]";
	}
	
}
