package org.htw.vis.setup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.htw.vis.helper.FeatureAccess;

public class Pairer {
	private static final double NORM = 1.0/2200.0;
	double[][] dMatrix;
	ArrayList<Integer> set;
	int size;
	
	Set<Integer> processed = new HashSet<Integer>();
	Set<Integer> remaining = new HashSet<Integer>();
	Set<Quad> quads = new HashSet<Quad>();
	
	static FeatureAccess features = new FeatureAccess();
	
	
	public Pairer(ArrayList<Integer> set){
		this.set = set;		
		this.size = set.size();
	}
	
	public void pair(){
		if(size < 4){
			remaining.addAll(set);
			return;
		}
		
		// unpaired set
		HashSet<Integer> unpaired = new HashSet<Integer>();
		
		// init matrix
		init();
		
		for(Integer c : set){
			remaining.add(c);
		}
						
		// do pairing		
		while(remaining.size() > 3){
			
			Set<Integer> currentSet = new HashSet<Integer>();
			
			Integer c1, c2;
			
			
			
			double dT = 0;
			int y = -1, x = -1;
			while(currentSet.size() < 4 && remaining.size() > 1) {
				double minD = Double.MAX_VALUE;
				Integer b1 = null, b2 = null;
				
				
				
				for(int j=0; j<size; j++){
					for(int i=j+1; i<size; i++){
						c1 = set.get(i);
						c2 = set.get(j);
						if(remaining.contains(c1) && remaining.contains(c2)){
							double d = dMatrix[j][i];
							if(currentSet.size() == 2){
								d = d + (dMatrix[j][x] + dMatrix[y][i]); 
								d /= 3;
							}
													
							if(d < minD ){
								minD = d;
								
								
								b1 = c1;
								b2 = c2;
								
								if(currentSet.size() == 0){
									y = j;
									x = i;
								}
							}
						}
					}
				}
				
				
				remaining.remove(b1);
				remaining.remove(b2);

				if(minD > FinalPairing.MAX_VIS_DISTANCE){
					unpaired.add(b1);
					unpaired.add(b2);
				}else{
				
					dT += minD;
				
					currentSet.add(b1);
					currentSet.add(b2);
				}
				
				
			}
			
			if(currentSet.size() == 4){
				// do something with set
				processQuad(currentSet, dT);
				
				// iteration
				processed.addAll(currentSet);		
			}else{
				remaining.addAll(currentSet);
			}
			
			
		}
		
		remaining.addAll(unpaired);	
		
		//System.out.println("Found " + quads.size() + " Quads / " + remaining.size() + " remaining ...");
	}
	
	private void processQuad(Set<Integer> currentSet, double distance){
		Quad q = new Quad(currentSet, distance);		
		quads.add(q);
	}
	
	private void init(){
		int s = set.size();
		dMatrix = new double[s][s];
		
		for(int j=0; j<s; j++){
			for(int i=0; i<s; i++){
				dMatrix[j][i] = calculateDistance(j, i);
			}
		}
	}
	
	private double calculateDistance(int a, int b){
		double dVis = features.getDistance(set.get(a), set.get(b))*NORM;
		return dVis;
		/*double dSem = Math.random()*0.9;
		double alfa = 0.99;
		return ((1-alfa)*dSem + alfa * dVis)/2;*/
		//return Math.sqrt((a-b)*(a-b));
		//return Math.random();
	}
	
	public class Quad {
		private ArrayList<Integer> items = new ArrayList<Integer>();
		private double distance = 0;
		public Quad(Set<Integer> items, double distance){
			this.items.addAll(items);
			this.distance = distance;
		}
		
		public List<Integer> getItems(){
			return items;
		}
		
		public double getDistance(){
			return distance;
		}
	}

	public Set<Quad> getQuads() {
		return quads;
	}

	public Set<Integer> getRemaining() {
		return remaining;		
	}
}
