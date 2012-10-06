package com.klarshift.kool.render;

import javax.media.opengl.GL2;

import com.klarshift.kool.camera.Camera;
import com.klarshift.kool.scenegraph.KNode;

/**
 * render pass
 * @author timo
 *
 */
public abstract class ARenderPass {
	protected RenderEngine engine;
	private int id;
	
	/**
	 * color pass
	 */
	public static final int PASS_COLOR = 100;
	
	public static final int PASS_GUI = 200;
	
	/**
	 * picking pass
	 */
	public static final int PASS_PICKING = 0;
	
	/**
	 * active state
	 */
	private boolean enabled = true;
	
	/**
	 * enable / disable render pass
	 * @param e
	 */
	public void setEnabled(boolean e){
		enabled = e;
	}
	
	/**
	 * is pass enabled
	 * @return
	 */
	public boolean isEnabled(){
		return enabled;
	}
	
	/**
	 * create a new render pass
	 * @param id
	 */
	public ARenderPass(int id){
		this.id = id;
	}
	
	/**
	 * get render pass id
	 * @return
	 */
	public int getId(){
		return id;
	}
		
	/**
	 * enable the render pass
	 * @param gl
	 */
	public abstract void enable(GL2 gl);
	
	/**
	 * disable the render pass
	 * @param gl
	 */
	public abstract void disable(GL2 gl);
	
	/**
	 * render 
	 * @param gl
	 */
	public void render(GL2 gl){
		Camera camera = engine.getCamera();
		if(camera != null){
			camera.render(gl, engine);
		}else{
			SceneRenderer sceneRenderer = engine.getSceneRenderer();
			KNode sceneRoot = engine.getSceneRoot();
			sceneRenderer.renderScene(gl, sceneRoot);
		}		
	}	
	
	/**
	 * set the engine
	 * @param engine
	 */
	public void setEngine(RenderEngine engine){
		this.engine = engine;
	}	
}
