package com.klarshift.kool.geometry;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL2;
import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import com.jogamp.common.nio.Buffers;

/**
 * geometry
 * @author timo
 *
 */
public class Geometry {
	int type;
	
	/* geometry */
	ArrayList<Vector3f> points = new ArrayList<Vector3f>();
	
	/* colors */
	ArrayList<Vector3f> colors = new ArrayList<Vector3f>();
	
	/* texture */
	ArrayList<Point2f> coords = new ArrayList<Point2f>();
	
	boolean compiled = false;	// compiled flag
	
	int pointSize = 0, colorSize = 0, coordSize = 0;
	
	/* buffers */
	FloatBuffer pointBuffer, colorBuffer, coordsBuffer;
		
	public Geometry(int type){
		this.type = type;
	}
	
	public ArrayList<Vector3f> getPoints(){
		return points;
	}
	
	public ArrayList<Vector3f> getColors(){
		return points;
	}
	
	public ArrayList<Point2f> getTextCoords(){
		return coords;
	}

	public void compile() throws Exception{
		float[] v3 = new float[3];
		float[] v2 = new float[2];
		
		// points
		pointBuffer = Buffers.newDirectFloatBuffer(points.size()*3);
		
		for(Vector3f p : points){
			p.get(v3);
			pointBuffer.put(v3);
		}
		pointBuffer.rewind();
		pointSize = points.size();
		
		// colors
		if(colors.size() > 0){
			colorBuffer = Buffers.newDirectFloatBuffer(colors.size()*3);		
			for(Vector3f p : colors){
				p.get(v3);
				colorBuffer.put(v3);
			}
			colorBuffer.rewind();
			colorSize = colors.size();
		}
		
		// coords
		if(coords.size() > 0){
			coordsBuffer = Buffers.newDirectFloatBuffer(coords.size()*2);		
			for(Point2f p : coords){
				p.get(v2);
				coordsBuffer.put(v2);
			}
			coordsBuffer.rewind();
			coordSize = coords.size();
		}
		
		// set compiled and store size
		compiled = true;		
	}
	
	public Geometry add(Vector3f point, Vector3f color){
		addPoint(point).addColor(color);
		return this;
	}
	
	public int colorSize(){
		return colorSize;
	}
	
	public int pointSize(){
		return pointSize;
	}
	
	public int textureSize(){
		return coordSize;
	}
	
	public Geometry addTexCoord(Point2f p){
		coords.add(p);
		onChanged();
		return this;
	}
	
	public Geometry addColor(Vector3f color){
		colors.add(color);
		onChanged();
		return this;
	}
	
	public Geometry addPoint(Vector3f point){
		points.add(point);
		onChanged();
		return this;
	}
	
	public Geometry setColor(int i, Vector3f v){
		colors.set(i, v);
		onChanged();
		return this;
	}
	
	public Geometry setTexCoord(int i, Point2f v){
		coords.set(i, v);
		onChanged();
		return this;
	}
	
	public Geometry setPoint(int i, Vector3f v){
		points.set(i, v);
		onChanged();
		return this;
	}
	
	private void onChanged(){
		compiled = false;
	}
	
	/**
	 * render the geometry
	 * @param gl
	 */
	public void render(GL2 gl){
		// compile
		if(!compiled){
			try {
				compile();
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		
		// enable vertex array
		gl.glEnableClientState( GL2.GL_VERTEX_ARRAY );
		gl.glVertexPointer( 3, GL2.GL_FLOAT, 0, pointBuffer );
		
		
		// enable geometry color array
		if(colorSize > 0){		
			gl.glEnableClientState( GL2.GL_COLOR_ARRAY );
			gl.glColorPointer( 3, GL2.GL_FLOAT, 0, colorBuffer );
		}
		
		// enable coords
		if(coordSize > 0){		
			gl.glEnableClientState( GL2.GL_TEXTURE_COORD_ARRAY );
			gl.glTexCoordPointer (2, GL2.GL_FLOAT, 0, coordsBuffer);
		}						
		
		// draw
		gl.glDrawArrays( type, 0, pointSize );
	}
}
