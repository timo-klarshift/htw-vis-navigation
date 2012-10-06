package org.htw.vis.client.kinect.visual;

import java.io.File;

import javax.vecmath.Vector3f;

import com.klarshift.kool.appearance.Appearance;
import com.klarshift.kool.appearance.KTexture;
import com.klarshift.kool.geometry.Geometry;
import com.klarshift.kool.geometry.PlaneGeometry;
import com.klarshift.kool.scenegraph.VisualNode;

public class CalibrationShape extends VisualNode {
	KTexture tex;
	
	public CalibrationShape(){
		super("calibrationShape");
		
		Appearance a = new Appearance();
		Geometry geo = new PlaneGeometry(0.8f, 1f);
		setGeometry(geo);
		
		// load texture
		/*if(tex == null){					
			tex = new KTexture(new File("data/images/shape.png"), "png");			
			a.setTexture(tex);
		}*/
		
		setAppearance(a);
	}
	
}
