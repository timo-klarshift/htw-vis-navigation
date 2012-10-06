package com.klarshift.kool.scenegraph;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4d;

import com.jogamp.opengl.util.gl2.GLUT;
import com.klarshift.kool.appearance.Appearance;
import com.klarshift.kool.camera.Camera;
import com.klarshift.kool.camera.frustum.Frustum;
import com.klarshift.kool.geometry.BoundingBox;
import com.klarshift.kool.geometry.Geometry;
import com.klarshift.kool.render.RenderEngine;

public class VisualNode extends KNode {
	private boolean pickable = false;

	private Geometry geometry;
	private Appearance appearance = new Appearance();
	
	final protected static GLUT glut = new GLUT();
	final protected static GLU glu = new GLU();

	public VisualNode() {
		super();
	}

	public Appearance getAppearance() {
		return appearance;
	}

	public VisualNode(String name) {
		super(name);
	}

	public void setPickable(boolean pickable) {
		this.pickable = pickable;
	}

	public boolean isPickable() {
		return pickable;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setAppearance(Appearance appearance) {
		this.appearance = appearance;
		appearance.setNode(this);
	}




	public void renderNode(GL2 gl, RenderEngine engine) {
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glDisableClientState(GL2.GL_COLOR_ARRAY);

		// apply appearance
		if (appearance != null) {
			appearance.apply(gl, this, engine);
		}

		// render geometry
		if (geometry != null) {

			// render geometry
			geometry.render(gl);
		}
	}
}
