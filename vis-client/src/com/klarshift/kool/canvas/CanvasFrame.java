package com.klarshift.kool.canvas;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JFrame;

/**
 * canvas frame
 * @author timo
 *
 */
public class CanvasFrame extends JFrame implements ICanvasListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6358652650109666098L;
	private final RenderCanvas canvas;
	private final String title;
	
	/**
	 * create a canvas frame
	 * @param canvas
	 * @param width
	 * @param height
	 */
	public CanvasFrame(String title, RenderCanvas canvas, int width, int height){
		// store canvas
		this.canvas = canvas;
		this.title = title;				
		setTitle(title);
		
		// add listener
		canvas.addListener(this);
		add(canvas);
		
		//setUndecorated(true);
		//setResizable(false);
		//setIgnoreRepaint(true);
		setAlwaysOnTop(true);
		

		
		// set size and pack
		setPreferredSize(new Dimension(width, height));
		setSize(width, height);
		pack();
		
		/*GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();             
       
		 if(false && graphicsDevice.isFullScreenSupported()) {
	       	 
	       	 graphicsDevice.setFullScreenWindow(this);
	       	 validate();
	       	 setVisible(true);
	        }
        
        DisplayMode[] dmodes = graphicsDevice.getDisplayModes();             
        for(int i=0;i<dmodes.length;i++) System.out.println(i + " " + dmodes[i].getWidth() + " " + dmodes[i].getHeight() + " " + dmodes[i].getRefreshRate() + " "  + dmodes[i].getBitDepth());
        
        
        DisplayMode dm = new DisplayMode(1920, 1080, -1, 60);
        graphicsDevice.setDisplayMode(dm);
		*/
		// show		
		 setVisible(true);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}	

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResize(GLAutoDrawable drawable, int x, int y, int width,
			int height) {
		
	}

	@Override
	public void onUpdate(long updateTime, long fc) {
		// show title
		if(fc % 50 == 0){
			setTitle(title + " :: FPS = " + canvas.getFps());
		}		
	}

	@Override
	public void onInit(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRender(GL2 gl) {
		// TODO Auto-generated method stub
		
	}

}
