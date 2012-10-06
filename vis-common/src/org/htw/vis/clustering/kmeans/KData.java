package org.htw.vis.clustering.kmeans;

import org.htw.vis.clustering.feature.Feature;

public class KData {
	public static final int UNCLASSIFIED = -100;
	public static final int NOISE = -200;
	
	
	Feature feature;
	int clusterId = -1;
	
	public KData(Feature f){
		this.feature = f;
	}
	
	public void setClusterId(int id){
		this.clusterId = id;
	}
	
	public Feature getFeature(){
		return feature;
	}

	public int getClusterId() {
		return clusterId;
	}
	
	public double getDistance(KClusterCenter c) {				
		Feature b = c.getFeature();
		return feature.getDistance(b);
	}
	
	public void reset(){
		clusterId = -1;
	}
}
