package org.htw.vis.clustering.som.test

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D;

import javax.swing.JPanel;

import org.htw.vis.clustering.TrainSet
import org.htw.vis.clustering.feature.Feature;
import org.htw.vis.clustering.feature.FloatFeature;
import org.htw.vis.clustering.som.SOM;
import org.htw.vis.clustering.som.SOMListener;
import org.htw.vis.clustering.som.SOMNode

class SOMVis extends JPanel implements SOMListener {
	SOM som
	int stepx, stepy	
	TrainSet set
	
	public SOMVis(SOM som){
		this.som = som
		setPreferredSize(new Dimension(200, 200))
		som.addListener(this)
	}

	public void paint(Graphics g){
		Graphics2D g2 = (Graphics2D)g
		
		if(!som)return
		
		stepx = width / som.width
		stepy = height / som.height;

		g.setColor(Color.BLACK)
		g.fillRect(0, 0, width, height)

		// draw features (SOM itself)
		g2.setStroke(new BasicStroke(2))
		for(int y=0; y<som.height; y++){
			for(int x=0; x<som.width; x++){
				// fill rect
				SOMNode n = som.getNode(x, y)
				if(!n)break;
				float[] color = n.getWeight().vector
				g.setColor(new Color((float)(color[0]*0.4), (float)(color[1]*0.4), (float)(color[2]*0.4)))
				
				g.drawRect(x*stepx, y*stepy, stepx, stepy)
			}
		}

		if(set){
			drawSet(g, set)
		}
	}
	
	private void drawSet(Graphics g, TrainSet set){
		// draw samples
		if(set?.samples){
			Iterator<Feature> iter = set.getSamples().clone().iterator()
			while(iter.hasNext()){
				Feature f = iter.next();
				def bmu = som.getBMU(f)
				if(bmu){
					int x = bmu.x
					int y = bmu.y
					float[] color = ((FloatFeature) f).vector
					g.setColor(new Color(color[0], color[1], color[2]))
					g.fillOval(x*stepx+2, y*stepy+2, stepx-4, stepy-4)
				}
			}
		}
	}

	@Override
	public void onIterationFinished(TrainSet set) {
		
	}


	@Override
	public void onTrainingFinished(TrainSet set) {
		
	}

	@Override
	public void onMappingFinished(TrainSet set) {
		
	}
}
