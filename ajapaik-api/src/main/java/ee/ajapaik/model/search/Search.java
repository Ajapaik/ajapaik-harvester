package ee.ajapaik.model.search;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;


public class Search implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private SearchField id;
	private SearchField fullSearch;
	private SearchField what; // title
	private SearchField who; // creators
	private SearchField where; // places
	private SearchField from; // institution
	private SearchField description; // description
	private SearchField number; // identifying number
	private List<CollectionType> collectionTypes;
	private List<InstitutionType> institutionTypes;
	private String luceneQuery;
	private boolean digital; // urlToRecord peab olemas olema
	private SortableField sortBy = SortableField.RELEVANCE; 
	
	private int maxResult = 10000; // Maximum total results
	private int pageSize; // Initial result size
	
	public int getMaxResult() {
		return maxResult;
	}

	public void setMaxResult(int maxResult) {
		this.maxResult = maxResult;
	}

	public List<CollectionType> getCollectionTypes() {
		return collectionTypes;
	}

	public void setCollectionTypes(List<CollectionType> collectionTypes) {
		this.collectionTypes = collectionTypes;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public SearchField getWhat() {
		return what;
	}

	public void setWhat(SearchField what) {
		this.what = what;
	}

	public SearchField getWho() {
		return who;
	}

	public void setWho(SearchField who) {
		this.who = who;
	}

	public SearchField getWhere() {
		return where;
	}

	public void setWhere(SearchField where) {
		this.where = where;
	}

	public SearchField getFrom() {
		return from;
	}

	public void setFrom(SearchField from) {
		this.from = from;
	}

	public SearchField getDescription() {
		return description;
	}

	public void setDescription(SearchField description) {
		this.description = description;
	}

	public SearchField getNumber() {
		return number;
	}

	public void setNumber(SearchField number) {
		this.number = number;
	}

	public void setDigital(boolean digital) {
		this.digital = digital;
	}

	public boolean isDigital() {
		return digital;
	}

	public SortableField getSortBy() {
		return sortBy;
	}

	public void setSortBy(SortableField sortBy) {
		this.sortBy = sortBy;
	}
	
	public SearchField getFullSearch() {
		return fullSearch;
	}

	public void setFullSearch(SearchField fullSearch) {
		this.fullSearch = fullSearch;
	}
	
	public int getSearchPhraseHash(){
		return getSearchPhrase().hashCode();
	}
	
	public SearchField getId() {
		return id;
	}

	public void setId(SearchField id) {
		this.id = id;
	}

	public String getSearchPhrase() {
		if(luceneQuery != null) {
			return (luceneQuery + getInstitutions() + getCollectionTypesForQuery()).trim();
		} else {
			String phrase = "";
			if (fullSearch != null && fullSearch.getValue() != null
					&& fullSearch.getValue().length() != 0)
				phrase += " " + fullSearch.getAndOr() + "FULL_SEARCH:(" + getTerm(fullSearch) + ")";
			if (what != null && what.getValue() != null
					&& what.getValue().length() != 0)
				phrase += " " + what.getAndOr() + "WHAT:(" + getTerm(what) + ")";
			if (who != null && who.getValue() != null
					&& who.getValue().length() != 0)
				phrase += " " + who.getAndOr() + "WHO:(" + getTerm(who) + ")";
			if (where != null && where.getValue() != null
					&& where.getValue().length() != 0)
				phrase += " " + where.getAndOr() + "WHERE:(" + getTerm(where) + ")";
			if (from != null && from.getValue() != null
					&& from.getValue().length() != 0  && !"all".equals(from.getValue()))
				phrase += " " + from.getAndOr() + "FROM:(" + getTerm(from) + ")";
			if (description != null && description.getValue() != null
					&& description.getValue().length() != 0)
				phrase += " " + description.getAndOr() + "DESCRIPTION:("
						+ getTerm(description) + ")";
			if (number != null && number.getValue() != null && number.getValue().length() != 0)
				phrase += " " + number.getAndOr() + "NUMBER:(" + getTerm(number) + ")";

			if (id != null && id.getValue() != null && id.getValue().length() != 0)
				phrase += " " + id.getAndOr() + "ID_NUMBER:(" + getTerm(id) + ")";
	
			if (phrase.startsWith(" "))
				phrase = phrase.substring(1);

			return (phrase + getInstitutions() + getCollectionTypesForQuery()).trim();
		}
	}

	private String getInstitutions() {
		return parseListForQuery(institutionTypes, "INSTITUTION_TYPE");
	}

	private String getCollectionTypesForQuery() {
		return parseListForQuery(collectionTypes, "COLLECTION_TYPE");
	}

	private String parseListForQuery(List<?> values, String fieldName) {
		StringBuilder builder = new StringBuilder();

		values.removeAll(Collections.singleton(null));

		if (values.size() > 0) {
			builder.append(" +" + fieldName + ":(");
			for (int i = 0; i < values.size(); i++) {

				builder.append(values.get(i).toString());

				if(i < values.size() - 1) {
					builder.append(" OR ");
				}
			}
			builder.append(") ");
		}
		return builder.toString();
	}

	private String getTerm(SearchField field) {
		if(field.getType().equals(FieldType.AND)){
			String[] words = field.getLuceneValue().split(" ");
			StringBuilder sb = new StringBuilder();
			for(String word: words) {
				if(word.length()>0) {
					sb.append("+"+word.toLowerCase()+" ");
				}
			}
			return sb.toString();
		}
		return field.value;
	}

	@Override
	public String toString() {
		return luceneQuery != null ? luceneQuery : getSearchPhrase();
	}

	public String getLuceneQuery() {
		return luceneQuery;
	}

	public void setLuceneQuery(String luceneQuery) {
		this.luceneQuery = luceneQuery;
	}

	public List<InstitutionType> getInstitutionTypes() {
		return institutionTypes;
	}

	public void setInstitutionTypes(List<InstitutionType> institutionTypes) {
		this.institutionTypes = institutionTypes;
	}
}
