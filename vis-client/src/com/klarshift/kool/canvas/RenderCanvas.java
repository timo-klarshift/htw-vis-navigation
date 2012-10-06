package com.klarshift.kool.canvas;

import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;

import org.apache.log4j.Logger;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 * render canvas abstraction
 * 
 * @author timo
 * 
 */
public class RenderCanvas extends GLCanvas implements GLEventListener {
	private static final long serialVersionUID = 1L;
	private static final Logger log = Logger.getLogger("RenderCanvas");
	private static final int MAX_FPS = 100;

	private ConcurrentLinkedQueue<ICanvasListener> listeners = new ConcurrentLinkedQueue<ICanvasListener>();

	/* gl properties */
	private GLCapabilities caps;
	private GL2 gl;
	private GLUT glut;

	/* frame calculation */
	private long lastUpdate = 0;
	private long updateTime = 0;
	private int fps = 0;
	private long frameCount = 0;

	/* animator */
	final FPSAnimator animator;
	private GLAutoDrawable drawable;

	// init
	static {
		String osName = System.getProperty("os.name");
		if (osName.equals("Linux")) {
			initLinux();
		} else if (osName.equals("Mac")) {

		}
	}

	/**
	 * do some linux specific setup
	 */
	static void initLinux() {
		log.info("Init OpenGL for linux ...");
		GLProfile.initSingleton(true);
	}

	/**
	 * create render canvas
	 * 
	 * @return
	 */
	public static RenderCanvas create() {
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);
		caps.setDoubleBuffered(true);

		return new RenderCanvas(caps);
	}

	public GL2 getGl() {
		return gl;
	}

	/**
	 * canvas constructor
	 * 
	 * @param caps
	 */
	public RenderCanvas(GLCapabilities caps) {
		this.caps = caps;
		glut = new GLUT();

		addGLEventListener(this);
		requestFocusInWindow();

		// create animator
		animator = new FPSAnimator(this, MAX_FPS, true);
		log.info("RenderCanvas created.");
	}

	/**
	 * add listener
	 * @param l
	 */
	public void addListener(ICanvasListener l) {
		listeners.add(l);
		log.debug("Listener added. / " + listeners.size());
	}

	/**
	 * remove listener
	 * @param l
	 */
	public void removeListener(ICanvasListener l) {
		listeners.remove(l);
		log.debug("Listener removed. / " + listeners.size());
	}

	/**
	 * start animator
	 */
	public void start() {
		animator.start();
		for (ICanvasListener l : listeners) {
			l.onStart();
		}
		log.debug("Animator started.");
	}

	/**
	 * stop
	 */
	public void stop() {
		animator.stop();
		for (ICanvasListener l : listeners) {
			l.onStop();
		}
		log.debug("Animator stopped.");
	}

	/**
	 * get total frames rendered
	 * 
	 * @return
	 */
	public long getFrameCount() {
		return frameCount;
	}

	/**
	 * get frames per second
	 * 
	 * @return
	 */
	public float getFps() {
		return fps;
	}

	@Override
	public void display(GLAutoDrawable drawable) {

		// update the scene
		long startUpdate = new Date().getTime();
		update();
		long ut = new Date().getTime() - startUpdate;

		// render the scene
		long startRender = new Date().getTime();
		render();
		long rt = new Date().getTime() - startRender;

		long totalTime = rt + ut;

		// frame stats
		frameCount++;

		// print some stats every n frames
		if (frameCount % 1000 == 0) {
			log.info("UPDATE: " + ut);
			log.info("RENDER: " + rt);
			log.info("TOTAL : " + totalTime);
			log.info("XX    : " + updateTime);
			log.info("FPS   : " + fps);
		}

		//

		long stop = new Date().getTime();
		updateTime = stop - lastUpdate;

		fps = (int) Math.round(1000.0 / updateTime);

		lastUpdate = stop;
	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		log.info("Dispose()");
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		setCurrentContext(drawable);

		// init open gl
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glClearColor(0f, 0f, 0f, 1.0f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LEQUAL);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);

		// inform listeners
		for (ICanvasListener l : listeners) {
			l.onInit(drawable);
		}

		log.info("Initialized.");
	}

	private void setCurrentContext(GLAutoDrawable drawable) {
		this.drawable = drawable;
		this.gl = drawable.getGL().getGL2();
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width,
			int height) {

		log.info("Reshape with " + x + "/" + y + " [" + width + "x" + height
				+ "]");

		if (height <= 0) {
			height = 1;
		}

		// store gl
		setCurrentContext(drawable);

		// inform listeners
		for (ICanvasListener l : listeners) {
			l.onResize(drawable, x, y, width, height);
		}
	}

	/**
	 * update the canvas
	 * 
	 * @param updateTime
	 */
	private void update() {

		Iterator<ICanvasListener> iter = listeners.iterator();
		while (iter.hasNext()) {
			ICanvasListener l = iter.next();
			l.onUpdate(updateTime, frameCount);
		}

	}

	/**
	 * render method
	 * 
	 * renders the internal render list
	 */
	private void render() {
		// inform listeners
		Iterator<ICanvasListener> iter = listeners.iterator();
		while (iter.hasNext()) {
			ICanvasListener l = iter.next();
			l.onRender(gl);
		}
	}
}
