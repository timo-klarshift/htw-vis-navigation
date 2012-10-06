package org.htw.vis.clustering.feature;

import org.htw.vis.clustering.Metric;


public class IntFeature extends Feature  {
	private final int[] vector;
	private final int size;
	private final Metric metric;
	
	public IntFeature(int[] vector){
		this.vector = vector;
		this.size = vector.length;
		
		this.metric = Metric.get("euclidian");
	}
	
	public IntFeature(int size, int value){
		this.size = size;
		this.vector = new int[size];
		for(int v=0; v<size; v++){
			this.vector[v] = value;
		}
		
		this.metric = Metric.get("euclidian");
	}
	
	public final int[] getVector(){
		return vector;
	}

	@Override
	public double getDistance(Feature other) {		
		return metric.getDistance(vector, ((IntFeature)other).getVector());
	}
	
	public int getSize(){
		return size;
	}
	
	public void set(int i, int v){
		vector[i] = v;
	}
	
	public int get(int i){
		return vector[i];
	}
	
	public void set(int[] v){
		for(int i=0; i<vector.length; i++)
			vector[i] = v[i];
	}

	@Override
	public void randomInit(double f) {
		for(int v=0; v<size; v++){
			vector[v] = (int) Math.round (Math.random() * f * Integer.MAX_VALUE);
		}		
	}

	@Override
	public final void adjust(Feature currentSample, float f) {
		int[] b = ((IntFeature)currentSample).getVector();
		for(int i=0; i<size; i++){
			vector[i] = Math.round (vector[i] + f*(b[i]-vector[i]));
		}
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for(int v=0; v<vector.length; v++){
			sb.append(vector[v]);
			if(v<vector.length - 1)sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}
}
