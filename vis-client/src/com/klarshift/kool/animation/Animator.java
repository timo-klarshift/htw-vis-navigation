package com.klarshift.kool.animation;

import com.klarshift.kool.canvas.CanvasListenerAdapter;
import com.klarshift.kool.render.RenderEngine;

/**
 * abstract animator providing an alpha timestamp on time-line
 * 
 * @author timo
 * 
 */
public abstract class Animator {
	public static final int INFINITE = -1;
	
	int loop = 0;
	AnimatorListener listener;
	boolean animating = false;

	long duration = 1000;
	long timestamp = 0;

	/**
	 * create the animator
	 */
	public Animator() {

	}

	/**
	 * set the animation duration in milliseconds
	 * 
	 * @param duration
	 * @return
	 */
	public Animator setDuration(long duration) {
		this.duration = duration;
		return this;
	}

	/**
	 * stop the animator
	 * 
	 * @return
	 */
	public Animator stop() {
		animating = false;
		timestamp = 0;
		RenderEngine.get().getCanvas().removeListener(listener);
		return this;
	}

	/**
	 * is the animator animating
	 * 
	 * @return
	 */
	public boolean isAnimating() {
		return animating;
	}

	/**
	 * set loop TODO: implement :)
	 * 
	 * @param loop
	 */
	public void setLoop(int loop) {
		this.loop = loop;
	}

	/**
	 * start the animator
	 */
	public void start() {
		timestamp = 0;
		animating = true;
		listener = new AnimatorListener();
		RenderEngine.get().getCanvas().addListener(listener);
	}

	/**
	 * on ticking
	 * 
	 * @param updateTime
	 * @param f
	 */
	private void onTick(long updateTime, long f) {
		// move in time
		timestamp += updateTime;

		// cap
		if (timestamp > duration) {
			timestamp = duration;
		}

		// fire
		fireTick(timestamp);

		// stop
		if (timestamp == duration) {
			if(loop > 0){
				loop--;
				reset();
			}else if(loop == 0){
				stop();
			}else{
				reset();
			}			
		}
	}
	
	public void reset(){
		timestamp = 0;
	}

	/**
	 * fire the tick
	 * 
	 * @param timestamp
	 */
	private void fireTick(long timestamp) {
		tick(timestamp / (double) duration);
	}

	/**
	 * tick method
	 * 
	 * @param alpha
	 */
	public abstract void tick(double alpha);

	/**
	 * animator listener
	 * 
	 * @author timo
	 * 
	 */
	private class AnimatorListener extends CanvasListenerAdapter {

		@Override
		public void onStop() {
			stop();
		}

		@Override
		public void onUpdate(long updateTime, long frameCount) {
			if (animating)
				onTick(updateTime, frameCount);
		}
	}

}
