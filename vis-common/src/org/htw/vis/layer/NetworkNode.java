package org.htw.vis.layer;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * network node
 * 
 * @author timo
 *
 */
public class NetworkNode implements INode {	
	private Integer fotoliaId = -1;
	private String thumbPath;
	private String words;
	private int x, y;
	private int parentId;
	
	/**
	 * create a new network node
	 * 
	 * @param fotoliaId
	 * @param thumbPath
	 * @param words
	 */
	public NetworkNode(int fotoliaId, String thumbPath, String words){
		this.fotoliaId = fotoliaId;
		this.thumbPath = thumbPath;
		this.words = words;				
	}
	
	/**
	 * create network node from sql resultSet
	 * @param rs
	 */
	public NetworkNode(ResultSet rs){
		try {
			this.fotoliaId = rs.getInt("fotoliaId");
			this.thumbPath = rs.getString("thumbPath");
			this.words = rs.getString("words");	
			this.parentId = rs.getInt("parent");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public Integer getParentId(){
		return parentId;
	}
	
	public static NetworkNode withPosition(ResultSet rs){
		NetworkNode n = new NetworkNode(rs);
		
		try {
			n.setPosition(rs.getInt("x"), rs.getInt("y"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return n;
	}
	
	public void setPosition(int x , int y){
		this.x = x;
		this.y = y;
	}
	
	/**
	 * get comma seperated keywords
	 * @return
	 */
	public String getWords(){
		return words;
	}
	
	/**
	 * set keywords
	 * @param words
	 */
	public void setWords(String words){
		this.words = words;
	}
	
	/**
	 * get fotolia id
	 * @return
	 */
	public Integer getFotoliaId(){
		return fotoliaId;
	}
	
	/**
	 * get string representation
	 */
	public String toString(){		
		return ""+fotoliaId + " / " + words;
	}
	
	/**
	 * get image source
	 * this is the complete url prefixed with cvision ip address
	 * @return
	 */
	public String getImageSource(){		
		return "http://141.45.146.52/jpg/" + thumbPath;		
	}
	
	public boolean equals(Object other){
		if(other instanceof NetworkNode == false)return false	;	
		return ((NetworkNode)other).fotoliaId == fotoliaId;
	}

	/**
	 * get the thumb path
	 * @return
	 */
	public String getThumbPath() {
		return thumbPath;
	}

	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
}
