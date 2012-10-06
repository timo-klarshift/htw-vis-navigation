package com.klarshift.kool.math;

import java.util.ArrayList;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

/**
 * simple triangle modeled by 3 points
 * @author timo
 *
 */
public class Triangle {
	public Vector3f a, b, c;
	
	public Triangle(Vector3f a, Vector3f b, Vector3f c){
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public Triangle(ArrayList<Vector3f> points) {
		a = points.get(0);
		b = points.get(1);
		c = points.get(2);
	}

	public void transform(Matrix4d m) {
		a = transform(a, m);
		b = transform(b, m);
		c = transform(c, m);		
	}
	
	private Vector3f transform(Vector3f a, Matrix4d m){
		Vector4f b = new Vector4f(a);
		b.w = 1;
		m.transform(b);
		return new Vector3f(b.x, b.y, b.z);
	}
	
	public String toString(){
		return "TRI [" + a + "/" + b + "/" + c + "]";
	}
}
