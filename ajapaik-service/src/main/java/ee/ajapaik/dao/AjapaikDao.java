package ee.ajapaik.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import ee.ajapaik.model.MediaView;
import ee.ajapaik.model.TaskView;
import ee.ajapaik.xml.model.Meta;
import ee.ajapaik.xml.model.Task;

public class AjapaikDao {

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void taskStarted(Long taskId) {
		jdbcTemplate.update("insert into task (task_id, started) values (?, now())", taskId);
	}

	public void taskFinished(Long taskId) {
		jdbcTemplate.update("update task set finished = now() where task_id = ?", taskId);
	}

	public void saveMedia(String identifier, Task task, Meta meta, String thumbnailUrl) {
		jdbcTemplate.update("INSERT INTO media(object_id, identifier, title, media, task_id) VALUES (?, ?, ?, ?, ?)",
				task.getObjectId(),
				identifier,
				meta.getTitle(),
				thumbnailUrl,
				task.getTaskId());
	}

	public List<MediaView> getMediaViews(Long taskId) {
		return jdbcTemplate.query("select * from media where task_id = ?", new RowMapper<MediaView>(){

			@Override
			public MediaView mapRow(ResultSet rs, int rowNum) throws SQLException {
				MediaView mediaView = new MediaView();
				mediaView.setId(rs.getLong("id"));
				mediaView.setObjectId(rs.getLong("object_id"));
				mediaView.setIdentifier(rs.getString("identifier"));
				mediaView.setTitle(rs.getString("title"));
				mediaView.setMedia(rs.getString("media"));
				mediaView.setTaskId(rs.getLong("task_id"));
				
				return mediaView;
			}
		}, taskId);
	}

	public boolean hasTask(Long taskId) {
		return jdbcTemplate.queryForInt("select count(*) from task where task_id = ?", taskId) == 1;
	}

	public List<TaskView> getTasks() {
		final Map<Long, TaskView> taskMap = new HashMap<Long, TaskView>();
		jdbcTemplate.query(
				" select t.task_id, t.finished, m.identifier from task t " +
				" left join media m on m.task_id = t.task_id ", 
				new RowCallbackHandler() {

					@Override
					public void processRow(ResultSet rs) throws SQLException {
						long id = rs.getLong("task_id");

						TaskView taskView = null;
						if(!taskMap.containsKey(id)) {
							taskView = new TaskView();
							taskView.setId(id);
							taskView.setFinished(rs.getTimestamp("finished"));
							
							taskMap.put(id, taskView);
						} else  {
							taskView = taskMap.get(id);
						}

						String identifier = rs.getString("identifier");
						if(identifier != null) {
							taskView.getTaskIds().add(identifier);
						}
					}
		});
		
		return new ArrayList<TaskView>(taskMap.values());
	}
}
