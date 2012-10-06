package org.htw.vis.gui

import java.awt.Point;
import java.awt.image.BufferedImage;

import org.htw.vis.layer.NetworkNode;

/**
 * image class
 * 
 * @author timo
 *
 */
class Image {
	final Point target = new Point()
	
	
	int x = 0, y = 0
	final NetworkNode node
	final int id
	float similarity
	
	private boolean selected = false
	
	public void setSelected(boolean s){
		this.selected = s
	}
	
	public boolean isSelected(){
		return selected;
	}
	
	BufferedImage bi
	
	public void setTarget(int x, int y){
		target.x = x
		target.y = y			
	}
	
	public Image(NetworkNode node, float similarity = 0){
		this.node = node
		this.id = node.fotoliaId
		this.similarity = similarity
	}
	
	public boolean equals(Object other){
		if(other instanceof Image == false)return false
		return ((Image)other).id == id
	}
	
	public String toString(){
		return "Image $id ($x/$y)"
	}
	
	public int getWidth(){
		return getBufferedImage().getWidth()
	}
	
	public int getHeight(){
		return getBufferedImage().getHeight()
	}
	
	public String getUrl(){
		return "http://141.45.146.52/jpg/${node.thumbPath}"
	}
	
	public BufferedImage getBufferedImage(){
		if(!bi){			
			File cFile = new File("/tmp/fotolia-${node.fotoliaId}")
			if(cFile.exists()){
				bi = ImageIO.read(cFile)
			}else{
				
				bi = ImageIO.read(new URL(getUrl()))
				
			}								
		}
		return bi
	}
}
