package org.htw.vis.server.context;

import java.io.Serializable;

import org.htw.vis.layer.INode;
import org.htw.vis.layer.NetworkNode;
import org.htw.vis.lucene.SearchResult;

public class ContainerImage implements INode, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1703165601747766209L;
	
	private Integer fotoliaId;
	private String thumbPath;
	private float similarity;
	//private ByteFeature feature;
	private String words = null;
	
	//private static transient FeatureAccess helper;
	
	private Integer x = -1, y = -1;

	private boolean mapped = false;
	
	public ContainerImage(NetworkNode node){
		this.fotoliaId = node.getFotoliaId();
		this.thumbPath = node.getThumbPath();
		this.similarity = -1;	
		this.words = node.getWords();
	}
	
	public boolean isMapped(){
		return mapped;
	}
	
	public String getWords(){
		return words;
	}
	
	/*public ByteFeature getFeatureVector(){
		if(feature == null){
			feature = helper.getFeature(fotoliaId);
			
			// TODO set id unique?
		}
		return feature;
	}*/
	
	public void map(Integer x, Integer y){
		this.x = x;
		this.y = y;
		this.mapped  = true;
	}
	
	public Integer getX(){
		return this.x;
	}
	
	public Integer getY(){
		return this.y;
	}
	
	public ContainerImage(SearchResult result){
		this.fotoliaId = result.getFotoliaId();
		this.thumbPath = result.getThumbPath();
		this.similarity = result.getSimilarity();
		this.words = result.getWords();
	}
	
	public float getSimilarity(){
		return similarity;
	}
	
	
	public ContainerImage(Integer fotoliaId, String thumbPath){
		this.fotoliaId = fotoliaId;
		this.thumbPath = thumbPath;
		this.similarity = -1;
	}
	
	public ContainerImage(Integer fotoliaId, String thumbPath, String words){
		this.fotoliaId = fotoliaId;
		this.thumbPath = thumbPath;
		this.similarity = -1;
		this.words = words;
	}

	public Integer getFotoliaId() {
		return fotoliaId;
	}
	
	public String getThumbPath(){
		return thumbPath;
	}
	
	public String getImageSource(){		
		return "http://141.45.146.52/jpg/" + thumbPath;		
	}
	
	public String toString(){
		return "" + getFotoliaId() + " :: " + x + "/" + y;
	}
}
