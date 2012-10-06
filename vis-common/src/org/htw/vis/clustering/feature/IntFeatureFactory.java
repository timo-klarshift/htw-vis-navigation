package org.htw.vis.clustering.feature;

import java.util.List;

/**
 * float feature factory
 * 
 * @author timo
 *
 */
public class IntFeatureFactory extends FeatureFactory {
	private final int size;
	
	public IntFeatureFactory(int size){
		this.size = size;
	}

	@Override
	public Feature generate() {
		return new IntFeature(size, 0);
	}

	

	@Override
	public Feature mean(List<Feature> features) {
		IntFeature meanFeature = (IntFeature) generate();
		double s = 1/features.size();
		for(int g=0; g<size; g++){
			double val = 0;
			for(Feature f : features){
				IntFeature ff = ((IntFeature)f);			
				val += ff.get(g);				
			}
			meanFeature.set(g, (int)(Math.round(s*val)));			
		}			
		return meanFeature;
	}
	
	@Override
	public Feature clone(Feature rnd) {	
		IntFeature ff = (IntFeature)rnd;
		IntFeature clone = (IntFeature) generate();
		for(int i=0; i<size; i++){
			clone.set(i, ff.get(i));
		}
		return clone;
	}
}
