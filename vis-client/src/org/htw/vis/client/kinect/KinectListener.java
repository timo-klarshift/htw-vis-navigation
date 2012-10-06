package org.htw.vis.client.kinect;

/**
 * kinect listener
 * @author timo
 *
 */
public interface KinectListener {
	/**
	 * a new skeleton has been detected
	 * @param skeleton
	 */
	public void onSkeletonAdded(InternalSkeleton skeleton);
	
	/**
	 * a skeleton has been lost
	 * @param skeleton
	 */
	public void onSkeletonRemoved(InternalSkeleton skeleton);
	
	public void onStartPoseDetected(InternalSkeleton s, String pose);
	
	public void onTrackingStarted(InternalSkeleton s);
}
