package org.htw.vis.clustering;

public class ManhattenMetric extends Metric {

	
	@Override
	public double getDistance(float[] a, float[] b) {
		double distance = 0;
		double d;
		
		for(int c=0; c<a.length; c++){
			d = Math.abs(a[c]-b[c]);
			distance += d; 
		}
		
		return distance;
	}
	
	@Override
	public double getDistance(int[] a, int[] b) {
		double distance = 0;
		double d;
		
		for(int c=0; c<a.length; c++){
			d = Math.abs(a[c]-b[c]);
			distance += d; 
		}
		
		return distance;
	}

	@Override
	public double getDistance(byte[] a, byte[] b) {
		double distance = 0;
		double d;
		int ia, ib; 		
		
		for(int c=0; c<a.length; c++){
			ia =a[c];
			ib =b[c];
			d = Math.abs(ia-ib);
			distance += d; 
		}
		
		return distance;
	}

	@Override
	public double getDistance(double[] a, double[] b) {
		double distance = 0;
		double d;
		
		for(int c=0; c<a.length; c++){
			d = Math.abs(a[c]-b[c]);
			distance += d; 
		}
		
		return distance;
	}
}
