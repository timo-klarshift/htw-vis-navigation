package org.htw.vis.clustering.feature;

import org.htw.vis.clustering.Metric;


public class SemanticFeature extends Feature  {
	private final byte[] vector;
	private final int size;
	private final Metric metric = Metric.get("euclidian");
	
	public SemanticFeature(byte[] vector){
		this.vector = vector;
		this.size = vector.length;
	}
	
	public SemanticFeature(int size, byte value){
		this.size = size;
		this.vector = new byte[size];
		for(int v=0; v<size; v++){
			this.vector[v] = value;
		}
	}
	
	public final byte[] getVector(){
		return vector;
	}

	@Override
	public double getDistance(Feature other) {		
		return metric.getDistance(vector, ((SemanticFeature)other).getVector());
	}
	
	public int getSize(){
		return size;
	}
	
	public void set(int i, byte v){
		vector[i] = v;
	}
	
	public byte get(int i){
		return vector[i];
	}
	
	public void set(byte[] v){
		for(int i=0; i<vector.length; i++)
			vector[i] = v[i];
	}

	@Override
	public void randomInit(double f) {
		for(int v=0; v<size; v++){
			vector[v] = (byte) ((float) (Math.random() * f * 250.0f-120));
		}		
	}

	@Override
	public final void adjust(Feature currentSample, float f) {
		byte[] b = ((ByteFeature)currentSample).getVector();
		for(int i=0; i<size; i++){
			vector[i] = (byte) (vector[i] + f*(b[i]-vector[i]));
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
