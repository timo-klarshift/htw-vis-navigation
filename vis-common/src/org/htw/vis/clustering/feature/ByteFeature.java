package org.htw.vis.clustering.feature;

import java.io.Serializable;
import java.util.Random;

import org.htw.vis.clustering.Metric;

/**
 * byte feature
 * 
 * @author timo
 *
 */
public class ByteFeature extends Feature implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8155497662646052824L;
	private final byte[] vector;
	private final int size;
	private Metric metric = Metric.get("manhatten");
	
	/**
	 * create byte feature
	 * @param vector
	 */
	public ByteFeature(byte[] vector){
		this.vector = vector;
		this.size = vector.length;
	}
	
	/**
	 * create byte feature
	 * @param size
	 * @param value
	 */
	public ByteFeature(int size, byte value){
		this.size = size;
		this.vector = new byte[size];
		for(int v=0; v<size; v++){
			this.vector[v] = value;
		}
	}
	
	/**
	 * get the vector
	 * @return
	 */
	public byte[] getVector(){
		return vector;
	}

	@Override
	public double getDistance(Feature other) {		
		return metric.getDistance(vector, ((ByteFeature)other).getVector());
	}
	
	public int getSize(){
		return size;
	}

	@Override
	public void randomInit(double f) {
		Random r = new Random();
		r.nextBytes(vector);
		for(int v=0; v<size; v++){
			vector[v] = (byte) (vector[v]*f);
		}		
	}

	@Override
	public void adjust(Feature currentSample, float f) {
		byte[] b = ((ByteFeature)currentSample).getVector();
		for(int i=0; i<size; i++){
			vector[i] = (byte) (vector[i] + f*(b[i]-vector[i]));
		}
	}

	public void set(int g, byte b) {
		vector[g] = b;		
	}

	public byte get(int g) {
		return vector[g];
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
