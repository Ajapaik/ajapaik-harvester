package ee.ajapaik.model;

import java.io.Serializable;

public class City implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;
	private String name;
	private Double lat;
	private Double lon;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public Double getLon() {
		return lon;
	}

	public void setLon(Double lon) {
		this.lon = lon;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "City [id=" + id + ", name=" + name + ", lat=" + lat + ", lon="
				+ lon + "]";
	}
}
