package org.htw.vis.setup

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.htw.vis.layer.ZoomWorld

class QueryCopy {
	static main(args){
		Logger.getRootLogger().setLevel(Level.INFO);
		//Database.register(Database.LAYERS, EnvDbConfig.get().layerConfig());
		ZoomWorld.create(1)
		ZoomWorld.get().getIndex(0)
		
		// create indexer and index all images of selected layer
		//NodeIndexer indexer = new NodeIndexer(0, 4);
		//indexer.indexAll();
		
		def layer = ZoomWorld.get().getLayer(0)
		layer.searcher().search("plant", 100000, 0.1)
		
		
	}
}
