package org.htw.vis.clustering.feature;

import java.util.List;

/**
 * float feature factory
 * 
 * @author timo
 *
 */
public class SemanticFeatureFactory extends FeatureFactory {
	private final int size;
	
	public SemanticFeatureFactory(int size){
		this.size = size;
	}

	@Override
	public Feature generate() {
		return new SemanticFeature(size, (byte) 0);
	}

	

	@Override
	public Feature mean(List<Feature> features) {
		SemanticFeature meanFeature = (SemanticFeature) generate();
		double s = 1.0/features.size();
		for(int g=0; g<size; g++){
			double val = 0;
			for(Feature f : features){
				SemanticFeature ff = ((SemanticFeature)f);			
				val += ff.get(g);				
			}
			meanFeature.set(g, (byte) (s*val));			
		}			
		return meanFeature;
	}

	@Override
	public Feature clone(Feature rnd) {	
		SemanticFeature ff = (SemanticFeature)rnd;
		SemanticFeature clone = (SemanticFeature) generate();
		for(int i=0; i<size; i++){
			clone.set(i, ff.get(i));
		}
		return clone;
	}
}
