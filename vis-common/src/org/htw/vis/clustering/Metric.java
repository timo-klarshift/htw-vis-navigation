package org.htw.vis.clustering;

import java.util.HashMap;

public abstract class Metric {	
	static final HashMap<String,Metric> metrics = new HashMap<String,Metric>();
	
	public abstract double getDistance(float[] a, float[] b);
	public abstract double getDistance(double [] a, double[] b);
	public abstract double getDistance(int[] a, int[] b);
	public abstract double getDistance(byte[] a, byte[] b);
	
	public static final Metric get(String name){
		Metric m = metrics.get(name);
		if(m == null){
			System.out.println("GET Metric : " + name);
			if(name.equals("euclidian")){
				m = new EuclidianMetric();				
			}else if(name.equals("manhatten")){
				m = new ManhattenMetric();
			}
			
			if(m != null)
				metrics.put(name, m);
		}
		
		return m;
	}
}
