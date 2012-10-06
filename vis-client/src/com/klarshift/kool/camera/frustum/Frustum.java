package com.klarshift.kool.camera.frustum;

import javax.media.opengl.GL2;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4d;


/**
 * camera frustum
 * 
 * http://wiki.delphigl.com/index.php/Tutorial_Frustum_Culling
 * http://www.lighthouse3d.com/tutorials/view-frustum-culling/
 * 
 * 
 * @author timo
 *
 */
public class Frustum {
	// plane types
	static final int NEAR = 0;
	static final int FAR = 1;
	static final int RIGHT = 2;
	static final int LEFT = 3;
	static final int TOP = 4;
	static final int BOTTOM = 5;


	// plane holder
	private FrustumPlane planes[] = new FrustumPlane[6];

	/**
	 * create frustum
	 * @param camera
	 */
	public Frustum() {			
		// init planes
		for (int i = 0; i < 6; i++) {
			planes[i] = new FrustumPlane();
		}
	}

	/**
	 * calculate the frustum
	 * @param gl
	 */
	public void calculate(GL2 gl) {
		double mvmatrix[] = new double[16];
		double projmatrix[] = new double[16];
		double clip[] = new double[16];
		
		gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, mvmatrix, 0);
		gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projmatrix, 0);
		clip[0] = mvmatrix[0] * projmatrix[0] + mvmatrix[1] * projmatrix[4]
				+ mvmatrix[2] * projmatrix[8] + mvmatrix[3] * projmatrix[12];
		clip[1] = mvmatrix[0] * projmatrix[1] + mvmatrix[1] * projmatrix[5]
				+ mvmatrix[2] * projmatrix[9] + mvmatrix[3] * projmatrix[13];
		clip[2] = mvmatrix[0] * projmatrix[2] + mvmatrix[1] * projmatrix[6]
				+ mvmatrix[2] * projmatrix[10] + mvmatrix[3] * projmatrix[14];
		clip[3] = mvmatrix[0] * projmatrix[3] + mvmatrix[1] * projmatrix[7]
				+ mvmatrix[2] * projmatrix[11] + mvmatrix[3] * projmatrix[15];
		clip[4] = mvmatrix[4] * projmatrix[0] + mvmatrix[5] * projmatrix[4]
				+ mvmatrix[6] * projmatrix[8] + mvmatrix[7] * projmatrix[12];
		clip[5] = mvmatrix[4] * projmatrix[1] + mvmatrix[5] * projmatrix[5]
				+ mvmatrix[6] * projmatrix[9] + mvmatrix[7] * projmatrix[13];
		clip[6] = mvmatrix[4] * projmatrix[2] + mvmatrix[5] * projmatrix[6]
				+ mvmatrix[6] * projmatrix[10] + mvmatrix[7] * projmatrix[14];
		clip[7] = mvmatrix[4] * projmatrix[3] + mvmatrix[5] * projmatrix[7]
				+ mvmatrix[6] * projmatrix[11] + mvmatrix[7] * projmatrix[15];
		clip[8] = mvmatrix[8] * projmatrix[0] + mvmatrix[9] * projmatrix[4]
				+ mvmatrix[10] * projmatrix[8] + mvmatrix[11] * projmatrix[12];
		clip[9] = mvmatrix[8] * projmatrix[1] + mvmatrix[9] * projmatrix[5]
				+ mvmatrix[10] * projmatrix[9] + mvmatrix[11] * projmatrix[13];
		clip[10] = mvmatrix[8] * projmatrix[2] + mvmatrix[9] * projmatrix[6]
				+ mvmatrix[10] * projmatrix[10] + mvmatrix[11] * projmatrix[14];
		clip[11] = mvmatrix[8] * projmatrix[3] + mvmatrix[9] * projmatrix[7]
				+ mvmatrix[10] * projmatrix[11] + mvmatrix[11] * projmatrix[15];
		clip[12] = mvmatrix[12] * projmatrix[0] + mvmatrix[13] * projmatrix[4]
				+ mvmatrix[14] * projmatrix[8] + mvmatrix[15] * projmatrix[12];
		clip[13] = mvmatrix[12] * projmatrix[1] + mvmatrix[13] * projmatrix[5]
				+ mvmatrix[14] * projmatrix[9] + mvmatrix[15] * projmatrix[13];
		clip[14] = mvmatrix[12] * projmatrix[2] + mvmatrix[13] * projmatrix[6]
				+ mvmatrix[14] * projmatrix[10] + mvmatrix[15] * projmatrix[14];
		clip[15] = mvmatrix[12] * projmatrix[3] + mvmatrix[13] * projmatrix[7]
				+ mvmatrix[14] * projmatrix[11] + mvmatrix[15] * projmatrix[15];				

		planes[RIGHT].setA(clip[3] - clip[0]);
		planes[RIGHT].setB(clip[7] - clip[4]);
		planes[RIGHT].setC(clip[11] - clip[8]);
		planes[RIGHT].setD(clip[15] - clip[12]);
		planes[RIGHT].normalize();

		planes[LEFT].setA(clip[3] + clip[0]);
		planes[LEFT].setB(clip[7] + clip[4]);
		planes[LEFT].setC(clip[11] + clip[8]);
		planes[LEFT].setD(clip[15] + clip[12]);
		planes[LEFT].normalize();

		planes[BOTTOM].setA(clip[3] + clip[1]);
		planes[BOTTOM].setB(clip[7] + clip[5]);
		planes[BOTTOM].setC(clip[11] + clip[9]);
		planes[BOTTOM].setD(clip[15] + clip[13]);
		planes[BOTTOM].normalize();

		planes[TOP].setA(clip[3] - clip[1]);
		planes[TOP].setB(clip[7] - clip[5]);
		planes[TOP].setC(clip[11] - clip[9]);
		planes[TOP].setD(clip[15] - clip[13]);
		planes[TOP].normalize();

		planes[FAR].setA(clip[3] - clip[2]);
		planes[FAR].setB(clip[7] - clip[6]);
		planes[FAR].setC(clip[11] - clip[10]);
		planes[FAR].setD(clip[15] - clip[14]);
		planes[FAR].normalize();

		planes[NEAR].setA(clip[3] + clip[2]);
		planes[NEAR].setB(clip[7] + clip[6]);
		planes[NEAR].setC(clip[11] + clip[10]);
		planes[NEAR].setD(clip[15] + clip[14]);
		planes[NEAR].normalize();		
	}

	/**
	 * check if a point is contained in the frustum
	 * @param point
	 * @return
	 */
	public boolean contains(Vector3d point) {
		for (FrustumPlane p : planes) {
			if (p.contains(point) == false) {
				return false;
			}
		}
		return true;
	}
	
	public boolean contains(Vector4d point) {
		for (FrustumPlane p : planes) {
			if (p.contains(point) == false) {
				return false;
			}
		}
		return true;
	}
	
	public boolean contains(Vector3f point) {
		for (FrustumPlane p : planes) {
			if (p.contains(point) == false) {
				return false;
			}
		}
		return true;
	}

	/**
	 * retrieve a plane
	 * @param type
	 * @return
	 */
	public FrustumPlane getPlane(int type) {
		return planes[type];
	}
}
