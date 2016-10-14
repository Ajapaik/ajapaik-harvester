package ee.ajapaik.model;

import java.io.Serializable;

public class Schedule implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private boolean monday;
	private boolean tuesday;
	private boolean wednesday;
	private boolean thursday;
	private boolean friday;
	private boolean saturday;
	private boolean sunday;
	private String updateTime; // hh:mm
	private boolean active;
	
	public Schedule() {
		// TODO Auto-generated constructor stub
	}
	public Schedule(boolean monday, boolean tuesday, boolean wednesday,
			boolean thursday, boolean friday, boolean saturday, boolean sunday) {
		this.monday = monday;
		this.tuesday = tuesday;
		this.wednesday = wednesday;
		this.thursday = thursday;
		this.friday = friday;
		this.saturday = saturday;
		this.sunday = sunday;
	}
	public boolean isMonday() {
		return monday;
	}
	public void setMonday(boolean monday) {
		this.monday = monday;
	}
	public boolean isTuesday() {
		return tuesday;
	}
	public void setTuesday(boolean tuesday) {
		this.tuesday = tuesday;
	}
	public boolean isWednesday() {
		return wednesday;
	}
	public void setWednesday(boolean wednesday) {
		this.wednesday = wednesday;
	}
	public boolean isThursday() {
		return thursday;
	}
	public void setThursday(boolean thursday) {
		this.thursday = thursday;
	}
	public boolean isFriday() {
		return friday;
	}
	public void setFriday(boolean friday) {
		this.friday = friday;
	}
	public boolean isSaturday() {
		return saturday;
	}
	public void setSaturday(boolean saturday) {
		this.saturday = saturday;
	}
	public boolean isSunday() {
		return sunday;
	}
	public void setSunday(boolean sunday) {
		this.sunday = sunday;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
}
