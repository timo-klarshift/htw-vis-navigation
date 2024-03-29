package vis.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

/**
 * search result
 * 
 * @author timo
 * 
 */
public class SearchResult {
	Document document;
	double score;

	public SearchResult(Document doc, double score) {
		this.score = score;
		this.document = doc;
	}
	
	public Document getDocument(){
		return document;
	}

	public String getId() {
		return document.get("id");
	}

	public String getFotoliaId() {
		return document.get("fotoliaId");
	}

	public String getWords() {
		return document.get("words");
	}

	public double getScore() {
		return score;
	}

	public String toString() {
		double s = score;
		s = (int) (s * 100);
		s /= 100.0;
		return "[" + getId() + " / " + getFotoliaId() + "] :: score=" + s
				+ " : " + getWords();
	}

	public String getUrl() {
		String url = "http://141.45.146.52/jpg/" + document.get("thumbPath");
		return url;
		// url = url.replaceAll("^http:\\/\\/t[\\d+]\\.ftcdn\\.net",
		// "http://141.45.146.52");
		// return url;
	}


}
