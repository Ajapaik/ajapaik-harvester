package ee.ajapaik.model;

import java.io.Serializable;
import java.util.Date;

public class TaskView implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private Date finished;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getFinished() {
		return finished;
	}

	public void setFinished(Date finished) {
		this.finished = finished;
	}
}
