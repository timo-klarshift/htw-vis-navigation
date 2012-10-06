package org.htw.vis.api;

import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.htw.vis.helper.ImageAccess;

/**
 * api image
 * @author timo
 *
 */
public class ApiImage {
	private final Integer fotoliaId;
	private final String thumbPath;
	private Integer x, y;
	private float similarity;
	private Image bi;
	private static ImageAccess access = new ImageAccess(); 
	
	/**
	 * 
	 * @param fotoliaId
	 * @param thumbPath
	 */
	public ApiImage(Integer fotoliaId, String thumbPath){
		this.fotoliaId = fotoliaId;
		this.thumbPath = thumbPath;		
	}
	
	/**
	 * 
	 * @param fotoliaId
	 * @param thumbPath
	 */
	public ApiImage(Integer fotoliaId, String thumbPath, float similarity){
		this.fotoliaId = fotoliaId;
		this.thumbPath = thumbPath;
		this.similarity = similarity;
	}
	
	public ApiImage(Integer fotoliaId, String thumbPath, float similarity, int x, int y){
		this.fotoliaId = fotoliaId;
		this.thumbPath = thumbPath;
		this.similarity = similarity;
		this.x = x;
		this.y = y;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public String toString(){
		return similarity + " :: " + fotoliaId + " :: " + " (" + x + "/" + y + ") " + thumbPath;
	}
	
	public Integer getFotoliaId(){
		return fotoliaId;
	}
	
	public String getThumbPath(){
		return thumbPath;
	}
	
	public String getImageSource(){		
		return "http://141.45.146.52/jpg/" + thumbPath;		
	}
	
	public boolean equals(Object other){
		if(other instanceof ApiImage == false){
			return false;
		}
		return ((ApiImage)other).getFotoliaId().equals(fotoliaId);
	}
	
	public Image getBufferedImage(){
		return access.getImage(fotoliaId, getImageSource());		
	}
}
