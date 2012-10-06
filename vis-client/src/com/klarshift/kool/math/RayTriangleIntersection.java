package com.klarshift.kool.math;

import javax.vecmath.Vector3f;

/**
 * ray triangle intersection
 * 
 * @author timo
 * 
 */
public class RayTriangleIntersection {

	private Vector3f edge1 = new Vector3f();
	private Vector3f edge2 = new Vector3f();
	private Vector3f tvec = new Vector3f();
	private Vector3f pvec = new Vector3f();
	private Vector3f qvec = new Vector3f();

	private static final float EPSILON = 0.000001f;

	public boolean intersectTriangle(Ray ray, Triangle triangle) {
		Vector3f vert0 = triangle.a;
		Vector3f vert1 = triangle.b;
		Vector3f vert2 = triangle.c;
		
		Vector3f rayD = ray.getDirection();
		Vector3f rayP = ray.getPosition();
		
		// Find vectors for two edges sharing vert0
		edge1.sub(vert1, vert0);
		edge2.sub(vert2, vert0);

		// Begin calculating determinant -- also used to calculate U parameter
		pvec.cross(rayD, edge2);

		// If determinant is near zero, ray lies in plane of triangle
		float det = edge1.dot(pvec);

		if (det > -EPSILON && det < EPSILON)
			return false;

		float invDet = 1.0f / det;

		// Calculate distance from vert0 to ray origin
		tvec.sub(rayP, vert0);

		// Calculate U parameter and test bounds
		float u = tvec.dot(pvec) * invDet;
		if (u < 0.0f || u > 1.0f)
			return false;

		// Prepare to test V parameter
		qvec.cross(tvec, edge1);

		// Calculate V parameter and test bounds
		float v = ray.getDirection().dot(qvec) * invDet;
		if (v < 0.0f || (u + v) > 1.0f)
			return false;

		// Calculate t, ray intersects triangle
		float t = edge2.dot(qvec) * invDet;

		Vector3f tuv = new Vector3f();
		tuv.set(t, u, v);
		
		return true;
	}

	/**
	 * intersect ray with triangle
	 * 
	 * @param ray
	 * @param triangle
	 * @return
	 */
	public static Vector3f intersect(Ray ray, Triangle triangle) {
		return new Vector3f();
	}

	/**
	 * intersection test
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		RayTriangleIntersection ti = new RayTriangleIntersection();
		
		// create the triangle
		Triangle t = new Triangle(new Vector3f(-1, -1, 0), new Vector3f(
				1, -1, 0), new Vector3f(0, 1, 0));

		// create the ray
		Ray r = new Ray(new Vector3f(0, 0, 0), new Vector3f(0, 0, -1));
		

		if(ti.intersectTriangle(r, t)){
			// result
			System.out.println("TRUE");
		}else{
			System.out.println("NO INTERS");
		}

		
	}
}
