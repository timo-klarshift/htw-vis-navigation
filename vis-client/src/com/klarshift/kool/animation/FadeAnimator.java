package com.klarshift.kool.animation;

import com.klarshift.kool.appearance.Appearance;

public class FadeAnimator extends Animator {
	Appearance appearance;
	
	public FadeAnimator(Appearance appearance){
		this.appearance = appearance;
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		super.start();
		appearance.setTransparency(1);
	}

	@Override
	public void tick(double alpha) {
		float t = appearance.getTransparency();
		t = (float) (1 - (alpha * alpha));
		
		if(t<0.01){			
			appearance.setTransparency(0);
			stop();
			return;
		}
		
		appearance.setTransparency(t);
	}
	
	
}
