package com.klarshift.kool.animation;

import javax.vecmath.Vector3d;

import com.klarshift.kool.scenegraph.KNode;

public class MovementAnimator extends Animator {
	private KNode node;
	private Vector3d target, source, move;
	private double eps = 0.001;
	private double startDistance = -1;
	
	public MovementAnimator(KNode node, Vector3d target){		
		this.node = node;
		this.target = target;
	}
	
	@Override
	public void start() {	
		// store start
		source = new Vector3d(node.t().getPosition());		
		move = new Vector3d();
		move.sub(target, source);
		
		startDistance = move.length();
		
		if(startDistance > eps){				
			super.start();
		}else{
			node.t().setPosition(target);
		}
	}

	@Override
	public void tick(double alpha) {
		// get distance to target
		Vector3d dist = new Vector3d();
		dist.sub(target, node.t().getPosition());
		double distance = dist.length();
		
		double g = 0.2*(distance / startDistance);
		
		if(distance < eps){
			node.t().setPosition(target);
			stop();
		}else{
		
			// move
			Vector3d m = new Vector3d(move);
			double scale = alpha * alpha;
			m.scale(scale);
			
			Vector3d s = new Vector3d(source);
			s.add(m);
			
			node.t().setPosition(s);			
		}
	}	
}
