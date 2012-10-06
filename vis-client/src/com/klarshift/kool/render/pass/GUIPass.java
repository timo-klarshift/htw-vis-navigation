package com.klarshift.kool.render.pass;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

import com.jogamp.opengl.util.gl2.GLUT;
import com.klarshift.kool.render.ARenderPass;
import com.klarshift.kool.render.SceneRenderer;
import com.klarshift.kool.scenegraph.KNode;
import com.klarshift.kool.scenegraph.VisualNode;

/**
 * region picking pass
 * 
 * @author timo
 * 
 */
public class GUIPass extends ARenderPass {
	KNode guiRoot = new KNode("GUIRoot");
	
	final GLUT glut = new GLUT();
	final GLU glu = new GLU();
	
	private SceneRenderer renderer;

	public GUIPass() {
		super(ARenderPass.PASS_GUI);
	}

	@Override
	public void enable(GL2 gl) {
		gl.glViewport(0, 0, engine.getWidth(), engine.getHeight());
		
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		
		glu.gluOrtho2D(0.0, engine.getWidth(), engine.getHeight(), 0);
		
		gl.glDisable(GL2.GL_DEPTH_TEST);
	    
	}
	
	public void add(VisualNode node){
		guiRoot.add(node);
	}

	@Override
	public void render(GL2 gl) {
		 if(guiRoot == null){
			 return;
		 }
		 
	
		if(renderer == null){
			renderer = new SceneRenderer(engine); 
		}
		
		 renderer.renderScene(gl, guiRoot);

	    	 
	    	  
	    	
	            

	    
	}

	@Override
	public void disable(GL2 gl) {

	}

	public void update(long updateTime, long frameCount) {
		guiRoot.update(updateTime, frameCount, engine);
	}

	public KNode getRoot() {
		return guiRoot;
	}
}
