package org.htw.vis.clustering.feature;

import org.htw.vis.clustering.Metric;


public class FloatFeature extends Feature  {
	private final float[] vector;
	private final int size;
	private final Metric metric;
	
	public FloatFeature(float[] vector){
		this.vector = vector;
		this.size = vector.length;
		
		this.metric = Metric.get("euclidian");
	}
	
	public FloatFeature(int size, float value){
		this.size = size;
		this.vector = new float[size];
		for(int v=0; v<size; v++){
			this.vector[v] = value;
		}
		
		this.metric = Metric.get("euclidian");
	}
	
	public final float[] getVector(){
		return vector;
	}

	@Override
	public double getDistance(Feature other) {		
		return metric.getDistance(vector, ((FloatFeature)other).getVector());
	}
	
	public int getSize(){
		return size;
	}
	
	public void set(int i, float v){
		vector[i] = v;
	}
	
	public float get(int i){
		return vector[i];
	}
	
	public void set(float[] v){
		for(int i=0; i<vector.length; i++)
			vector[i] = v[i];
	}

	@Override
	public void randomInit(double f) {
		for(int v=0; v<size; v++){
			vector[v] = (float) (Math.random() * f);
		}		
	}

	@Override
	public final void adjust(Feature currentSample, float f) {
		float[] b = ((FloatFeature)currentSample).getVector();
		for(int i=0; i<size; i++){
			vector[i] = vector[i] + f*(b[i]-vector[i]);
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
