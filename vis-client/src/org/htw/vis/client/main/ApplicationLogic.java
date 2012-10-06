package org.htw.vis.client.main;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

import org.apache.log4j.Logger;
import org.htw.vis.client.controller.KeyboardApplicationController;
import org.htw.vis.client.controller.KinectGridController;
import org.htw.vis.client.grid.GridImage;
import org.htw.vis.client.grid.ImageGrid;
import org.htw.vis.client.kinect.KinectLogic;
import org.htw.vis.client.kinect.KinectServer;
import org.htw.vis.server.ClientListener;
import org.htw.vis.server.VISClient;
import org.htw.vis.server.context.ContainerImage;
import org.htw.vis.server.protocol.VISLoad;
import org.htw.vis.server.protocol.VISProto;
import org.htw.vis.server.protocol.VISUpdate;

import com.klarshift.kool.appearance.TextureManager;
import com.klarshift.kool.camera.Camera;
import com.klarshift.kool.canvas.ICanvasListener;
import com.klarshift.kool.canvas.RenderCanvas;
import com.klarshift.kool.render.ARenderPass;
import com.klarshift.kool.render.RenderEngine;
import com.klarshift.kool.render.pass.GUIPass;

public class ApplicationLogic implements ICanvasListener, ClientListener {
	private final Logger log = Logger.getLogger(this.getClass());
	private RenderCanvas canvas;
	private Application app;
	private Camera camera;
	private RenderEngine engine;
	private ImageGrid grid;

	private LinkedHashMap<Integer, ContainerImage> imagesToUpdate = new LinkedHashMap<Integer, ContainerImage>();
	private LinkedHashMap<Integer, GridImage> imagesToRemove = new LinkedHashMap<Integer, GridImage>();	

	private KeyboardApplicationController keyboard;
	private KinectGridController kinect;

	private boolean useKinect = false;

	private VISLoad nextLoad = new VISLoad();
	private VISUpdate nextUpdate;
	VISClient client;
	
	private int currentLayer = -1;
	private int shiftZ = 0;

	private boolean loading = false;
	private boolean sessionStarted = false;
	
	public int getCurrentLayer(){
		return currentLayer;
	}

	public ApplicationLogic() {
		app = Application.getInstance();
		camera = app.getCamera();
		engine = app.getEngine();
		grid = app.getGrid();

		client = app.getClient();

		canvas = app.getCanvas();
	}

	public void init() {
		client.addListener(this);
		canvas.addListener(this);
		initController();
	}

	public boolean isSessionStarted() {
		return sessionStarted;
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

		if (camera != null) {
			// set pick area
			camera.setPickArea(canvas.getWidth() / 2, canvas.getHeight() / 2);
		}
	}

	@Override
	public void onUpdate(long updateTime, long frameCount) {
		// update kinect
		if (kinect != null) {
			kinect.update(updateTime, frameCount);
		}

		// update keyboard
		if (keyboard != null) {
			keyboard.update(updateTime, frameCount);
		}

		// update gui (weak hack :( )
		GUIPass gp = (GUIPass) engine.getPass(ARenderPass.PASS_GUI);
		if (gp != null) {
			gp.update(updateTime, frameCount);
		}

		// load images
		if (frameCount % 100 == 0) {
			// reload the container
			reloadContainer();
		}

		// handle updates
		handleIncomingUpdate();
		
		// handle zoom
		handleZoomEvents();
	}
	
	private void handleZoomEvents(){
		double cd = grid.getCamDistance();
		double cMin = 3;
		double cMax = 20;
		if(cd != 0.0){
			if(cd < cMin){
				if(shiftZ == 0)
					shiftZ--;
				
				grid.t().move(0, 0, -10+cd);
			}else if(cd > cMax){
				if(shiftZ == 0)
					shiftZ++;
				
				grid.t().move(0, 0, +cd-10);
			}
		}
	}
	


	private void handleIncomingUpdate() {
		if (nextUpdate != null) {
			
			// shift
			int x = nextUpdate.shiftX;
			int y = nextUpdate.shiftY;
			if (x != 0 || y != 0) {
				log.info("Received Shift: " + x + "/" + y);
				grid.t().move(-x, -y, 0);

				for (GridImage i : grid.getImages()) {
					i.t().move(x, y, 0);
				}
			}
			
			currentLayer = nextUpdate.layer;
			
			shiftZ = 0;

			for (GridImage gi : imagesToRemove.values()) {
				grid.removeImage(gi.getFotoliaId());				
			}

			for (ContainerImage ci : imagesToUpdate.values()) {
				grid.updateImage(ci);
			}					
			

			// reset state
			nextUpdate = null;
			setLoading(false);
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

	public boolean isLoading() {
		return loading;
	}

	private void reloadContainer() {
		nextLoad.searchQuery = null;
		updateContainer();
	}

	public void search(String query, int maxResults) {
		nextLoad.searchQuery = query;
		nextLoad.maxResults = maxResults;
		updateContainer();
	}

	public void search(String query) {
		search(query, 100);
	}

	private void updateContainer() {
		if (sessionStarted == true && !loading) {
			setLoading(true);

			log.info("Reload Container ... ");

			nextLoad.focusX = grid.getFocusX();
			nextLoad.focusY = grid.getFocusY();
			nextLoad.shiftX = grid.getShiftX();
			nextLoad.shiftY = grid.getShiftY();
			nextLoad.shiftZ = shiftZ;

			nextLoad.maxResults = 200;

			System.out.println(nextLoad);

			client.write(VISProto.LOAD, nextLoad);

		}
	}

	private void setLoading(boolean loading) {
		this.loading = loading;
	}

	/**
	 * init the controller
	 */
	private void initController() {
		// keyboard controller
		keyboard = new KeyboardApplicationController();

		// kinect controller

		if (useKinect) {

			KinectServer server = new KinectServer();
			KinectLogic kinectLogic = new KinectLogic(server.getContext());

			// visualize skeleton
			// VisualKinect vk = new VisualKinect(server);
			// scene.add(vk);

			/*
			 * CalibrationShape cs = new CalibrationShape(); scene.add(cs);
			 * cs.t().setZ(8).scale(1.2f); cs.setVisible(false);
			 */

			// create controller
			kinect = new KinectGridController(server, grid, null);

			// start server
			server.start();
		}

	}

	public void setUseKinect(boolean useKinect) {
		this.useKinect = useKinect;
	}

	@Override
	public void onImagePreload(ContainerImage image) {
		try {
			log.info("Preloading: " + image);
			// preload the images texture
			TextureManager.getInstance().addTexture(
					image.getFotoliaId().toString(),
					new URL(image.getImageSource()), "jpg", 0);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onImagesUpdate(VISUpdate update) {
		// store update
		// this update object will be collected in next
		// render update
		// TODO: maybe do this here ?
		
		Set<Integer> removedDueShift = update.getRemovedImages();

		//Collection<ContainerImage> images = nextUpdate.getImages();

		imagesToUpdate.clear();
		imagesToRemove.clear();

		// 1st add all
		for (ContainerImage ci : update.getImages()) {
			// if (!grid.hasImage(ci.getFotoliaId())) {
			
			imagesToUpdate.put(ci.getFotoliaId(), ci);
			// }
		}

		// 2 loop grid
		for (GridImage gi : grid.getImages()) {
			if (imagesToUpdate.containsKey(gi.getFotoliaId())) {
				// keep			
				
				if(removedDueShift.contains(gi.getFotoliaId())){
					// removed due shifting but contained in new ersult
					gi.fadeIn();
				}else{
					// position keeps the same
				}
				
			} else {
				// clear, remove all
				imagesToRemove.put(gi.getFotoliaId(), gi);
			}
		}

		log.info("Update " + imagesToUpdate.size());		
		log.info("Remove " + imagesToRemove.size());

		nextUpdate = update;

	}

	@Override
	public void onSessionStarted() {
		sessionStarted = true;
		search("fresh fruit", 400);
	}
}
