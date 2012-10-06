package org.htw.vis.clustering.som;

import org.htw.vis.clustering.TrainSet;

/**
 * som listener
 * 
 * @author timo
 *
 */
public interface SOMListener {
	public void onIterationFinished(TrainSet set);
	public void onTrainingFinished(TrainSet set);
	public void onMappingFinished(TrainSet set);
}
