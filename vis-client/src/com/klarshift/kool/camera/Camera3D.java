package com.klarshift.kool.camera;

import javax.media.opengl.GL2;
import javax.vecmath.Vector3f;

import com.klarshift.kool.math.Ray;
import com.klarshift.kool.render.RenderEngine;
import com.klarshift.kool.scenegraph.VisualNode;

/**
 * perspective camera
 * 
 * @author timo
 *
 */
public class Camera3D extends Camera {
	protected double aspect;
	
	
	private double distortion = 1.0;
	
	private boolean crossed = false;
	
	private PerspectiveCamera current = null;

	
	
	PerspectiveCamera leftEye, rightEye;
	private double eyeDistance = 0.1;
	
	public Camera3D(){
		super("Camera 3D");
		init();
	}
	
	public Camera3D(String name){
		super(name);
		init();
	}
	
	@Override
	public void setPickArea(int x, int y) {
		super.setPickArea(x, y);
		leftEye.setPickArea((int)(((double)x) * 0.5), y);
		rightEye.setPickArea((int)(((double)x) * 1.5), y);
	}
	
	@Override
	public Ray getPickRay() {
		Ray l = leftEye.getPickRay();
		Ray r = rightEye.getPickRay();
		if(l == null || r == null){
			return null;
		}
		
		Vector3f pos = new Vector3f(getGlobalPosition());
		Vector3f dir = new Vector3f();
		dir.add(l.getDirection());
		dir.add(r.getDirection());
		dir.scale(0.5f);
		
		
		return new Ray(pos, dir);
		
	}
	
	private void init(){
		leftEye = new PerspectiveCamera("LeftEye");
		rightEye = new PerspectiveCamera("RightEye");
		
		
		
		add(leftEye);
		add(rightEye);
		
		
		
		setFOV(fov);
		setDistortion(distortion);
		setEyeDistance(eyeDistance);
	}
	
	public void setEyeDistance(double eyeDistance){
		this.eyeDistance = eyeDistance;
		double e2 = eyeDistance/2.0;
		leftEye.t().setPosition(e2, 0, 0);
		rightEye.t().setPosition(-e2, 0, 0);
	}
	
	public double getEyeDistance(){
		return eyeDistance;
	}
	
	public void setDistortion(double distortion) {
		this.distortion = distortion;
		leftEye.setDistortion(distortion);
		rightEye.setDistortion(distortion);
	}

	public void setFOV(double fov){
		super.setFOV(fov);
		leftEye.setFOV(fov);
		rightEye.setFOV(fov);
	}

	@Override
	public void project(GL2 gl) {				
		// projection matrix
		/*gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(fov, aspect, 0.1, 1000.0);
		
		// set look at 
		Vector3d p = t().getPosition();
        glu.gluLookAt(p.x, p.y, p.z, lookAt.x, lookAt.y, lookAt.z, upVector.x, upVector.y, upVector.z);*/
	}
	
	@Override
	public void update(long updateTime, long frameCount, RenderEngine engine) {
		super.update(updateTime, frameCount, engine);		
	}
	
	
	
	@Override
	public void render(GL2 gl, RenderEngine engine) {
		renderEye(leftEye, gl, engine);
		renderEye(rightEye, gl, engine);	
	}
	
	@Override
	public boolean isVisible(VisualNode node) {
		return current.isVisible(node);
	}
	
	private void renderEye(PerspectiveCamera eye, GL2 gl, RenderEngine engine){
		current = eye;
		eye.setUpdateViewport(true);
		eye.setUpdateFrustum(true);
		eye.render(gl, engine);		
	}
	
	
	@Override
	public void updateViewport(int x, int y, int width, int height) {		
		super.updateViewport(x, y, width, height);
		
		// update viewport for both eyes
		leftEye.updateViewport(x, y, width/2, height);
		rightEye.updateViewport(width/2, y, width/2, height);
	}	
	
	public void setCrossed(boolean crossed){
		this.crossed = crossed;
	}
}
