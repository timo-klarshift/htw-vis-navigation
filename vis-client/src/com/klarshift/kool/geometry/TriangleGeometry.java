package com.klarshift.kool.geometry;

import javax.media.opengl.GL2;
import javax.vecmath.Vector3f;

import com.klarshift.kool.math.Triangle;

/**
 * triangle geometry
 * @author timo
 *
 */
public class TriangleGeometry extends Geometry {
	float width, height;
	
	/**
	 * create triangle geometry
	 * @param width
	 * @param height
	 */
	public TriangleGeometry(float width, float height) {
		super(GL2.GL_TRIANGLES);
		
		this.width = width;
		this.height = height;
		float h2 = height/2.0f;
		float w2 = width/2.0f;
		
		// create geometry
		addPoint(new Vector3f(-1*w2, -1*h2, 0))
			.addPoint(new Vector3f(1*w2, -1*h2, 0))
			.addPoint(new Vector3f(0, 1*h2, 0));
		
		try {
			compile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public Triangle getTriangle(){
		return new Triangle(getPoints());
	}
}
