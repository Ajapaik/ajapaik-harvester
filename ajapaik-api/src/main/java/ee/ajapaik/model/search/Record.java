package ee.ajapaik.model.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Record implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	
	private List<String> creators; // dc:creator + dc:publisher
	private String title; // dc:title
	private String description; // dc:description
	private String identifyingNumber; // dc: identifier???
	private List<String> types; // dc:type
	private List<String> materials; // dc:medium
	private List<String> collections; // setSpec
	private List<String> institutions; // ese:provider, dc:source
	private List<String> places; // dc:spatial
	private List<String> dates; // dc:temporal, dc:date, dc:created, dc:issued,
	// ese:date
	private String urlToRecord; // ese:isShownAt
	private String providerHomepageUrl;
	private String providerName;
	private String cachedThumbnailUrl;
	private String imageUrl;
	private InstitutionType institutionType;
	private String id; // kirje id
	private boolean deleted;
	private List<String> setSpec;
	private Date dateCreated;
	private String mediaId;
	private Integer mediaOrder;
	private String latitude;
	private String longitude;
	private CollectionType collectionType;

	public String getCreatorsAsString() {
		if (creators == null)
			return "";
		String result = "";
		for (String creator : creators) {
			result += ", " + creator;
		}
		if (result.startsWith(", "))
			result = result.substring(2);
		return result;
	}
	public String getInstitutionsAsString() {
		if (institutions == null)
			return "";
		String result = "";
		for (String institution : institutions) {
			result += ", " + institution;
		}
		if (result.startsWith(", "))
			result = result.substring(2);
		return result;
	}
	public String getCollectionsAsString() {
		if (collections == null)
			return "";
		String result = "";
		for (String collection : collections) {
			result += ", " + collection;
		}
		if (result.startsWith(", "))
			result = result.substring(2);
		return result;
	}

	public String getMaterialsAsString() {
		if (materials == null)
			return "";
		String result = "";
		for (String material : materials) {
			result += ", " + material;
		}
		if (result.startsWith(", "))
			result = result.substring(2);
		return result;
	}

	public String getTypesAsString() {
		if (types == null)
			return "";
		String result = "";
		for (String type : types) {
			result += ", " + type;
		}
		if (result.startsWith(", "))
			result = result.substring(2);
		return result;
	}

	public String getTitle() {
		return notNull(title);
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return notNull(description);
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIdentifyingNumber() {
		return notNull(identifyingNumber);
	}

	public void setIdentifyingNumber(String identifyingNumber) {
		this.identifyingNumber = identifyingNumber;
	}

	public List<String> getCreators() {
		return creators;
	}

	public void setCreators(List<String> creators) {
		this.creators = creators;
	}

	public List<String> getTypes() {
		return types;
	}

	public void setTypes(List<String> types) {
		this.types = types;
	}

	public List<String> getMaterials() {
		return materials;
	}

	public void setMaterials(List<String> materials) {
		this.materials = materials;
	}

	public List<String> getPlaces() {
		return places;
	}

	public void setPlaces(List<String> places) {
		this.places = places;
	}

	public List<String> getDates() {
		return dates;
	}

	public void setDates(List<String> dates) {
		this.dates = dates;
	}



	public List<String> getCollections() {
		return collections;
	}

	public void setCollections(List<String> collections) {
		this.collections = collections;
	}

	public List<String> getInstitutions() {
		return institutions;
	}

	public void setInstitutions(List<String> institutions) {
		this.institutions = institutions;
	}

	public String getUrlToRecord() {
		return notNull(urlToRecord);
	}

	public void setUrlToRecord(String urlToRecord) {
		this.urlToRecord = urlToRecord;
	}

	public String getProviderHomepageUrl() {
		return notNull(providerHomepageUrl);
	}

	public void setProviderHomepageUrl(String providerHomepageUrl) {
		this.providerHomepageUrl = providerHomepageUrl;
	}

	public String getId() {
		return notNull(id);
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public InstitutionType getInstitutionType() {
		return institutionType;
	}

	public void setInstitutionType(InstitutionType institutionType) {
		this.institutionType = institutionType;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	public String getProviderName() {
		return notNull(providerName);
	}

	public void appendDescription(List<String> values) {
		for (String value : values) {
			description += ", "+value;
		}
		if(description.startsWith(", "))
			description = description.substring(1);

	}
	private String notNull(String s){
		return s==null?"":s;
	}

	public RecordView getRecordView(){
		return new RecordView(
				getCreatorsAsString(),
				identifyingNumber,
				title,
				description,
				getTypesAsString(),
				getMaterialsAsString(),
				getCollectionsAsString(),
				getInstitutionsAsString(),
				urlToRecord,
				providerHomepageUrl,
				cachedThumbnailUrl,
				imageUrl,
				institutionType,
				id,
				getProviderName(),
				mediaId,
				mediaOrder,
				latitude,
				longitude);
	}

	public void setSetSpec(List<String> setSpec) {
		this.setSpec = setSpec;
	}

	public List<String> getSetSpec() {
		return setSpec;
	}
	public void setDateCreated(Date date) {
		this.dateCreated = date;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void addInstitution(String institution) {
		if(institutions == null)
			institutions = new ArrayList<String>();
		institutions.add(institution);
	}

	public String getCachedThumbnailUrl() {
		return cachedThumbnailUrl;
	}
	public void setCachedThumbnailUrl(String cachedThumbnailUrl) {
		this.cachedThumbnailUrl = cachedThumbnailUrl;
	}

	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getMediaId() {
		return mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}

	public Integer getMediaOrder() {
		return mediaOrder;
	}

	public void setMediaOrder(Integer mediaOrder) {
		this.mediaOrder = mediaOrder;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public CollectionType getCollectionType() {
		return collectionType;
	}

	public void setCollectionType(CollectionType collectionType) {
		this.collectionType = collectionType;
	}

	@Override
	public Record clone() {
		try {
			return (Record) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}