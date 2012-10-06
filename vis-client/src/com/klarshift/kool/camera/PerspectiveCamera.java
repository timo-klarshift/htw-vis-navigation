package com.klarshift.kool.camera;

import javax.media.opengl.GL2;
import javax.vecmath.Vector3d;

import com.klarshift.kool.render.RenderEngine;

/**
 * perspective camera
 * 
 * @author timo
 *
 */
public class PerspectiveCamera extends Camera {
	
	
	
	public PerspectiveCamera(){
		super("PerspectiveCamera");
	}
	
	public PerspectiveCamera(String name){
		super(name);
	}

	@Override
	public void project(GL2 gl) {				
		// projection matrix
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(fov, aspect, 0.1, 1000.0);
		
		// set look at 
		Vector3d p = getGlobalPosition();
        glu.gluLookAt(p.x, p.y, p.z, lookAt.x, lookAt.y, lookAt.z, upVector.x, upVector.y, upVector.z);               
	}	

	

	
	
	
}
