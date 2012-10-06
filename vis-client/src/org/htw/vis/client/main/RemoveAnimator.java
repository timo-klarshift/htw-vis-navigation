package org.htw.vis.client.main;

import org.htw.vis.client.grid.GridImage;
import org.htw.vis.client.grid.ImageGrid;

import com.klarshift.kool.animation.Animator;
import com.klarshift.kool.appearance.Appearance;

public class RemoveAnimator extends Animator{
	GridImage image;
	ImageGrid grid;
	Appearance appearance;
	
	public RemoveAnimator(GridImage image, ImageGrid grid){
		this.grid = grid;
		this.image = image;
		this.appearance = image.getAppearance();
		
		setDuration((long) (500 + Math.random()*2000));
	}
	
	@Override
	public void start() {	
		super.start();
		appearance.setTransparency(0);
	}

	@Override
	public void tick(double alpha) {
		float t = appearance.getTransparency();
		t = (float) ( (alpha * alpha));
		
		if(t>(1-0.01)){			
			appearance.setTransparency(1);
			grid.remove(image);
			stop();
			return;
		}
		
		appearance.setTransparency(t);
	}

}
