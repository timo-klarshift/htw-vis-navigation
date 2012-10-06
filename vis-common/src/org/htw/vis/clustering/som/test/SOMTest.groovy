package org.htw.vis.clustering.som.test

import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.htw.vis.clustering.TrainSet
import org.htw.vis.clustering.feature.FloatFeature;
import org.htw.vis.clustering.feature.FloatFeatureFactory;
import org.htw.vis.clustering.som.SOM
import org.htw.vis.gui.FrameWrapper




class SOMTest {

	static main(args) {
		Logger.getRootLogger().setLevel(Level.INFO)
		
		def ff = new FloatFeatureFactory(3)
		int w = 25; int h=15;
		int size = w*h
		SOM som = SOM.create(w, h, ff)			
							
		
		// training set		
		def set = new TrainSet()
		
		// view
		def view = new SOMVis(som)
		view.set = set
		new FrameWrapper('SOM', view)			
		
		def add = { TrainSet s, n ->
			n.times{
				FloatFeature f = ff.generate()
				f.randomInit()
				//f.set(0, (set.samples.size / size))
				s.addSample(f)
				set.addSample(f)
			}
		}
		
		TrainSet rset = new TrainSet();
		add(rset, 100)
		
		// train som
		som.train(rset, 12, w, 0.1)
		//som.map(rset)
			
		int radius;
		while(set.samples.size() < (size*0.9)){
			rset = new TrainSet()
			add(rset, 30)
			som.train(rset, 5, (int)(w/2), 0.1f)
		}
		
		while(true){
			set.samples.each{
				def bmu = som.getFreePosition(it)
				if(bmu)
					som.lock(bmu)
			}
			
			
			som.shift(3, 1)
			//som.map(set)
			//som.train(set, 12, w, 0.1)
			view.repaint()
			som.train(set, 5, 12, 0.3)
			Thread.sleep 200
		}
				
		//som.map(set)
		view.repaint()
	}
}
