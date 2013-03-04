/**
 * 
 */
package ee.ajapaik.index;

import java.util.List;

import org.apache.lucene.document.Document;

/**
 * @author <a href="mailto:kaido@urania.ee?subject=Result">Kaido Kalda</a>
 */
public class Result {
	private List<Document> result;
	private int totalHits;

	public Result(List<Document> result, int totalHits) {
		this.result = result;
		this.totalHits = totalHits;
	}

	public List<Document> getResult() {
		return result;
	}

	public int getTotalHits() {
		return totalHits;
	}
}
