package ee.ajapaik.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import ee.ajapaik.model.MediaView;

public class MediaViewMapper implements RowMapper<MediaView> {

	@Override
	public MediaView mapRow(ResultSet rs, int rowNum) throws SQLException {
		MediaView mediaView = new MediaView();
		mediaView.setId(rs.getLong("id"));
		mediaView.setPuri(rs.getString("puri"));
		mediaView.setObjectId(rs.getLong("object_id"));
		mediaView.setIdentifier(rs.getString("identifier"));
		mediaView.setTitle(rs.getString("title"));
		mediaView.setMedia(rs.getString("media"));
		mediaView.setTaskId(rs.getLong("task_id"));
		mediaView.setLinkProposed(rs.getBoolean("link_proposed"));
		mediaView.setLocationProposed(rs.getBoolean("location_proposed"));

		return mediaView;
	}
}
