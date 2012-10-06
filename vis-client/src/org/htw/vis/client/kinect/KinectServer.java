package org.htw.vis.client.kinect;

import java.io.File;

import javax.vecmath.Point3f;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * kinect server
 * @author timo
 *
 */
public class KinectServer extends Thread {
	private final Logger log = Logger.getLogger("KinectServer");	
	
	static{
		// load the native library for linux
		// TODO this would not run on machines other than linux
		System.load(new File("lib/native-libs/libSimpleOpenNI32.so").getAbsolutePath());
	}
	
	private final KinectContextWrapper ctx;
	
	/**
	 * create a kinect server
	 */
	public KinectServer(){
		ctx = new KinectContextWrapper();		
	}

	/**
	 * get the context itself
	 * @return
	 */
	public KinectContextWrapper getContext(){
		return ctx;
	}
	
	/**
	 * server loop
	 */
	public void run(){
		log.info("Started Kinect Server Thread.");
		while(true){			
			try {
				ctx.update();
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * get a skeleton joint position
	 * @param id
	 * @param j
	 * @return
	 */
	public Point3f getJointPosition(long id, Integer j){
		return ctx.getSkeletonJointPosition(id, j);		
	}
	
	/**
	 * get a joint position for first skeleton
	 * @param j
	 * @return
	 */
	public Point3f getJointPosition(Integer j){
		return ctx.getSkeletonJointPosition(1, j);		
	}
	
	/**
	 * server entry point
	 * @param args
	 */
	public static void main(String[] args){
		// setup logging
		Logger.getRootLogger().setLevel(Level.DEBUG);
		
		// create kinect server and start
		KinectServer server = new KinectServer();
		server.start();			
	}
}
