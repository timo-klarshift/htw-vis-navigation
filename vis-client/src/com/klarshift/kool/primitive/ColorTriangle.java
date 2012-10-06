package com.klarshift.kool.primitive;

import javax.media.opengl.GL2;
import javax.vecmath.Matrix4d;

import com.klarshift.kool.camera.Camera;
import com.klarshift.kool.render.ARenderPass;
import com.klarshift.kool.render.RenderEngine;
import com.klarshift.kool.render.pass.RegionPickingPass;
import com.klarshift.kool.scenegraph.VisualNode;

public class ColorTriangle extends VisualNode {

	public ColorTriangle() {
		super("ColorTriangle");	
	}	

	@Override
	public void renderNode(GL2 gl, RenderEngine engine) {
		ARenderPass p = engine.getCurrentPass();
		int pid = p.getId();
		if(pid == ARenderPass.PASS_COLOR){		
			gl.glBegin(GL2.GL_TRIANGLES);                      // Drawing Using Triangles
				gl.glColor3f(1, 0, 0);
				gl.glVertex3f( 0.0f, 1.0f, 0.0f);              // Top
				    
			    gl.glColor3f(0, 1, 0);
			    gl.glVertex3f(-1.0f,-1.0f, 0.0f);  
			    
			    gl.glColor3f(0, 0, 1);
			    gl.glVertex3f( 1.0f,-1.0f, 0.0f);              // Bottom Right
				
			gl.glEnd();
		}else if(pid == ARenderPass.PASS_PICKING){
			RegionPickingPass pp = (RegionPickingPass)p;
			pp.setPickColor(gl, this);
			gl.glBegin(GL2.GL_TRIANGLES);
				gl.glVertex3f( 0.0f, 1.0f, 0.0f);
				gl.glVertex3f(-1.0f,-1.0f, 0.0f);  			  
				gl.glVertex3f( 1.0f,-1.0f, 0.0f);
			gl.glEnd();	
		}
	}

}
