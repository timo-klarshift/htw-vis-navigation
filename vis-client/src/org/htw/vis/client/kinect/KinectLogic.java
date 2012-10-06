package org.htw.vis.client.kinect;

import org.htw.vis.client.kinect.visual.CalibrationShape;
import org.htw.vis.client.main.Application;

/**
 * kinect logic
 * @author timo, daniel, katrin
 *
 */
public class KinectLogic implements KinectListener {
	private KinectContextWrapper context;
	
	/**
	 * create kinect logic
	 * @param ctx
	 */
	public KinectLogic(KinectContextWrapper ctx){
		this.context = ctx;
		context.addListener(this);
	}
	
	private CalibrationShape getShape(){
		return (CalibrationShape) Application.getInstance().getScene().getByName("calibrationShape");
	}

	@Override
	public void onSkeletonAdded(InternalSkeleton skeleton) {
		// TODO: Show helping stuff for calibration
		CalibrationShape shape = getShape();
		if(shape != null)
			shape.setVisible(true);
		
		// begin start pose detection
		context.startPoseDetection("Psi", (int) skeleton.getId());
	}

	@Override
	public void onSkeletonRemoved(InternalSkeleton skeleton) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartPoseDetected(InternalSkeleton s, String pose) {
		// stop current detection
		context.stopPoseDetection((int) s.getId());
		
		// calibrate skeleton
		context.requestCalibrationSkeleton((int) s.getId(), true);
	}

	@Override
	public void onTrackingStarted(InternalSkeleton s) {
		// TODO Hide helping stuff for calibration
		
	}
}
