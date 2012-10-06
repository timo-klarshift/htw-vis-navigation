package org.htw.vis.clustering.feature;

import java.util.List;

/**
 * byte feature factory
 * @author timo
 *
 */
public class ByteFeatureFactory extends FeatureFactory {
	private final int size;
	
	public ByteFeatureFactory(int size){
		this.size = size;
	}

	@Override
	public Feature generate() {
		return new ByteFeature(size, (byte)0);
	}
	
	@Override
	public Feature mean(List<Feature> features) {
		ByteFeature meanFeature = (ByteFeature) generate();
		double s = 1.0/features.size();
		for(int g=0; g<size; g++){
			double val = 0;
			for(Feature f : features){
				ByteFeature ff = ((ByteFeature)f);		
				val += ff.get(g);				
			}
			meanFeature.set(g, (byte)(s*val));			
		}			
		return meanFeature;
	}
	
	@Override
	public Feature clone(Feature rnd) {	
		ByteFeature ff = (ByteFeature)rnd;
		ByteFeature clone = (ByteFeature) generate();
		for(int i=0; i<size; i++){
			clone.set(i, ff.get(i));
		}
		return clone;
	}
}
