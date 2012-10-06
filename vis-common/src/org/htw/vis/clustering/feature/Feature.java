package org.htw.vis.clustering.feature;

import java.io.Serializable;
import java.util.UUID;

/**
 * clustering feature
 * @author timo
 *
 */
public abstract class Feature implements Serializable{
	private String id = UUID.randomUUID().toString();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6404952605121386017L;

	/**
	 * get the distance to another feature
	 * @param other
	 * @return
	 */
	public abstract double getDistance(final Feature other);
	
	/**
	 * initialize the feature
	 * @param f
	 */
	public abstract void randomInit(double f);
	
	/**
	 * random initialize
	 */
	public void randomInit() {
		randomInit(1);		
	}
	
	/**
	 * adjust feature
	 * make it more similar to given feature
	 * @param currentSample
	 * @param f
	 */
	public abstract void adjust(Feature currentSample, float f);
	
	public boolean equals(Object o){
		if(o instanceof Feature == false){
			return false;
		}
		return ((Feature)o).getId().equals(id);
	}
	
	public String getId(){
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
