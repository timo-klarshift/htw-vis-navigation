package org.htw.vis.clustering.feature;

import java.util.List;

/**
 * feature factory
 * 
 * @author timo
 *
 */
public abstract class FeatureFactory {
	public abstract Feature generate();
	public abstract Feature mean(List<Feature> features);
	public abstract Feature clone(Feature rnd);
}
