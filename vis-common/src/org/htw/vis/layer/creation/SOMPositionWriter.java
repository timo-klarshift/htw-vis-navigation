package org.htw.vis.layer.creation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.htw.vis.clustering.TrainSet;
import org.htw.vis.clustering.feature.ByteFeatureFactory;
import org.htw.vis.clustering.feature.Feature;
import org.htw.vis.clustering.som.SOM;
import org.htw.vis.clustering.som.SOMNode;
import org.htw.vis.helper.FeatureAccess;
import org.htw.vis.layer.NetworkNode;
import org.htw.vis.layer.ZoomLayer;
import org.htw.vis.layer.ZoomWorld;
import org.htw.vis.setup.WorldCreator;

public class SOMPositionWriter  {	
	private static final Integer SOM_COUNT = 1024/16;

	private ZoomLayer layer;
	
	protected final Logger log = Logger.getLogger(this.getClass());
	
	private ZoomWorld world = ZoomWorld.create(5);
	private FeatureAccess features = new FeatureAccess();
	
	
	public SOMPositionWriter(){
		// get the first som-friendly layer
		ZoomLayer layer = world.getLastLayer();		
		
		// sort it
		sortLayer(layer);
	}
	
	private void sortLayer(ZoomLayer layer){			
		// create train set
		TrainSet t = new TrainSet();
		
		// get all nodes from layer
		// and add to train set
		ArrayList<NetworkNode> nodes = new ArrayList<NetworkNode>();		
		ResultSet rs;			
		try {
			rs = layer.queryNodes();
			while(rs.next()){
				NetworkNode node = new NetworkNode(rs);
				t.addSample(features.getFeature(node.getFotoliaId()));
				nodes.add(node);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		
		
		// train som
		int n = (int) Math.ceil(Math.sqrt(layer.nodeCount()));
		SOM som = SOM.create(n, n, new ByteFeatureFactory(60));
		som.train(t, 30);
					
		// write positions
		for(NetworkNode node : nodes){
			Feature sample = features.getFeature(node.getFotoliaId());
			SOMNode bmu = som.getFreePosition(sample);					
			if(bmu != null){				
				int x = bmu.getX();
				int y = bmu.getY();
				//layer.setPosition(node.getFotoliaId(), x, y);
				
				SimplePositionWriter.set(layer, node.getFotoliaId(), x, y);
			}else{
				log.error("NO BMU");
			}
		}
		
		
	}
	

	
	public static void main(String[] args) {
		// setup logger
		Logger.getRootLogger().setLevel(Level.INFO);
				
		try {
			new SOMPositionWriter()	;		
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
}
