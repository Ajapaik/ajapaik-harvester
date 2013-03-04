package ee.ajapaik.model.search;

import java.io.Serializable;


public class Search implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int pageSize;
	
	private SearchField fullSearch;
	private SearchField what; // title
	private SearchField who; // creators
	private SearchField where; // places
	private SearchField from; // institution
	private SearchField description; // description
	private SearchField number; // identifying number
	private YearField yearStart; // dates
	private YearField yearEnd; // dates
	private boolean digital; // urlToRecord peab olemas olema
	private SortableField sortBy = SortableField.RELEVANCE; 
	
	private int resultsCount; // for previousSearches page

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

	public void setYearStart(YearField yearStart) {
		this.yearStart = yearStart;
	}

	public YearField getYearStart() {
		return yearStart;
	}

	public void setYearEnd(YearField yearEnd) {
		this.yearEnd = yearEnd;
	}

	public YearField getYearEnd() {
		return yearEnd;
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
	
	public String getSearchPhrase() {
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
		if (number != null && number.getValue() != null
				&& number.getValue().length() != 0)
			phrase += " " + number.getAndOr() + "NUMBER:(" + getTerm(number)
					+ ")";

		boolean hasStart = hasYearValue(yearStart);
		boolean hasEnd = hasYearValue(yearEnd);
		if (hasStart || hasEnd) {
			phrase += " +YEAR:";
			if(hasStart && hasEnd) {
				phrase += "[" + yearStart.getYear() + " TO " + yearEnd.getYear() + "]";
			} else if(hasStart) {
				phrase += yearStart.getYear();
			} else if(hasEnd) {
				phrase += yearEnd.getYear();
			}
		}
		
		if (phrase.startsWith(" "))
			phrase = phrase.substring(1);
		return phrase;
	}

	private String getTerm(SearchField field) {
		if(field.getType().equals(FieldType.AND)){
			String[] words = field.getLuceneValue().split(" ");
			StringBuilder sb = new StringBuilder();
			for(String word: words)
				if(word.length()>0)
					sb.append("+"+word.toLowerCase()+" ");
			return sb.toString();
		}
		return field.value;
	}


	private boolean hasYearValue(YearField year) {
		return year != null && year.getYear() != null && year.getYear().length() != 0;
	}

	public boolean isSimpleSearch() {
		if(fullSearch != null && fullSearch.getValue() != null && fullSearch.getValue().length() > 0)
			return true;
		
		return false;
	}

	public void setResultsCount(int resultsCount) {
		this.resultsCount = resultsCount;
	}

	public int getResultsCount() {
		return resultsCount;
	}
	
	@Override
	public String toString() {
		return getSearchPhrase();
	}
}
