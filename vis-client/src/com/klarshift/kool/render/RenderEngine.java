package com.klarshift.kool.render;

import java.util.LinkedHashMap;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import org.apache.log4j.Logger;

import com.klarshift.kool.camera.Camera;
import com.klarshift.kool.canvas.CanvasListenerAdapter;
import com.klarshift.kool.canvas.RenderCanvas;
import com.klarshift.kool.scenegraph.KNode;

/**
 * scene renderer
 * @author timo
 *
 */
public class RenderEngine  {
	/* canvas */
	private final RenderCanvas canvas;
	
	/* scene */
	private final KNode sceneRoot;
	
	/* camera */
	private Camera camera;
	
	/* logging */
	private final Logger log = Logger.getLogger("RenderEngine");
	
	/* passes */
	private final LinkedHashMap<Integer,ARenderPass> passes = new LinkedHashMap<Integer,ARenderPass>();
	private final SceneRenderer nodeRenderer = new SceneRenderer(this);
	private ARenderPass currentPass = null;
	
	private static RenderEngine engine;
	
	public static RenderEngine create(KNode scene, RenderCanvas canvas) {
		return new RenderEngine(scene, canvas);
	}
	
	public static RenderEngine get(){
		return engine;
	}
	
	/**
	 * create render engine
	 * @param sceneRoot
	 * @param canvas
	 */
	private RenderEngine(KNode sceneRoot, RenderCanvas canvas){
		this.sceneRoot = sceneRoot;
		this.canvas = canvas;
		
		// add listener
		canvas.addListener(new RenderListener());
		
		engine = this;
	}	
	
	public int getWidth(){return canvas.getWidth();}
	public int getHeight(){return canvas.getHeight();}
	
	
	/**
	 * add a render pass to the engine
	 * @param pass
	 */
	public void addPass(ARenderPass pass){
		log.info("Add pass | " + pass.getClass().getName());
		passes.put(pass.getId(), pass);
		pass.setEngine(this);
	}
	
	/**
	 * set a camera
	 * @param c
	 */
	public void setCamera(Camera c){
		log.info("Set camera: " + c);
		camera = c;
	}
	
	/**
	 * get the scene root
	 * @return
	 */
	public KNode getSceneRoot(){
		return sceneRoot;
	}
	
	/**
	 * get the canvas
	 * @return
	 */
	public RenderCanvas getCanvas(){
		return canvas;
	}
	
	/**
	 * get the current pass
	 * @return
	 */
	public ARenderPass getCurrentPass(){
		return currentPass;
	}
	
	/**
	 * get render pass by id
	 * @param id
	 * @return
	 */
	public ARenderPass getPass(int id){
		return passes.get(id);
	}

	/**
	 * render engine canvas listener
	 * @author timo
	 *
	 */
	private class RenderListener extends CanvasListenerAdapter{
		
		@Override
		public void onRender(GL2 gl) {
			
			// setup (could be a pass as well)
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
			
			// keep matrix
			gl.glPushMatrix();
																				
			// render passes
			for(ARenderPass p : passes.values()){
				gl.glPushMatrix();
				if(p.isEnabled()){
					// set pass state
					currentPass = p;
					
					// enable current pass
					p.enable(gl);
					
					// render the scene with node renderer					
					p.render(gl);
					
					// disable current pass
					p.disable(gl);									
				}
				gl.glPopMatrix();
			}	
			
			// reset matrix
			gl.glPopMatrix();
										
			// clean
			gl.glFlush();
		}
		
		@Override
		public void onResize(GLAutoDrawable drawable, int x, int y, int w, int h) {
			// update camera when there is one
			if(camera != null ){				
				camera.updateViewport(x, y, w, h);
			}
		}
		
		@Override
		public void onInit(GLAutoDrawable drawable) {

		}
		
		@Override
		public void onUpdate(long updateTime, long frameCount) {	
			// update scene
			sceneRoot.update(updateTime, frameCount, engine);
			
		}			
	}
	
	/**
	 * get the current camera
	 * @return
	 */
	public Camera getCamera() {
		return camera;
	}

	/**
	 * get a node renderer
	 * @return
	 */
	public SceneRenderer getSceneRenderer() {
		return nodeRenderer;
	}

	
}
