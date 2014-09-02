package ee.ajapaik.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import ee.ajapaik.dao.mapper.MediaViewMapper;
import ee.ajapaik.model.Location;
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
		jdbcTemplate.update("insert into task (id, started) values (?, now())", taskId);
	}

	public void taskFinished(Long taskId) {
		jdbcTemplate.update("update task set finished = now() where id = ?", taskId);
	}

	public void saveMedia(String identifier, Task task, Meta meta, String thumbnailUrl) {
		jdbcTemplate.update("INSERT INTO media(object_id, identifier, title, media, task_id, puri) VALUES (?, ?, ?, ?, ?, ?)",
				task.getObjectId(),
				identifier,
				meta.getTitle(),
				thumbnailUrl,
				task.getTaskId(),
				meta.getAbout());
	}

	public List<MediaView> getMediaViews(Long taskId) {
		return jdbcTemplate.query("select * from media where task_id = ?", new MediaViewMapper(), taskId);
	}
	
	public List<MediaView> getMediaViewsForProposal() {
		return jdbcTemplate.query("select * from media where link_proposed is null or location_proposed is null", new MediaViewMapper()); 
	}

	public boolean hasTask(Long taskId) {
		return jdbcTemplate.queryForInt("select count(*) from task where id = ?", taskId) == 1;
	}

	public List<TaskView> getTasks() {
		final Map<Long, TaskView> taskMap = new HashMap<Long, TaskView>();
		jdbcTemplate.query(
				" select t.id, t.finished, m.identifier from task t " +
				" left join media m on m.task_id = t.id ", 
				new RowCallbackHandler() {

					@Override
					public void processRow(ResultSet rs) throws SQLException {
						long id = rs.getLong("id");

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
							if(identifier.contains(" ")) {
								taskView.getTaskIds().add("\"" + identifier + "\"");
							} else {
								taskView.getTaskIds().add(identifier);
							}
						}
					}
		});
		
		return new ArrayList<TaskView>(taskMap.values());
	}

	public void removeTask(Long taskId) {
		jdbcTemplate.update("delete from task where id = ?", taskId);
	}

	public String getPermalink(String identifier) {
		try {
			return jdbcTemplate.queryForObject("select id from project_photo where source_key = ?", String.class, identifier);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public void updateLinkProposed(Long id) {
		jdbcTemplate.update("update media set link_proposed = TRUE where id = ?", id);
	}
	
	public void updateLocationProposed(Long id) {
		jdbcTemplate.update("update media set location_proposed = TRUE where id = ?", id);
	}

	public Location getLocation(String identifier) {
		try{
			return jdbcTemplate.queryForObject("select lat, lon, confidence from project_photo where source_key = ? and confidence > 0.6", new RowMapper<Location>() {
	
				@Override
				public Location mapRow(ResultSet rs, int rowNum) throws SQLException {
					Location location = new Location();
					location.setLat(rs.getString("lat"));
					location.setLon(rs.getString("lon"));
					location.setNotes(rs.getString("confidence"));
					
					return location;
				}
				
			}, identifier);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
}
