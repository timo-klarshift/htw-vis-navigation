package com.klarshift.kool.render;

import javax.vecmath.AxisAngle4d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

/**
 * transformation
 * @author timo
 *
 */
public class Transformation {
	Vector3d position = new Vector3d(0, 0, 0);
	float scale = 1.0f;
	Quat4d rotation = new Quat4d(0, 0, 0, 1);
	
	/**
	 * create transformation
	 */
	public Transformation(){
		reset();
	}
	
	/**
	 * set current position
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public Transformation setPosition(double x, double y, double z){		
		position.set(x, y, z);
		return this;
	}
	
	public Transformation setPosition(Vector3d v){
		position.set(v);
		return this;
	}
	
	/**
	 * get the local matrix
	 * @return
	 */
	public Matrix4d getLocalMatrix(){
		Matrix4d matrix = new Matrix4d(rotation, position, scale);		
		return matrix;
	}
		
	/**
	 * scale current local matrix
	 * @param scale
	 * @return
	 */
	public Transformation scale(float scale){
		this.scale = scale;
		return this;
	}	
	
	public AxisAngle4d getAxisAngle(){
		return getAxisAngle(rotation);
	}
	
	public static AxisAngle4d getAxisAngle(Quat4d rotation){
		rotation.normalize();
		Vector3d axis = new Vector3d(rotation.x, rotation.y, rotation.z);
		axis.normalize();
		double angle = 2*Math.acos(rotation.w) ;//* (180.0 / Math.PI);
		AxisAngle4d aa = new AxisAngle4d(axis, angle);
		return aa;
	}
	
	public Transformation rotate(double x, double y, double z, double angle) {
		AxisAngle4d aa = new AxisAngle4d(new Vector3d(x, y, z), angle);
		return rotate(aa);
	}

	public Transformation rotate(AxisAngle4d aa) {
		Quat4d rot = new Quat4d();
		rot.set(aa);
		return rotate(rot);
	}
	
	public Transformation rotateX(double rad) {
		return rotate(1.0, 0, 0, rad);
	}

	public Transformation rotateY(double rad) {
		return rotate(0, 1.0, 0, rad);
	}

	public Transformation rotateZ(double rad) {
		return rotate(0, 0, 1.0, rad);
	}

	public Transformation rotate(Quat4d rot) {
		rotation.mul(rot);
		rotation.normalize();		
		return this;
	}

	public Transformation move(double x, double y, double z) {
		setPosition(position.x+x, position.y+y, position.z+z);
		return this;
	}

	public Vector3d getPosition() {
		return position;
	}

	public float getScale() {
		return scale;
	}

	public Transformation reset() {
		position = new Vector3d(0, 0, 0);
		scale = 1.0f;
		rotation = new Quat4d(0, 0, 0, 1);
		return this;
	}

	public Transformation setZ(double v) {
		position.z = v;
		return this;
	}
	public Transformation setX(double v) {
		position.x = v;
		return this;
	}
	public Transformation setY(double v) {
		position.y = v;
		return this;
	}

	public Transformation move(Vector3d d) {
		move(d.x, d.y, d.z);
		return this;
	}

	public Transformation setPosition(int x, int y) {
		return setPosition(x, y, 0);	
	}
	
	public Transformation setPosition(double x, double y) {
		return setPosition(x, y, 0);	
	}
}
