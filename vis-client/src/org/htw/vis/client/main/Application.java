package org.htw.vis.client.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.htw.vis.client.grid.ImageGrid;
import org.htw.vis.server.VISClient;
import org.htw.vis.server.protocol.VISLoad;

import com.klarshift.kool.camera.Camera;
import com.klarshift.kool.camera.PerspectiveCamera;
import com.klarshift.kool.canvas.CanvasFrame;
import com.klarshift.kool.canvas.RenderCanvas;
import com.klarshift.kool.gui.EngineStats;
import com.klarshift.kool.primitive.Triangle;
import com.klarshift.kool.render.ARenderPass;
import com.klarshift.kool.render.RenderEngine;
import com.klarshift.kool.render.pass.ColorPass;
import com.klarshift.kool.render.pass.GUIPass;
import com.klarshift.kool.scenegraph.KNode;

/**
 * navigation application
 * 
 * @author timo
 * 
 */
public class Application {
	/* logging */
	final static Logger log = Logger.getLogger("App");

	/* settings */
	public static final Level LOG_LEVEL = Level.INFO;
	private int containerWidth = 60;
	private int containerHeight = 35;
	private boolean useKinect = false;
	private boolean walkerEnabled = false;
	private String serverHost = "localhost";

	/* application properties */
	private static Application instance;

	/* canvas and engine */
	RenderEngine engine;
	RenderCanvas canvas;

	VISLoad nextLoad = new VISLoad();

	/* the scene */
	KNode scene = new KNode("Vis3D Scene");
	Camera camera;
	ImageGrid grid;

	VISClient client;

	//
	ApplicationLogic logic;

	public Triangle triangle = new Triangle(1, 1);
	private CanvasFrame canvasFrame;


	/**
	 * get the singleton instance of the application
	 * 
	 * @return
	 */
	public static Application getInstance() {
		if (instance == null) {
			// set logging
			Logger.getRootLogger().setLevel(LOG_LEVEL);

			instance = new Application();
			log.info("Application Singleton created.");
		}

		return instance;
	}

	public ApplicationLogic getLogic() {
		return logic;
	}

	/**
	 * private singleton constructor
	 */
	private Application() {
	}

	/**
	 * get the vis client
	 * 
	 * @return
	 */
	public VISClient getClient() {
		return client;
	}

	/**
	 * get the canvas
	 * 
	 * @return
	 */
	public RenderCanvas getCanvas() {
		return canvas;
	}

	/**
	 * get the grid
	 * 
	 * @return
	 */
	public ImageGrid getGrid() {
		return grid;
	}

	private void readConfig() {
		log.info("Reading config ...");
		Properties props = new Properties();
		InputStream pStream = getClass().getClassLoader().getResourceAsStream(
				"org/htw/vis/client/config/application.properties");
		try {
			props.load(pStream);

			useKinect = Boolean.parseBoolean(props.getProperty(
					"kinect.enabled", "false"));
			walkerEnabled = Boolean.parseBoolean(props.getProperty(
					"walker.enabled", "false"));
			containerWidth = Integer.parseInt(props.getProperty("container.width",
					"50"));
			containerHeight = Integer.parseInt(props.getProperty(
					"container.height", "30"));

			serverHost = props.getProperty("server.host", "localhost");

			pStream.close();

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		log.info("Config read.");
	}

	/**
	 * init the application
	 */
	public void init() {
		log.info("Init application ...");

		// read config
		readConfig();

		// init canvas (+engine +camera)
		initCanvas();
		initEngine();
		initCamera();

		// init the scene
		initScene();

		

		// create client
		client = new VISClient();

		

		// start walker
		if (walkerEnabled)
			new RandomWalker().walk();

		log.info("Init application done.");
		
		// logic
		logic = new ApplicationLogic();
		logic.setUseKinect(useKinect);
		logic.init();

		// start animation
		canvas.start();
		
		// start a session
		startSession();
	}

	/**
	 * get the application camera
	 * 
	 * @return
	 */
	public Camera getCamera() {
		return camera;
	}

	/**
	 * init the grid
	 */
	private void initScene() {
		// create the grid
		grid = new ImageGrid(containerWidth, containerHeight);
		grid.t().move(0, 0, -10);

		// add the grid
		scene.add(grid);
		scene.add(triangle);

		// stats
		GUIPass gui = (GUIPass) engine.getPass(ARenderPass.PASS_GUI);
		if (gui != null) {
			EngineStats stats = new EngineStats();
			stats.t().setPosition(3, 20);
			gui.add(stats);
		}
	}

	public void shutdown() {
		log.info("Shutting down ...");
		client.shutdown();
		System.exit(0);
	}

	/**
	 * start the session
	 */
	public void startSession() {
		log.info("Starting session ...");
		try {
			client.start(containerWidth, containerHeight);
		} catch (ConnectException e) {
			log.error("Cannot reach server. Started?");
			shutdown();
		} catch (Exception e) {
			e.printStackTrace();
			shutdown();
		}
	}	

	/**
	 * init the canvas
	 */
	private void initCanvas() {

		// create canvas
		canvas = RenderCanvas.create();
		canvas.requestFocus();

		// wrap in frame
		canvasFrame = new CanvasFrame("3D VIS", canvas, 800, 400);

	}

	/**
	 * get the scene
	 * 
	 * @return
	 */
	public KNode getScene() {
		return scene;
	}

	/**
	 * init the camera
	 */
	private void initCamera() {
		// create a perspective camera
		camera = new PerspectiveCamera();
		//camera = new Camera3D();

		// set pick area
		camera.setPickArea(canvas.getWidth() / 2, canvas.getHeight() / 2);

		camera.t().move(0, 0, 0); // put in origin

		// set camera to engine
		engine.setCamera(camera);

		// add camera to scene
		scene.add(camera);
	}

	/**
	 * init the engine
	 */
	private void initEngine() {
		// create
		engine = RenderEngine.create(scene, canvas);
		engine.addPass(new ColorPass());
		engine.addPass(new GUIPass());
	}

	/**
	 * get the gui root node
	 * 
	 * @return
	 */
	public KNode getGui() {
		return ((GUIPass) engine.getPass(ARenderPass.PASS_GUI)).getRoot();
	}

	/**
	 * app entry point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Application.getInstance().init();
	}

	public RenderEngine getEngine() {
		return engine;		
	}

}
