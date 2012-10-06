package com.klarshift.kool.geometry;

import javax.media.opengl.GL2;
import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

/**
 * plane geometry
 * @author timo
 *
 */
public class PlaneGeometry extends Geometry {
	float width, height;
	
	public float getWidth(){
		return width;
	}
	
	public float getHeight(){
		return height;
	}
	
	public PlaneGeometry(float width, float height) {
		super(GL2.GL_TRIANGLE_STRIP);
		
		this.width = width;
		this.height = height;
		float h2 = height/2;
		float w2 = width/2;
		
		// create geometry
		// clockwise cause of triangle strip
		addPoint(new Vector3f(-1*w2, -1*h2, 0))
			.addPoint(new Vector3f(-1*w2, 1*h2, 0))
			.addPoint(new Vector3f(1*w2, -1*h2, 0))
			.addPoint(new Vector3f(1*w2, 1*h2, 0));
		
		// texture coords (flipped to display correctly?)
		addTexCoord(new Point2f(0, 1));
		addTexCoord(new Point2f(0, 0));
		addTexCoord(new Point2f(1, 1));
		addTexCoord(new Point2f(1, 0));
		
		try {
			compile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
