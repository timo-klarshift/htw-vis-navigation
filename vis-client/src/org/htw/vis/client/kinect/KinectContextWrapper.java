package org.htw.vis.client.kinect;

import java.util.HashMap;
import java.util.LinkedList;

import javax.vecmath.Point3f;

import org.apache.log4j.Logger;

import SimpleOpenNI.ContextWrapper;
import SimpleOpenNI.SimpleOpenNI;
import SimpleOpenNI.XnSkeletonJointPosition;
import SimpleOpenNI.XnVector3D;

/**
 * skeleton state model
 * @author timo
 *
 */
enum SkeletonState {
	RECOGNIZED, CALIBRATING, CALIBRATED, LOST
}

/**
 * kinect context wrapper
 * @author timo
 *
 */
public class KinectContextWrapper extends ContextWrapper {
	/* logging */
	private final Logger log = Logger.getLogger("Kinect Context");

	private boolean closed = false;
	
	/* skeleton holder */
	private final HashMap<Long, InternalSkeleton> skeletons = new HashMap<Long, InternalSkeleton>();
	private final LinkedList<KinectListener> listeners = new LinkedList<KinectListener>();

	/**
	 * create a context wrapper
	 */
	public KinectContextWrapper() {		
		// init context
		init();
		
		// enable full skeleton
		enableUser(SimpleOpenNI.SKEL_PROFILE_ALL);
		
		// enable depth map
		enableDepth();
		
		setMirror(true);
		// enableRGB();
		
		log.info("Context initialised");
	}
	
	/**
	 * add a kinect listener
	 * @param listener
	 */
	public void addListener(KinectListener listener){
		listeners.add(listener);
	}
	
	/**
	 * internally add a skeleton
	 * @param s
	 */
	private void addSkeleton(InternalSkeleton s){
		// put in map
		skeletons.put(s.getId(), s);
		log.info("Added Skeleton: " + s);
		
		// inform listeners
		for(KinectListener l : listeners){
			l.onSkeletonAdded(s);
		}
	}
	
	/**
	 * internally remove a skeleton
	 * @param s
	 */
	private void removeSkeleton(InternalSkeleton s ){
		// put in map
		skeletons.remove(s.getId());
		log.info("Removed Skeleton: " + s);
		
		// inform listeners
		for(KinectListener l : listeners){
			l.onSkeletonRemoved(s);
		}
	}

	@Override
	protected void onNewUserCb(long id) {
		log.info("Detected User " + id);
		
		// get skeleton, set state and add it
		InternalSkeleton skel = new InternalSkeleton((int)id);
		skel.setState(SkeletonState.RECOGNIZED);
		addSkeleton(skel);
	}
	
	/**
	 * get skeleton by id
	 * @param id
	 * @return
	 */
	public InternalSkeleton getSkeleton(long id){
		return skeletons.get(id);
	}

	@Override
	protected void onLostUserCb(long id) {
		// get skeleton and set LOST state
		InternalSkeleton s = getSkeleton(id);
		s.setState(SkeletonState.LOST);
		
		// remove it
		removeSkeleton(s);
	}

	@Override
	protected void onStartPoseCb(String pose, long id) {
		InternalSkeleton s = getSkeleton(id);
		log.info("Detected Start Pose for: " + s);
				
		// inform listeners
		for(KinectListener l : listeners){
			l.onStartPoseDetected(s, pose);
		}
	}

	@Override
	protected void onStartCalibrationCb(long id) {
		InternalSkeleton s = getSkeleton(id);
		s.setState(SkeletonState.CALIBRATING);
		
		log.info("Start calibration for: " + s);			
	}

	@Override
	protected void onEndCalibrationCb(long id, final boolean success) {
		InternalSkeleton s = getSkeleton(id);
		if (success) {
			log.info("Calibration successful for: " + s);
			s.setState(SkeletonState.CALIBRATED);
			
			
			// start tracking
			startTrackingSkeleton((int) id);
			log.info("Started Tracking: " + s);
			
			// inform listeners
			for(KinectListener l : listeners){
				l.onTrackingStarted(s);
			}
		} else {
			// reset state and restart calibration
			s.setState(SkeletonState.RECOGNIZED);			
			requestCalibrationSkeleton((int) id, true);
		}
		
	}

	@Override
	public void update() {
		super.update();
		
		// update joints for all skeletons
		for (long id : skeletons.keySet()) {
			InternalSkeleton internalSkeleton = skeletons.get(id);			
			if (internalSkeleton.getState() != SkeletonState.CALIBRATED)
				continue;

			// update the joints
			updateSkeletonJoints(internalSkeleton);			
		} 
	}

	/**
	 * update all joints
	 * @param s
	 */
	private void updateSkeletonJoints(InternalSkeleton s) {		
		// set joints
		setJoint(s, SimpleOpenNI.SKEL_HEAD);
		setJoint(s, SimpleOpenNI.SKEL_TORSO);
		setJoint(s, SimpleOpenNI.SKEL_RIGHT_HAND);
		setJoint(s, SimpleOpenNI.SKEL_LEFT_HAND);
		
		setJoint(s, SimpleOpenNI.SKEL_LEFT_SHOULDER);
		setJoint(s, SimpleOpenNI.SKEL_RIGHT_SHOULDER);	
		
		setJoint(s, SimpleOpenNI.SKEL_LEFT_ELBOW);
		setJoint(s, SimpleOpenNI.SKEL_RIGHT_ELBOW);
		
		setJoint(s, SimpleOpenNI.SKEL_LEFT_KNEE);
		setJoint(s, SimpleOpenNI.SKEL_RIGHT_KNEE);
		
		setJoint(s, SimpleOpenNI.SKEL_LEFT_FOOT);
		setJoint(s, SimpleOpenNI.SKEL_RIGHT_FOOT);
		
		setJoint(s, SimpleOpenNI.SKEL_LEFT_HIP);
		setJoint(s, SimpleOpenNI.SKEL_RIGHT_HIP);
			
	}
	
	/**
	 * get the 3D position for given skeleton id and joint id
	 * @param skeletonId
	 * @param joint
	 * @return
	 */
	public Point3f getSkeletonJointPosition(long skeletonId, Integer joint){
		InternalSkeleton s = getSkeleton(skeletonId);
		if(s != null){
			return s.getJoint(joint);
		}
		return null;
	}
	
	/**
	 * internal joint setter
	 * @param s
	 * @param j
	 */
	private void setJoint(InternalSkeleton s, Integer j){
		s.setJoint(j, readPosition(s, j));
	}
	
	/**
	 * read position from frame buffer
	 * @param s
	 * @param joint
	 * @return
	 */
	private Point3f readPosition(InternalSkeleton s, int joint){
		XnSkeletonJointPosition tmpPos = new XnSkeletonJointPosition();
		getJointPositionSkeleton((int)s.getId(), joint, tmpPos);
		XnVector3D p = tmpPos.getPosition();
		return new Point3f(p.getX(), p.getY(), p.getZ());		
	}

	@Override
	protected void finalize() {
		if (!closed) {
			closed = true;
			close();
		}
		super.finalize();
	}
}

/**
 * internal skeleton model
 * @author timo
 *
 */
class InternalSkeleton {
	private SkeletonState state = SkeletonState.LOST;
	private HashMap<Integer,Point3f> joints = new HashMap<Integer,Point3f>();
	
	private long id;
	
	/**
	 * create new skeleton
	 * @param id
	 */
	public InternalSkeleton(long id){
		this.id = id;
	}
	
	/**
	 * set skeleton joint
	 * @param i
	 * @param p
	 */
	public void setJoint(Integer i, Point3f p){
		joints.put(i,  p);
	}
	
	/**
	 * get skeleton joint
	 * @param i
	 * @return
	 */
	public Point3f getJoint(Integer i){
		return joints.get(i);
	}
	
	/**
	 * set skeleton state
	 * @param state
	 */
	public void setState(SkeletonState state) {
		this.state = state;
	}
	
	/**
	 * get skeleton state
	 * @return
	 */
	public SkeletonState getState(){
		return state;
	}

	/**
	 * get skeleton id
	 * @return
	 */
	public long getId(){
		return id;
	}
	
	/**
	 * string representation
	 */
	public String toString(){
		return "" + id + " / STATE=" + state;
	}
}


