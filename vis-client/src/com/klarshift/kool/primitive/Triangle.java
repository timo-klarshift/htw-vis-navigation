package com.klarshift.kool.primitive;

import javax.vecmath.Matrix4d;

import com.klarshift.kool.geometry.TriangleGeometry;
import com.klarshift.kool.math.Ray;
import com.klarshift.kool.math.RayTriangleIntersection;
import com.klarshift.kool.scenegraph.VisualNode;

/**
 * triangle primitive
 * @author timo
 *
 */
public class Triangle extends VisualNode{
	TriangleGeometry geo;
	
	public Triangle(float width, float height){
		geo = new TriangleGeometry(width, height);
		setGeometry(geo);
	}
	
	/**
	 * intersect with ray (world coordinates)
	 * @param ray
	 * @return
	 */
	public boolean intersect(Ray ray){
		com.klarshift.kool.math.Triangle t = geo.getTriangle();
		Matrix4d m = getGlobalMatrix();		
		t.transform(m);		
		RayTriangleIntersection rt = new RayTriangleIntersection();
		return rt.intersectTriangle(ray, t);		
	}
}
