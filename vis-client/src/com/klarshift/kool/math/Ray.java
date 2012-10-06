package com.klarshift.kool.math;

import javax.vecmath.Vector3f;

/**
 * ray
 * @author timo
 *
 */
public class Ray {
	private Vector3f position, direction;
	
	/**
	 * create a ray
	 * @param p
	 * @param d
	 */
	public Ray(Vector3f p, Vector3f d){
		this.position = p;
		this.direction = d;
	}	
	
	public String toString(){
		return "RAY [" + position + "/" + direction + "]"; 
	}
	
	public Vector3f getPosition(){
		return position;
	}
	
	public Vector3f getDirection(){
		return direction;
	}
}
