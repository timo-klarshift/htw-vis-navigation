package com.klarshift.kool.camera;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4d;

import com.klarshift.kool.camera.frustum.Frustum;
import com.klarshift.kool.geometry.Geometry;
import com.klarshift.kool.math.Ray;
import com.klarshift.kool.render.RenderEngine;
import com.klarshift.kool.render.SceneRenderer;
import com.klarshift.kool.scenegraph.KNode;
import com.klarshift.kool.scenegraph.VisualNode;
import com.klarshift.kool.util.RayPicker;

/**
 * abstract camera class
 * 
 * @author timo
 * 
 */
public abstract class Camera extends KNode {
	protected GL2 gl;
	protected final GLU glu = new GLU();

	/* frustum */
	private Frustum frustum;	
	private boolean updateFrustum = true;

	/* viewport */
	protected int vpX;
	protected int vpY;
	protected int vpWidth;
	protected int vpHeight;
	
	private boolean updateViewport = true;
	
	protected double aspect;
	protected double fov = 45.0;
	protected double distortion = 1.0;
	private int pickX = -1;
	private int pickY = -1;
	private Ray pickRay;
	

	/**
	 * create camera
	 */
	public Camera() {
		super("Camera");
	}

	public void setUpdateViewport(boolean b) {
		updateViewport = b;
	}
	
	public void setUpdateFrustum(boolean b) {
		updateFrustum = b;
	}

	public Camera(String name) {
		super(name);
	}

	public int getViewportX() {
		return vpX;
	}

	public int getViewportY() {
		return vpY;
	}

	public double getViewportAspect() {
		return aspect;
	}

	public int getViewportWidth() {
		return vpWidth;
	}

	public int getViewportHeight() {
		return vpHeight;
	}

	/**
	 * create the camera frustum
	 * 
	 * @return
	 */
	public Frustum getFrustum() {
		if (frustum == null) {
			frustum = new Frustum();
		}
		return frustum;
	}

	@Override
	public void update(long updateTime, long frameCount, RenderEngine engine) {
		super.update(updateTime, frameCount, engine);		
	}

	public void updateFrustum(GL2 gl) {
		getFrustum().calculate(gl);
	}

	public void setDistortion(double dist) {
		this.distortion = dist;
	}

	public abstract void project(GL2 gl);

	/**
	 * update the viewport
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void updateViewport(int x, int y, int width, int height) {
		this.vpX = x;
		this.vpY = y;
		this.vpWidth = width;
		this.vpHeight = height;

		aspect = ((double) width / (double) height) * distortion;

		updateViewport = true;
		updateFrustum = true;

		log.info("Update Viewport for : " + getName());
		log.info("\tx     : " + x);
		log.info("\ty     : " + y);
		log.info("\tw     : " + width);
		log.info("\th     : " + height);
		log.info("\taspect: " + 0.01 * Math.round(100 * aspect) + " : 1");
		log.info("\tdist  : " + distortion);
	}

	/**
	 * is node visible from cameras point of view
	 * 
	 * @param camera
	 * @return
	 */
	public boolean isVisible(VisualNode node) {
		Frustum f = getFrustum();
		if (f == null)
			return true;

		Geometry geometry = node.getGeometry();
		if (geometry != null) {
			Matrix4d m = node.getGlobalMatrix();
			if (m == null) {
				return false;
			}

			// when geometry contains more points than bounding box
			// use bounding box to check

			for (Vector3f v : geometry.getPoints()) {
				Matrix4d c = new Matrix4d();
				c.setIdentity();

				c.set(1, new Vector3d(v));

				Vector4d g = new Vector4d();

				c.mul(m);
				c.getColumn(3, g);

				if (f.contains(g)) {
					return true;
				}
			}
			return false;
		}

		return true;
	}
	
	public Ray getPickRay(){
		return pickRay;
	}
	
	public void applyViewport(GL2 gl){
		gl.glViewport(vpX, vpY, vpWidth, vpHeight);
		updateViewport = false;
	}

	public void render(GL2 gl, RenderEngine engine) {
		if (updateViewport == true) {
			applyViewport(gl);
		}
			
		// setup camera projection
		project(gl);
		
		if(updateFrustum  == true){
			updateFrustum(gl);
			updateFrustum = false;
		}
		
		// get pick ray
		pickRay = RayPicker.getRay(gl, pickX, pickY);
		
		// render
		SceneRenderer renderer = engine.getSceneRenderer();
		KNode scene = engine.getSceneRoot();
		renderer.renderScene(gl, scene);
	}

	public String toString() {
		return "CAM: " + getName();
	}

	public void setFOV(double fov) {
		this.fov = fov;
		setUpdateViewport(true);
		setUpdateFrustum(true);
	}

	public double getFOV() {
		return fov;
	}

	public void setPickArea(int x, int y) {
		this.pickX = x;
		this.pickY = y;
	}
}
