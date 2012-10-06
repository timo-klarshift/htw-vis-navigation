package org.htw.vis.client.controller;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.htw.vis.client.grid.ImageGrid;
import org.htw.vis.client.kinect.KinectServer;
import org.htw.vis.client.kinect.visual.VisualKinect;

import SimpleOpenNI.SimpleOpenNI;

/**
 * kinect grid controller
 * @author timo
 *
 */
public class KinectGridController  {
	/* settings */
	private static final double MAX_DISTANCE = 1200;
	private static final double MOVE_SPEED = 0.0005f;
	private static final double ZOOM_THRESHOLD = 0.2;
	private static final double ZOOM_SPEED = 0.1;
	
	
	private final ImageGrid grid;	
	private final KinectServer server;
	private final VisualKinect vk;
	
	private double distance = 0;
	Point3f center = new Point3f();
	Vector3d force = new Vector3d();
		
	/**
	 * kinect controller
	 * @param server
	 * @param grid
	 * @param vk
	 */
	public KinectGridController(KinectServer server, ImageGrid grid, VisualKinect vk){
		this.grid = grid;		
		this.vk = vk;
		this.server = server;		
	}
	
	public KinectServer getServer(){
		return server;
	}
	
	/**
	 * update controller
	 */
	public void update(long updateTime, long frameCount){		
		// get needed joints
		Point3f rh = server.getJointPosition(SimpleOpenNI.SKEL_RIGHT_HAND);
		Point3f lh = server.getJointPosition(SimpleOpenNI.SKEL_LEFT_HAND);
		Point3f torso = server.getJointPosition(SimpleOpenNI.SKEL_TORSO);
		
		if(rh != null && lh != null){
			// connection between right hand and left hand
			Vector3f dist = new Vector3f();			
			dist.sub(rh, lh);
			distance = dist.length();
			
			// get center point between connection of
			// both hands
			Vector3f dist2 = new Vector3f(dist);
			dist2.scale(0.5f);					
			center.add(lh, dist2);	
			if(vk != null)
				vk.setCenter(center);
			
			// get movement direction
			// centerPoint -> torso
			Vector3f moveDirection = new Vector3f();
			moveDirection.sub( center, torso);			
			
			// generate force out of movementVector
			force = new Vector3d(moveDirection);			
			force.scale(MOVE_SPEED);	
										
			// reset z-axis
			force.z = 0;			
			
			// set z by distance
			double h = Math.min(1, distance / MAX_DISTANCE);			
			h -= 0.5;
			
			// zoom threshold
			if(Math.abs(h) > ZOOM_THRESHOLD){
				force.z = h * ZOOM_SPEED;			
			}else{
				force.z = 0;
			}			
			
			// update grid itself
			grid.addForce(new Vector3d(force));			
		}
		
	}
}
