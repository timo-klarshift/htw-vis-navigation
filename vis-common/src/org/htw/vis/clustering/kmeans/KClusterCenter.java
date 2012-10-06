package org.htw.vis.clustering.kmeans;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import org.htw.vis.clustering.feature.Feature;

public class KClusterCenter implements Comparable<KClusterCenter> {
	String id = UUID.randomUUID().toString();
	Feature feature;
	HashMap<Integer,KData> data = new HashMap<Integer,KData>();
	
	public String getId(){
		return id;
	}
	
	public KClusterCenter(Feature feature){
		this.feature = feature;
	}
	
	public void add(Integer id, KData d){
		data.put(id, d);
	}
	
	public void remove(Integer id){
		data.remove(id);
	}

	public Feature getFeature() {
		return feature;
	}
	
	public Collection<KData> getData(){
		return data.values();
	}

	public void setFeature(Feature mean) {
		this.feature = mean;
	}

	public int getDataCount() {
		return data.size();
	}
	
	public double getDistance(KData data) {				
		Feature b = data.getFeature();
		return feature.getDistance(b);
	}

	@Override
	public int compareTo(KClusterCenter o) {
		int oc = o.getDataCount();
		return oc > data.size() ? 1 : (oc < data.size() ? -1 : 0); 
	}

}
