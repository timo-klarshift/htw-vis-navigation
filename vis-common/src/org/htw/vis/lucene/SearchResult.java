package org.htw.vis.lucene;

import org.apache.lucene.document.Document;
import org.htw.vis.layer.NetworkNode;

/**
 * search result 
 * @author timo
 *
 */
public class SearchResult {	
	private NetworkNode node;
		
	/* private properties */
	private final String thumbPath;
	private final int fotoliaId;
	private final float similarity;
	private final String words;
	private final int docId;
	
	/**
	 * create a search result
	 * 
	 * @param document
	 * @param documentId
	 * @param similarity
	 */
	public SearchResult(Document document, int documentId, float similarity){
		this.fotoliaId = Integer.parseInt(document.get("fotoliaId"));
		this.words = document.get("words");
		this.thumbPath = document.get("thumbPath");
		this.docId = documentId;				
		this.similarity = similarity;
	}
	
	/**
	 * get string representation
	 */
	public String toString(){			
		return fotoliaId + " :: " + similarity;	
	}
	
	/**
	 * get the fotolia id
	 * @return
	 */
	public int getFotoliaId(){
		return fotoliaId;
	}
	
	/**
	 * get results words
	 * @return
	 */
	public String getWords(){
		return words;
	}
	
	/**
	 * get thumb path
	 * @return
	 */
	public String getThumbPath(){
		return thumbPath;
	}
	
	/**
	 * get the according networkNode instance
	 * @return
	 */
	public NetworkNode getNode(){
		if(node == null){
			node = new NetworkNode(fotoliaId, thumbPath, words);
		}
		return node;
	}

	/**
	 * get the similarity
	 * @return
	 */
	public float getSimilarity() {
		return similarity;
	}
	
	/**
	 * get the image source url
	 * @return
	 */
	public String getImageSource(){		
		return "http://141.45.146.52/jpg/" + thumbPath;		
	}
}
