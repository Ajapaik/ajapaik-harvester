package ee.ajapaik.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class InfoSystem implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private String address;
	private List<String> setsToUse;
	private String mapper;
	private Date lastHarvestTime;
	private Boolean running;
	private String schedule;
	private String email;
	private String homepageUrl;
	private String ignoreSet;
	private Boolean disableSets;
	private String metadataPrefix;

	public Boolean isSetsDisabled() {
		return disableSets;
	}

	public void setDisableSets(Boolean disableSets) {
		this.disableSets = disableSets;
	}

	public InfoSystem() {
	}

	public InfoSystem(String name) {
		this.name = name;
	}

	public String getHomepageUrl() {
		return homepageUrl;
	}

	public void setHomepageUrl(String homepageUrl) {
		this.homepageUrl = homepageUrl;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getLastHarvestTime() {
		return lastHarvestTime;
	}

	public void setLastHarvestTime(Date lastHarvestTime) {
		this.lastHarvestTime = lastHarvestTime;
	}

	public Boolean getRunning() {
		return running;
	}

	public void setRunning(Boolean running) {
		this.running = running;
	}

	public List<String> getSetsToUse() {
		return setsToUse;
	}

	public void setSetsToUse(List<String> setsToUse) {
		this.setsToUse = setsToUse;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public String getMapper() {
		return mapper;
	}

	public void setMapper(String mapper) {
		this.mapper = mapper;
	}
	
	public String getIgnoreSet() {
		return ignoreSet;
	}

	public void setIgnoreSet(String ignoreSet) {
		this.ignoreSet = ignoreSet;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && name.equals(((InfoSystem) obj).getName());
	}

	public String getMetadataPrefix() {
		return metadataPrefix;
	}

	public void setMetadataPrefix(String metadataPrefix) {
		this.metadataPrefix = metadataPrefix;
	}
}
