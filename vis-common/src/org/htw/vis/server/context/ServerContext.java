package org.htw.vis.server.context;

import java.util.logging.Logger;

import org.htw.vis.layer.ZoomLayer;
import org.htw.vis.layer.ZoomWorld;
import org.htw.vis.setup.WorldCreator;

public class ServerContext {
	private final Logger log = Logger.getLogger("ServerCtx");
	
	String sessionId;
	ImageContainer container;
	
	int x = -1;
	int y = -1;
	
	int layerId = 1;
	
	public void setFocus(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public String getSessionId(){
		return sessionId;
	}
	
	public ServerContext(String sessionId){
		this.sessionId = sessionId;
	}
	
	public void createContainer(int width, int height){
		log.info("Creating container " + width + "x" + height);
		container = new ImageContainer(this, width, height);
		log.info("Done.");
	}	
	
	public ZoomLayer getLayer(){		
		return ZoomWorld.get().getLayer(layerId);
	}

	public ImageContainer getContainer() {
		return container;
	}

	public void zoomIn() {
		if(layerId > 0)
			layerId--;
		
		log.info("Layer ID: " + layerId);
	}

	public void zoomOut() {
		if(layerId < ZoomWorld.get().getLayerCount()-1)
			layerId++;
		
		log.info("Layer ID: " + layerId);
	}
}
