package com.klarshift.kool.math;

import javax.vecmath.Vector3f;

/**
 * plane
 * @author timo
 *
 */
public class Plane {
	private Vector3f position, normal;
	
	/**
	 * create plane from a position vector 
	 * and a normal vector
	 * @param p0
	 * @param n
	 */
	public Plane(Vector3f p0, Vector3f n){
		this.position = p0;		
		this.normal = n;
	}
	
	/**
	 * create plane from position vector
	 * and two spanning vectors
	 * @param p0
	 * @param a
	 * @param b
	 */
	public Plane(Vector3f p0, Vector3f a, Vector3f b){
		this.position = p0;
		normal = new Vector3f();
		normal.cross(a, b);		
	}
	
	public Vector3f getPosition(){
		return position;		
	}
	
	public Vector3f getNormal(){
		return normal;
	}
	
	public String toString(){
		return "PLANE [" + position + "/" + normal + "]";
	}
}

