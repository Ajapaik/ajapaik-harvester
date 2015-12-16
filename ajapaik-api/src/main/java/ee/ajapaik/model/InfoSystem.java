package ee.ajapaik.model;

import java.io.Serializable;
import java.util.Date;

public class InfoSystem implements Serializable {
	private static final long serialVersionUID = 1L;

	private String name;
	private String address;
	private String useSet;
	private String mapper;
	private Date lastHarvestTime;
	private Boolean running;
	private String schedule;
	private String email;
	private String homepageUrl;
	private String ignoreSet;
	private Boolean useSets = Boolean.TRUE;
	
	public Boolean getUseSets() {
		return useSets;
	}

	public void setUseSets(Boolean useSets) {
		this.useSets = useSets;
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

	public String getUseSet() {
		return useSet;
	}

	public void setUseSet(String useSet) {
		this.useSet = useSet;
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
}
