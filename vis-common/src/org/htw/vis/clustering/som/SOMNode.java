package org.htw.vis.clustering.som;

import java.util.UUID;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.htw.vis.clustering.feature.Feature;

/**
 * som node
 * 
 * @author timo
 * 
 */
public class SOMNode {
	private int x, y;
	private final String id = UUID.randomUUID().toString();
	

	private Feature weight;

	

	/**
	 * create a new som node
	 * 
	 * @param x
	 * @param y
	 * @param weight
	 */
	public SOMNode(int x, int y, Feature weight) {
		this.x = x;
		this.y = y;
		this.weight = weight;

		init();
	}
	
	public void setPosition(int x, int y){
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	private void init() {
		randomize();
	}
	
	public void randomize(){
		weight.randomInit(1);
	}

	public String getId() {
		return id;
	}

	public String toString() {
		return id + "/" + x + "/" + y;
	}

	public Feature getWeight() {
		return weight;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SOMNode == false)
			return false;
		SOMNode o = (SOMNode) obj;
		return o.getId().equals(id);
	}
}
