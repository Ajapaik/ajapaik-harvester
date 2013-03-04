package ee.ajapaik.model.search;

import java.io.Serializable;


public class RecordView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static String seprator = "\u0666\u0666\u0666";
	private String creators;
	private String identifyingNumber;
	private String title;
	private String description;
	private String types;
	private String materials;
	private String collections;
	private String institution;
	private String urlToRecord;
	private String providerHomepageUrl;
	private String providerName;
	private String imageUrl;
	private InstitutionType institutionType;
	private String id;
	
	private boolean inBasket;
	private String cachedThumbnailUrl;

	public RecordView(String creators, String identifyingNumber, String title,
			String description, String types, String materials,
			String collections, String institution, String urlToRecord,
			String providerHomepageUrl, String cachedThumbnailUrl,
			String imageUrl, InstitutionType institutionType, String id, String providerName) {
		super();
		this.creators = notNull(creators);
		this.identifyingNumber = notNull(identifyingNumber);
		this.title = notNull(title);
		this.description = notNull(description);
		this.types = notNull(types);
		this.materials = notNull(materials);
		this.collections = notNull(collections);
		this.institution = notNull(institution);
		this.urlToRecord = notNull(urlToRecord);
		this.providerHomepageUrl = notNull(providerHomepageUrl);
		this.imageUrl = imageUrl;		
		this.cachedThumbnailUrl = cachedThumbnailUrl;
		this.institutionType = institutionType;
		this.id = notNull(id);
		this.providerName = notNull(providerName);
	}

	public String getProviderName() {
		return providerName;
	}
	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}
	public String getCreators() {
		return creators;
	}

	public void setCreators(String creators) {
		this.creators = creators;
	}

	public String getIdentifyingNumber() {
		return identifyingNumber;
	}

	public void setIdentifyingNumber(String identifyingNumber) {
		this.identifyingNumber = identifyingNumber;
	}
	
	public String getTitle() {
		return title.substring(0, 1).toUpperCase() + title.substring(1);
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTypes() {
		return types;
	}

	public void setTypes(String types) {
		this.types = types;
	}

	public String getMaterials() {
		return materials;
	}

	public void setMaterials(String materials) {
		this.materials = materials;
	}

	public String getCollections() {
		return collections;
	}

	public void setCollections(String collections) {
		this.collections = collections;
	}

	public String getInstitution() {
		return institution;
	}

	public void setInstitution(String institution) {
		this.institution = institution;
	}

	public String getUrlToRecord() {
		return urlToRecord;
	}

	public void setUrlToRecord(String urlToRecord) {
		this.urlToRecord = urlToRecord;
	}

	public String getProviderHomepageUrl() {
		return providerHomepageUrl;
	}

	public void setProviderHomepageUrl(String providerHomepageUrl) {
		this.providerHomepageUrl = providerHomepageUrl;
	}

	public InstitutionType getInstitutionType() {
		return institutionType;
	}

	public void setInstitutionType(InstitutionType institutionType) {
		this.institutionType = institutionType;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public RecordView(String serialized) {
		String[] fields = serialized.split(seprator);
		
		this.creators = fields[0];
		this.identifyingNumber = fields[1];
		this.title = fields[2];
		this.description = fields[3];
		this.types = fields[4];
		this.materials = fields[5];
		this.collections = fields[6];
		this.institution = fields[7];
		this.urlToRecord = fields[8];
		this.providerHomepageUrl = fields[9];
		// XXX no thumbnailUrl anymore this.thumbnailUrl = fields[10];
		this.cachedThumbnailUrl = fields[11];
		
		String typeString = fields[12];
		if(typeString != null && !typeString.equals("null"))
			this.institutionType = InstitutionType.valueOf(typeString);
		
		this.id = fields[13];
		
		if(fields.length > 14)
			this.providerName = fields[14];
		
		if(fields.length > 15)
			this.imageUrl = fields[15];
	}

	public String getFullSearchData(){
		return serialize(" ");
	}
	
	public String serialize(){
		return serialize(seprator);
	}
	private String serialize(String seprator) {
		StringBuilder sb = new StringBuilder();
		sb.append(creators).append(seprator);
		sb.append(identifyingNumber).append(seprator);
		sb.append(title).append(seprator);
		sb.append(description).append(seprator);
		sb.append(types).append(seprator);
		sb.append(materials).append(seprator);
		sb.append(collections).append(seprator);
		sb.append(institution).append(seprator);
		sb.append(urlToRecord).append(seprator);
		sb.append(providerHomepageUrl).append(seprator);
		sb.append("").append(seprator); // XXX: no thumbnailUl anymore
		sb.append(cachedThumbnailUrl).append(seprator);
		sb.append(institutionType).append(seprator);
		sb.append(id).append(seprator);
		sb.append(providerName).append(seprator);
		sb.append(imageUrl);

		return sb.toString();
	}
	
	
	public String getImageUrl() {
		return imageUrl;
	}

	public boolean isInBasket() {
		return inBasket;
	}

	public void setInBasket(boolean inBasket) {
		this.inBasket = inBasket;
	}
	
	private String notNull(String s){
		return s==null?"":s;
	}

	public void setCachedThumbnailUrl(String cachedThumbnailUrl) {
		this.cachedThumbnailUrl = cachedThumbnailUrl;
	}

	public String getCachedThumbnailUrl() {
		return cachedThumbnailUrl;
	}
}