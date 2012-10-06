package org.htw.vis.clustering;

import java.util.ArrayList;

import org.htw.vis.clustering.feature.Feature;

/**
 * train set
 * @author timo
 *
 */
public class TrainSet {
	/* samples holder */
	private ArrayList<Feature> samples = new ArrayList<Feature>();

	/**
	 * create a new train set
	 */
	public TrainSet(){ }

	/**
	 * add sample to train set
	 * @param f
	 */
	public void addSample(Feature f){
		samples.add(f);
	}

	/**
	 * pick a random feature
	 * @return
	 */
	public Feature randomPick(){
		int i = (int)(Math.random() * (samples.size()-1));
		return samples.get(i);
	}
	
	/**
	 * clear train set
	 */
	public void clear(){
		samples.clear();
	}

	/**
	 * get all samples
	 * @return
	 */
	public ArrayList<Feature> getSamples(){
		return samples;
	}
	
	public int size(){
		return samples.size();
	}

	public Feature get(int i) {
		return samples.get(i);
	}
	
	public boolean removeSample(Feature f){
		return samples.remove(f);
	}
}
