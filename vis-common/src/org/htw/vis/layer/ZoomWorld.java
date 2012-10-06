package org.htw.vis.layer;

import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.htw.vis.config.EnvDbConfig;
import org.htw.vis.db.Database;
import org.htw.vis.lucene.LuceneIndex;

/**
 * zoom world
 * 
 * world abstraction containing layers
 * 
 * @author timo
 * 
 */
public class ZoomWorld {
	private static final Logger log = Logger.getLogger("ZoomWorld");
	private final ArrayList<ZoomLayer> layers = new ArrayList<ZoomLayer>();
	
	static final int LAYER_BASE = 0;	

	private static ZoomWorld instance;
	
	/**
	 * create a zoomWorld with given size
	 */
	private ZoomWorld(int size) {
		String hostname = EnvDbConfig.getHostname();
		boolean isLocal = hostname.equals("viscomp1") == false;
		
		// create layers
		for (int w = 0; w <= size; w++) {
			// register index
			if(isLocal)
				LuceneIndex.register("layer-" + w, "/home/timo/vis-Workspace/vis-common/index/layer-" + w);
			else
				LuceneIndex.register("layer-" + w, "/home/timo/vis/index/layer-" + w);
					
			// add layer
			layers.add(new ZoomLayer(w));					
		}
	}

	/**
	 * create the world
	 * 
	 * @param size
	 * @return
	 */
	public static ZoomWorld create(int size) {
		if (instance != null) {
			log.error("Already created.");
			return null;
		}
		
		// register databases
		Database.register(Database.LAYERS, EnvDbConfig.get().layerConfig());
		Database.register(Database.FEATURES, EnvDbConfig.get().featureConfig());			
						
		// create instance
		instance = new ZoomWorld(size);
		
		return instance;
	}
	
	/**
	 * get index by its id
	 * @param id
	 * @return
	 */
	public LuceneIndex getIndex(int id){
		return LuceneIndex.get("layer-" + id);
	}
	
	/**
	 * get base index
	 * @return
	 */
	public LuceneIndex getBaseIndex(){
		return getIndex(0);
	}	
	
	/**
	 * get base layer
	 * @return
	 */
	public ZoomLayer getBaseLayer(){
		return getLayer(0);
	}
	
	public ZoomLayer getFirstLayer(){
		return getLayer(1);
	}
	
	public LuceneIndex getFirstIndex(){
		return getIndex(1);
	}
		
	/**
	 * get the zoom world
	 * 
	 * @return
	 */
	public static ZoomWorld get() {
		return instance;
	}
	
	/**
	 * world create flag
	 * @return
	 */
	public static boolean exists(){
		return instance != null;
	}
		
	/**
	 * get layer count
	 * @return
	 */
	public int getLayerCount() {
		return layers.size();
	}

	/**
	 * get layer
	 * @param i
	 * @return
	 */
	public ZoomLayer getLayer(int i) {
		if(i < 0 || i > layers.size()-1)return null;
		return layers.get(i);
	}

	/**
	 * shutdown the world
	 * @throws SQLException
	 */
	public void shutdown() throws SQLException {
		for(ZoomLayer l : layers){
			l.shutdown();
		}
		
		log.debug("ZoomWorld shutdown.");
	}

	public ZoomLayer getLastLayer() {
		return getLayer(layers.size()-1);
	}
}
