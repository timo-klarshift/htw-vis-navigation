package org.htw.vis.clustering.feature;

import java.util.List;

/**
 * float feature factory
 * 
 * @author timo
 *
 */
public class FloatFeatureFactory extends FeatureFactory {
	private final int size;
	
	public FloatFeatureFactory(int size){
		this.size = size;
	}

	@Override
	public Feature generate() {
		return new FloatFeature(size, 0);
	}

	

	@Override
	public Feature mean(List<Feature> features) {
		FloatFeature meanFeature = (FloatFeature) generate();
		double s = 1/features.size();
		for(int g=0; g<features.size(); g++){
			double val = 0;
			for(Feature f : features){
				FloatFeature ff = ((FloatFeature)f);			
				val += ff.get(g);				
			}
			meanFeature.set(g, (float)(s*val));			
		}			
		return meanFeature;
	}

	@Override
	public Feature clone(Feature rnd) {	
		FloatFeature ff = (FloatFeature)rnd;
		FloatFeature clone = (FloatFeature) generate();
		for(int i=0; i<size; i++){
			clone.set(i, ff.get(i));
		}
		return clone;
	}
}
