package org.htw.vis.server.protocol;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.htw.vis.server.context.ContainerImage;

/**
 * update packet
 * 
 * @author timo
 *
 */
public class VISUpdate implements Serializable {
	/* added images */
	private List<ContainerImage> images = new ArrayList<ContainerImage>();
	
	/* removed images */
	private Set<Integer> removed = new HashSet<Integer>();
	
	public ContainerImage image;
	
	/* shift information */
	public int shiftX = 0;
	public int shiftY = 0;
	public int shiftZ = 0;
	public int layer;
	
	/**
	 * remove image
	 * @param image
	 */
	public void removeImage(ContainerImage image){
		removed.add(image.getFotoliaId());
	}
	
	/**
	 * remove image by id
	 * @param id
	 */
	public void removeImage(Integer id){
		removed.add(id);
	}
	
	/**
	 * add image
	 * @param image
	 */
	public void addImage(ContainerImage image){
		images.add(image);
	}
	
	/**
	 * set images
	 * @param collection
	 */
	public void setImages(List<ContainerImage> collection){
		images = collection;
	}
	
	public VISUpdate(){
		
	}
	
	public Set<Integer> getRemovedImages(){
		return removed;
	}
	
	public List<ContainerImage> getImages(){
		return images;
	}

	public void setRemovedImages(Set<Integer> removed) {
		this.removed = removed;
		
	}
}
