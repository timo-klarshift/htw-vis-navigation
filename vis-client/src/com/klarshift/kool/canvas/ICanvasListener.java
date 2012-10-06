package com.klarshift.kool.canvas;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

public interface ICanvasListener {
	public void onStart();
	public void onStop();
	public void onResize(GLAutoDrawable drawable, int x, int y, int width, int height);
	public void onUpdate(long updateTime, long frameCount);
	public void onInit(GLAutoDrawable drawable);
	public void onRender(GL2 gl);	
}
