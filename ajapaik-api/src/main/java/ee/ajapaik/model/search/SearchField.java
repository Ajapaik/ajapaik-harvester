package ee.ajapaik.model.search;

import java.io.Serializable;

public class SearchField implements Serializable{

	private static final long serialVersionUID = 1L;
	
	protected String value;
	private FieldType type = FieldType.AND;

	public SearchField() {}
	
	public SearchField(String value) {
		this.value = value;
	}
	
	public SearchField(String value, FieldType type) {
		this.value = value;
		this.type = type;
	}

	public String getValue() {
		return value;
	}
	
	public String getLuceneValue(){
		if(value != null ) {
			String str = new String(value);
			if(str.contains("%"))
				str = str.replaceAll("%", "*");
			
			if(str.contains(":"))
				str = str.replaceAll(":", " ");
			
			if(str.contains(new String(new char[]{'+'})))
				str = str.replaceAll("\\+", " ");
			
			if(str.startsWith("*") || str.startsWith("?")) 
				str = str.substring(1);
			
			//value.setValue(str);
			return str;
		}else{
			return "";
		}
	}

	public void setValue(String value) {
		this.value = value;
	}

	public FieldType getType() {
		return type;
	}

	public void setType(FieldType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		if (value == null || value == "")
			return "";
		return value;
	}

	public String getAndOr() {
		if(type.equals(FieldType.NOT))
			return "-";
		else
			return "+";
	}

}
