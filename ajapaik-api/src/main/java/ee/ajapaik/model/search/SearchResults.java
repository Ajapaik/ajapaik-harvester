package ee.ajapaik.model.search;

import java.io.Serializable;
import java.util.List;


public class SearchResults implements Serializable {
	private static final long serialVersionUID = 1L;

	private String[] ids;
	private List<Record> firstRecords;
	private RecordView[] firstRecordViews;
	private String[] museumIds;
	private String[] archiveIds;
	private String[] libraryIds;
	private String[] mediaIds;

	private int totalMuseumIds;
	private int totalArchiveIds;
	private int totalLibraryIds;
	private int totalMediaIds;
	private double searchTime;
	public double getSearchTime() {
		return searchTime;
	}
	public void setSearchTime(double searchTime) {
		this.searchTime = searchTime;
	}
	public int getTotalIds() {
		return totalMuseumIds + totalArchiveIds + totalLibraryIds
				+ totalMediaIds;
	}
	public RecordView[] getFirstRecordViews() {
		return firstRecordViews;
	}
	public int getTotalMuseumIds() {
		return totalMuseumIds;
	}

	public void setTotalMuseumIds(int totalMuseumIds) {
		this.totalMuseumIds = totalMuseumIds;
	}

	public int getTotalArchiveIds() {
		return totalArchiveIds;
	}

	public void setTotalArchiveIds(int totalArchiveIds) {
		this.totalArchiveIds = totalArchiveIds;
	}

	public int getTotalLibraryIds() {
		return totalLibraryIds;
	}

	public void setTotalLibraryIds(int totalLibraryIds) {
		this.totalLibraryIds = totalLibraryIds;
	}

	public String[] getMediaIds() {
		return mediaIds;
	}

	public void setMediaIds(String[] mediaIds) {
		this.mediaIds = mediaIds;
	}

	public int getTotalMediaIds() {
		return totalMediaIds;
	}

	public void setTotalMediaIds(int totalMediaIds) {
		this.totalMediaIds = totalMediaIds;
	}

	public String[] getIds() {
		return ids;
	}

	public void setIds(String[] ids) {
		this.ids = ids;
	}

	public List<Record> getFirstRecords() {
		return firstRecords;
	}

	public void setFirstRecords(List<Record> firstRecords) {
		this.firstRecords = firstRecords;
	}

	public String[] getMuseumIds() {
		return museumIds;
	}

	public void setMuseumIds(String[] museumIds) {
		this.museumIds = museumIds;
	}

	public String[] getArchiveIds() {
		return archiveIds;
	}

	public void setArchiveIds(String[] archiveIds) {
		this.archiveIds = archiveIds;
	}

	public String[] getLibraryIds() {
		return libraryIds;
	}

	public void setLibraryIds(String[] libraryIds) {
		this.libraryIds = libraryIds;
	}

	public void setFirstRecordViews(RecordView[] views) {
		this.firstRecordViews = views;
		
	}
}
