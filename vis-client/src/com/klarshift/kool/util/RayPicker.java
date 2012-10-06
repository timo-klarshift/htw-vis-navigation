package com.klarshift.kool.util;

import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.klarshift.kool.math.Ray;

/**
 * ray picker
 * @author timo
 *
 */
public class RayPicker {
	static final GLU glu = new GLU();

	/**
	 * get the ray
	 * @param gl
	 * @param mouseX
	 * @param mouseY
	 * @return
	 */
	public static Ray getRay(GL2 gl, int mouseX, int mouseY) {
		float z = 0;

		int viewport[] = new int[4];
		double modelView[] = new double[16];
		double projection[] = new double[16];
		double wcoord[] = new double[4];// wx, wy, wz;// returned xyz coords

		gl.glGetIntegerv(GL2.GL_VIEWPORT, viewport, 0);
		gl.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, modelView, 0);
		gl.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projection, 0);

		// transform y
		mouseY = viewport[3] - mouseY;

		// unproject near point
		z = .0f;
		glu.gluUnProject(mouseX, mouseY, z, modelView, 0, projection, 0,
				viewport, 0, wcoord, 0);
		Vector3d near = new Vector3d(wcoord[0], wcoord[1], wcoord[2]);

		// unproject far point
		z = .9f;
		glu.gluUnProject(mouseX, mouseY, z, modelView, 0, projection, 0,
				viewport, 0, wcoord, 0);
		Vector3d far = new Vector3d(wcoord[0], wcoord[1], wcoord[2]);

		// get direction
		Vector3d dir = new Vector3d();
		dir.sub(far, near);

		return new Ray(new Vector3f(near), new Vector3f(dir));
	}
}
