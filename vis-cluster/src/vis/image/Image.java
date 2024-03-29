package vis.image;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

public class Image {

	private Integer id = null;
	private Integer fotoliaId = null;
	private String words = null;
	private String thumbPath;
	private Integer x = null;
	private Integer y = null;
	private float quadScore;
	
	public Image(int id, int fotoliaId, String thumbPath, String words){
		this.id  = id;
		this.fotoliaId = fotoliaId;
		this.thumbPath = thumbPath;
		this.words = words;
	}	
	
	public void setQuadScore(float score){
		this.quadScore = score;
	}
	
	public void setPosition(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public Document toDocument(){
		Document doc = new Document();
		
		Field idField = new TextField("id", id.toString(), Field.Store.YES);
		doc.add(idField);
		
		Field fidField = new TextField("fotoliaId", fotoliaId.toString(), Field.Store.YES);
		doc.add(fidField);
		
		Field tagsField = new TextField("words", words, Field.Store.YES);
		doc.add(tagsField);
		
		Field urlField = new StringField("thumbPath", thumbPath, Field.Store.YES);
		doc.add(urlField);
		
		/*Field xField = new StringField("x", x.toString(), Field.Store.YES);
		doc.add(xField);
		
		Field yField = new StringField("y", y.toString(), Field.Store.YES);
		doc.add(yField);*/
		
		return doc;	
	}
	
	public String toString(){
		return "[" +id+" / " + fotoliaId + "] " + thumbPath + " :: " + words.substring(0, Math.min(60, words.length()-1)); 
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public String getUrl() {
		String url = "http://141.45.146.52/jpg/" + thumbPath;
		return url;		
	}

	public float getQuadScore() {
		return quadScore;
	}
	
	

}
