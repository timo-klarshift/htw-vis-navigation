package com.klarshift.kool.math;

import javax.vecmath.Vector3f;

/**
 * ray plane intersection
 * @author timo
 *
 */
public class RayPlaneIntersection {
	private Ray ray;
	private Plane plane;
	private Vector3f intersectionPoint;
	private IntersectionType intersectionType;
	
	public static final float EPSILON = 0.00001f; 
	
	/**
	 * intersection types
	 * @author timo
	 *
	 */
	public enum IntersectionType {
		NONE, INTERSECTS, INSIDE
	}
	
	/**
	 * create a new ray plane intersection
	 * @param ray
	 * @param plane
	 */
	public RayPlaneIntersection(Ray ray, Plane plane){
		this.ray = ray;
		this.plane = plane;		
	}
	
	/**
	 * 
	 * @param ray
	 */
	public RayPlaneIntersection(Ray ray){
		this.ray = ray;
	}
	
	public RayPlaneIntersection(Plane plane){
		this.plane = plane;
	}
	
	/**
	 * set the ray
	 * @param ray
	 */
	public void setRay(Ray ray){
		this.ray = ray;
	}
	
	/**
	 * set the plane
	 * @param plane
	 */
	public void setPlane(Plane plane){
		this.plane = plane;
	}
	
	/**
	 * get the intersection point
	 * @return
	 */
	public Vector3f getIntersectionPoint(){
		return intersectionPoint;
	}
	
	/**
	 * get the type of intersection
	 * @return
	 */
	public IntersectionType getType(){
		return intersectionType;
	}
	
	/**
	 * perform intersection
	 */
	public void intersect(){
		if(plane == null)	throw new NullPointerException("No plane given for intersection.");
		if(ray == null)	throw new NullPointerException("No ray given for intersection.");
		
		Vector3f tmp = new Vector3f();
		
		// numerator
		tmp.sub(plane.getPosition(), ray.getPosition());
		float num = tmp.dot(plane.getNormal());		
		
		// denominator
		tmp = new Vector3f(ray.getDirection());
		float denom = tmp.dot(plane.getNormal());
		
		// distance / parameter
		float dist = num/denom;
		
		// outside and parallel
		// denominator = zero
		// numerator != zero
		if(Math.abs(denom) <= EPSILON && Math.abs(num) > EPSILON){
			intersectionType = IntersectionType.NONE;
			return;
		}
		
		// inside and parallel
		// denominator = zero
		// numerator == zero
		if(Math.abs(denom) <= EPSILON && Math.abs(num) <= EPSILON){
			intersectionType = IntersectionType.INSIDE;
			return;
		}
		
		// get intersection point
		intersectionType = IntersectionType.INTERSECTS;
		intersectionPoint = new Vector3f(ray.getPosition());
		Vector3f dir = new Vector3f(ray.getDirection());
		dir.scale(dist);
		intersectionPoint.add(dir);
	}
	
	/**
	 * debug the intersection
	 */
	public void print(){
		System.out.println("Ray-Plane-Intersection");
		System.out.println("\tRay    : " + ray);
		System.out.println("\tPlane  : " + plane);		
		if(intersectionType == IntersectionType.INTERSECTS){
			System.out.println("\tResult : Intersection at  " + intersectionPoint);
		}else{
			System.out.println("\tResult : " + (intersectionType == IntersectionType.NONE ? "None" : "Contained"));	
		}
	}
	
	/**
	 * perform intersection and print results
	 */
	public void intersectAndPrint(){
		intersect();
		print();
	}
	
	public static void main(String[] args){
		// ray
		Ray r = new Ray(new Vector3f(0, 0, 10), new Vector3f(0, 0, -1));
		RayPlaneIntersection rpi = new RayPlaneIntersection(r);
		
		// intersection
		Plane p1 = new Plane(new Vector3f(0, 0, 0), new Vector3f(0, 0, -1));
		rpi.setPlane(p1);		
		rpi.intersectAndPrint();
		
		// parallel
		Plane p2 = new Plane(new Vector3f(-1, 0, 0), new Vector3f(1, 0, 0));
		rpi.setPlane(p2);		
		rpi.intersectAndPrint();
		
		// contained
		Plane p3 = new Plane(new Vector3f(0, 0, 0), new Vector3f(1, 0, 0));
		rpi.setPlane(p3);		
		rpi.intersectAndPrint();
	}
}
