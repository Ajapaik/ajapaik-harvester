package ee.ajapaik.model.search;

import java.io.Serializable;

public class YearField extends SearchField implements Serializable {

	private static final long serialVersionUID = 1L;
	private Era era;

	public String getYear() {
		return value;
	}

	public void setYear(String year) {
		this.value = year;
	}

	public Era getEra() {
		return era;
	}

	public void setEra(Era era) {
		this.era = era;
	}
	
	@Override
	public String toString() {
		if(value == null || value.length() == 0)
			return "";
		if(era != null)
			return value+"("+era+")";
		return value;
	}

}
