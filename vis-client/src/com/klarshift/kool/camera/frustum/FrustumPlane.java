package com.klarshift.kool.camera.frustum;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4d;

/**
 * frustum plane
 * @author timo
 *
 */
public class FrustumPlane {
	// 
	private double a = 0;
	private double b = 0;
	private double c = 0;
	private double d = 0;	

	public double getB() {
		return b;
	}

	public void setB(double b) {
		this.b = b;
	}

	public double getC() {
		return c;
	}

	public void setC(double c) {
		this.c = c;
	}

	public double getD() {
		return d;
	}

	public void setD(double d) {
		this.d = d;
	}

	public double getA() {
		return a;
	}

	public void setA(double a) {
		this.a = a;
	}
	
	public FrustumPlane(){
		
	}
	
	public String toString(){
		return a + "/" + b + "/" + c + "/" + d;
	}

	/**
	 * check if a point is `inside` the plane
	 * @param p
	 * @return
	 */
	public boolean contains(Vector3d p) {
		if (a*p.x + b*p.y + c*p.z + d < 0){
			return false;
		}
		return true;
	}
	
	public boolean contains(Vector4d p) {
		if (a*p.x + b*p.y + c*p.z + d < 0){
			return false;
		}
		return true;
	}

	/**
	 * normalize the plane
	 */
	public void normalize() {
		double mag = Math.sqrt(a*a+b*b+c*c);
		a /= mag;
		b /= mag;
		c /= mag;
		d /= mag;
	}

	public boolean contains(Vector3f p) {
		if (a*p.x + b*p.y + c*p.z + d < 0){
			return false;
		}
		return true;
	}

}
