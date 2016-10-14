package ee.ajapaik.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Photo {

	private Integer id;
	private String lat;
	private String lon;
	private String azimuth;
	private Double confidence;

	@JsonProperty(value = "azimuth_confidence")
	private Double azimuthConfidence;

	@JsonProperty(value = "source_key")
	private String sourceKey;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	public String getAzimuth() {
		return azimuth;
	}

	public void setAzimuth(String azimuth) {
		this.azimuth = azimuth;
	}

	public Double getConfidence() {
		return confidence;
	}

	public void setConfidence(Double confidence) {
		this.confidence = confidence;
	}

	public Double getAzimuthConfidence() {
		return azimuthConfidence;
	}

	public void setAzimuthConfidence(Double azimuthConfidence) {
		this.azimuthConfidence = azimuthConfidence;
	}

	public String getSourceKey() {
		return sourceKey;
	}

	public void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
	}

	@Override
	public String toString() {
		return "Photo [id=" + id + ", lat=" + lat + ", lon=" + lon
				+ ", azimuth=" + azimuth + ", confidence=" + confidence
				+ ", azimuthConfidence=" + azimuthConfidence + ", sourceKey="
				+ sourceKey + "]";
	}
}
