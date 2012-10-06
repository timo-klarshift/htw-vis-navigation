package com.klarshift.kool.gui;

import javax.media.opengl.GL2;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.htw.vis.client.main.Application;

import com.klarshift.kool.appearance.Appearance;
import com.klarshift.kool.appearance.TextureManager;
import com.klarshift.kool.render.RenderEngine;
import com.klarshift.kool.scenegraph.VisualNode;

public class EngineStats extends VisualNode {
	private int fps;
	private int nodeCount;
	private int textureCount;

	Appearance a = new Appearance();
	private int currentLayer;

	public EngineStats() {
		super("EngineStats");
		a.setTransparency(0.3f);
		a.setColor(new Vector3f(0.2f, 0.2f, 0.2f));
		setAppearance(a);
	}

	@Override
	public void renderNode(GL2 gl, RenderEngine engine) {
		super.renderNode(gl, engine);

		Vector3d pos = t().getPosition();
		double x = pos.x;
		double y = pos.y;

		gl.glPushMatrix();
		gl.glTranslated(0, 0, -0.1);
		gl.glScaled(140, 80, 0);

		gl.glBegin(GL2.GL_QUADS);

		gl.glVertex3f(1.0f, 1.0f, .0f); // Top Right Of The Quad (Front)
		gl.glVertex3f(-1.0f, 1.0f, .0f); // Top Left Of The Quad (Front)
		gl.glVertex3f(-1.0f, -1.0f, .0f); // Bottom Left Of The Quad (Front)
		gl.glVertex3f(1.0f, -1.0f, .0f);
		gl.glEnd();
		gl.glPopMatrix();

		gl.glColor3f(.0f, 1.0f, 0.0f);

		gl.glScaled(1, 1, 1);

		gl.glRasterPos2d(x, y);
		glut.glutBitmapString(2, "FPS     : " + fps);

		gl.glRasterPos2d(x, y + 20);
		glut.glutBitmapString(2, "Nodes   : " + nodeCount);

		gl.glRasterPos2d(x, y + 40);
		glut.glutBitmapString(2, "Textures: " + textureCount);
		
		gl.glRasterPos2d(x, y + 60);
		glut.glutBitmapString(2, "Layer: " + currentLayer);

	}

	@Override
	public void update(long updateTime, long frameCount, RenderEngine engine) {
		super.update(updateTime, frameCount, engine);
		if (frameCount % 30 == 0) {
			fps = (int) engine.getCanvas().getFps();
			nodeCount = engine.getSceneRenderer().getVisibleCount();
			textureCount = TextureManager.getInstance().count();
			currentLayer = Application.getInstance().getLogic().getCurrentLayer();
		}
	}
}
