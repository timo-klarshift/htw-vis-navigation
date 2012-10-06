package com.klarshift.kool.geometry;

import javax.media.opengl.GL2;
import javax.vecmath.Vector3f;

import com.klarshift.kool.render.RenderEngine;
import com.klarshift.kool.scenegraph.VisualNode;

public class BoundingBox extends VisualNode {
	private Vector3f min, max;
	private Vector3f a, b, c, d, e, f, g, h;
	private boolean update = false;	
	
	public BoundingBox(){
		
	}
	
	public BoundingBox(Geometry geometry){
		add(geometry);
	}
	
	public void clear(){
		min = max = null;
		update = true;
	}
	
	public void add(Geometry geometry){
		for(Vector3f v : geometry.getPoints()){
			addPoint(v);
		}
	}
	
	public void add(BoundingBox bbox){
		if(bbox == null)return;
		
		for(Vector3f v : bbox.getPoints()){
			addPoint(v);
		}
	}
	
	public Vector3f[] getPoints(){
		if(update == true){
			calculate();
		}
		return new Vector3f[]{a, b, c, d, e, f, g, h};
	}

	private void addPoint(Vector3f v) {
		float len = v.length();
		if(min == null || len < min.length()){
			min = v;
		}
		
		if(max == null || len > max.length()){
			max = v;
		}
		
		update = true;			
	}
	
	public void calculate(){	
		if(max == null || min == null){
			return;
		}
		
		b = new Vector3f(max.x, min.y, min.z);
		c = new Vector3f(max.x, min.y, max.z);
		d = new Vector3f(min.x, min.y, max.z);
		e = new Vector3f(min.x, max.y, min.z);
		f = new Vector3f(min.x, max.y, max.z);
		h = new Vector3f(max.x, max.y, min.z);
									
		a = min;
		g = max;
		
		update = false;
	}
	
	public void renderNode(GL2 gl, RenderEngine engine){
		if(min == null || max == null){
			return;
		}
		
		if(update == true){
			calculate();
		}
		
		gl.glColor3f(0.2f, 1.0f, 0);
		gl.glBegin(GL2.GL_LINES);
			gl.glVertex3f(a.x, a.y, a.z);
			gl.glVertex3f(b.x, b.y, b.z);
			
			gl.glVertex3f(b.x, b.y, b.z);
			gl.glVertex3f(c.x, c.y, c.z);
			
			gl.glVertex3f(c.x, c.y, c.z);
			gl.glVertex3f(d.x, d.y, d.z);
			
			gl.glVertex3f(d.x, d.y, d.z);
			gl.glVertex3f(a.x, a.y, a.z);
			
			
			gl.glVertex3f(e.x, e.y, e.z);
			gl.glVertex3f(h.x, h.y, h.z);
			gl.glVertex3f(e.x, e.y, e.z);
			gl.glVertex3f(a.x, a.y, a.z);
			
			gl.glVertex3f(h.x, h.y, h.z);
			gl.glVertex3f(g.x, g.y, g.z);
			gl.glVertex3f(h.x, h.y, h.z);
			gl.glVertex3f(b.x, b.y, b.z);
			
			gl.glVertex3f(g.x, g.y, g.z);
			gl.glVertex3f(f.x, f.y, f.z);
			gl.glVertex3f(g.x, g.y, g.z);
			gl.glVertex3f(c.x, c.y, c.z);
			
			gl.glVertex3f(f.x, f.y, f.z);
			gl.glVertex3f(e.x, e.y, e.z);
			gl.glVertex3f(f.x, f.y, f.z);
			gl.glVertex3f(d.x, d.y, d.z);
		gl.glEnd();	
	}
	

}
