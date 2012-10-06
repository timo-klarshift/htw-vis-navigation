package org.htw.vis.client.kinect.visual;

import javax.media.opengl.GL2;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import org.htw.vis.client.kinect.KinectServer;

import SimpleOpenNI.SimpleOpenNI;

import com.jogamp.opengl.util.gl2.GLUT;
import com.klarshift.kool.appearance.Appearance;
import com.klarshift.kool.camera.Camera;
import com.klarshift.kool.render.RenderEngine;
import com.klarshift.kool.scenegraph.VisualNode;

public class VisualKinect extends VisualNode {
	private final KinectServer server;
	private final GLUT glut = new GLUT();
	
	Point3f lh, rh, h, ls, rs, t, le, re, rhi, lhi;
	Point3f center;
		
	
	public void setCenter(Point3f c){
		center = c;
	}
	
	private static final float SCALE = 0.01f;
	
	public VisualKinect(KinectServer server){
		this.server = server;
		Appearance a = new Appearance();
		a.setColor(new Vector3f(1, 1, 0));
		setAppearance(a);
	}

	
	@Override
	public void renderNode(GL2 gl, RenderEngine engine) {
		super.renderNode(gl, engine);	
		
		lh = server.getJointPosition(SimpleOpenNI.SKEL_LEFT_HAND);
		rh = server.getJointPosition(SimpleOpenNI.SKEL_RIGHT_HAND);
		h = server.getJointPosition(SimpleOpenNI.SKEL_HEAD);
		ls = server.getJointPosition(SimpleOpenNI.SKEL_LEFT_SHOULDER);
		rs = server.getJointPosition(SimpleOpenNI.SKEL_RIGHT_SHOULDER);
		t = server.getJointPosition(SimpleOpenNI.SKEL_TORSO);
		
		le = server.getJointPosition(SimpleOpenNI.SKEL_LEFT_ELBOW);
		re = server.getJointPosition(SimpleOpenNI.SKEL_RIGHT_ELBOW);
		

		
		lhi = server.getJointPosition(SimpleOpenNI.SKEL_LEFT_HIP);
		rhi = server.getJointPosition(SimpleOpenNI.SKEL_RIGHT_HIP);
		
		
	
		
		drawCenter(gl, lhi, 0.2);		
		drawCenter(gl, rhi, 0.2);
		
		
		
		drawCenter(gl, lh, 0.2);		
		drawCenter(gl, rh, 0.2);
		
		drawCenter(gl, le, 0.1);
		drawCenter(gl, re, 0.1);
		
		drawCenter(gl, ls, 0.2);
		drawCenter(gl, rs, 0.2);
		

		
		drawCenter(gl, h, 0.5);
		
		drawCenter(gl, center, 0.4);
		
		drawCenter(gl, t, 0.3);	
		
		drawLimbs(gl);
	}
	
	private void drawCenter(GL2 gl, Point3f p, double s){
		if(p != null){
			if(p == center){
				gl.glColor3f(1, 0, 0);
			}else{
				gl.glColor3f(0, 1, 0);
			}
			gl.glPushMatrix();
				gl.glTranslatef(p.x*SCALE, p.y*SCALE, +5 + p.z*-SCALE);
				glut.glutSolidSphere(s, 9, 9);
			gl.glPopMatrix();
		}
	}
	
	private void drawLimbs(GL2 gl){
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glLineWidth(3.0f);

		gl.glBegin(GL2.GL_LINES);
			// shoulders
			drawLimb(gl, rs, ls);
			
			// head to torso
			drawLimb(gl, h, t);
			
			// elbow - hand
			drawLimb(gl, lh, le);
			drawLimb(gl, rh, re);
			
			// elbow - shoulder
			drawLimb(gl, ls, le);
			drawLimb(gl, rs, re);
			
			// hips
			drawLimb(gl, lhi, rhi);
			
			
			
			drawLimb(gl, lhi, t);
			drawLimb(gl, rhi, t);
	
			
		gl.glEnd();
		gl.glPopMatrix();
	}
	
	private void drawLimb(GL2 gl, Point3f a, Point3f b){
		if(a == null || b == null)return;
		
		gl.glVertex3f(a.x*SCALE, a.y*SCALE, 5+a.z*-SCALE);
		gl.glVertex3f(b.x*SCALE, b.y*SCALE, 5+b.z*-SCALE);
	}
}
