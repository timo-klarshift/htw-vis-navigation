package com.klarshift.kool.appearance;

import javax.media.opengl.GL2;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.jogamp.opengl.util.texture.TextureCoords;
import com.klarshift.kool.geometry.Geometry;
import com.klarshift.kool.render.ARenderPass;
import com.klarshift.kool.render.RenderEngine;
import com.klarshift.kool.render.pass.RegionPickingPass;
import com.klarshift.kool.scenegraph.VisualNode;

public class Appearance {
	private Vector3f color = new Vector3f(new Vector3d(Math.random(), Math.random(), Math.random()));
	private KTexture texture;
	private VisualNode node;
	private Float transparency = 0f;
	
	public Appearance(){
		
	}
	
	public void setTransparency(float transparency){
		this.transparency = transparency;
	}
	
	public void setNode(VisualNode node){
		this.node = node;
	}
		
	public Appearance setColor(Vector3f color){
		this.color = color;
		return this;
	}
	
	public Vector3f getColor(){
		return color;
	}
	
	public void setTexture(KTexture texture){
		this.texture = texture;			
	}

	/**
	 * apply the appearance
	 * @param gl
	 * @param node
	 * @param engine
	 */
	public void apply(GL2 gl, VisualNode node, RenderEngine engine) {
		// get current pass
		ARenderPass p = engine.getCurrentPass();
		
		// picking pass
		if(p.getId() == ARenderPass.PASS_PICKING){
			if(node.isPickable()){
				RegionPickingPass pp = (RegionPickingPass)p;
				pp.setPickColor(gl, node);
				return;
			}
		}else{
			// handle transparency
			if(transparency > 0){
				// alpha blending
				gl.glEnable(GL2.GL_BLEND);
				gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
			}else{
				// alpha blending
				gl.glDisable(GL2.GL_BLEND);				
			}
			
			gl.glColor4f(color.x, color.y, color.z, 1.0f-transparency);
			
			if(texture != null && texture.isEnabled() && texture.isLoaded()){
				texture.apply(gl);
			}else{
				
			}
		}				
	}

	public float getTransparency() {
		return transparency;
	}
}
