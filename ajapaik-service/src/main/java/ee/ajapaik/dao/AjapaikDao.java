package ee.ajapaik.dao;

import org.springframework.jdbc.core.JdbcTemplate;

public class AjapaikDao {

	private JdbcTemplate jdbcTemplate;

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	

	
}
