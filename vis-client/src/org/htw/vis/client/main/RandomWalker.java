package org.htw.vis.client.main;

import java.util.Random;

import javax.vecmath.Vector3d;

import org.apache.log4j.Logger;
import org.htw.vis.client.grid.ImageGrid;

import com.klarshift.kool.animation.Animator;

/**
 * random walker
 * @author timo
 *
 */
public class RandomWalker extends Animator  {	
	private ImageGrid grid;
	private Vector3d force = new Vector3d();
	private final Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * create the random walker
	 */
	public RandomWalker(){
		log.info("Created random walker");
		setLoop(INFINITE);
		setDuration(5000);
	}
	
	/**
	 * let the walker wal randomly around
	 */
	public void walk(){
		log.info("Random walker is on the way ...");
		
		grid = Application.getInstance().getGrid();
		if(grid == null)return;
		
		start();
		
	}

	@Override
	public void tick(double alpha) {
		Random r = new Random();
		
				
		Vector3d add = new Vector3d();
		if(r.nextDouble() < 0.1){
			
			add.add(new Vector3d((r.nextDouble()-0.5), (r.nextDouble()-0.5), 0));
			
			
		
		}
		
		if(r.nextDouble() > 0.99){
			add.add(new Vector3d(0, 0, (r.nextDouble()-0.5)*10));
		}
		
		
		if(r.nextDouble() > 0.9){
			force.z = 0;
		}
		
		if(add.length() > 0){
			add.normalize();
			
			if(r.nextDouble() > 0.8){
				add.scale(0.8);
			}else{
				add.scale(0.1);	
			}
			
			force.add(add);
		}
		
		if(force.length() > 0){
			force.normalize();	
		
			Vector3d f = new Vector3d(force);
			f.scale(0.05);
			grid.addForce(f);
		}
	}
	
}
