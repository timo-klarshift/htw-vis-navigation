package org.htw.vis.client.controller;

import java.awt.event.KeyEvent;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import org.htw.vis.client.grid.ImageGrid;
import org.htw.vis.client.main.Application;

import com.klarshift.kool.camera.Camera;
import com.klarshift.kool.camera.Camera3D;
import com.klarshift.kool.canvas.RenderCanvas;
import com.klarshift.kool.controller.KeyboardController;
import com.klarshift.kool.scenegraph.KNode;

/**
 * keyboard application controller
 * @author timo
 *
 */
public class KeyboardApplicationController {
	private final Application app;
	private final ImageGrid grid;
	private final RenderCanvas canvas;
	private final KeyboardController keyboard = new KeyboardController();
	
	/**
	 * create an keyboard application controller
	 */
	public KeyboardApplicationController(){
		app = Application.getInstance();
		grid = app.getGrid();
		canvas = app.getCanvas();		
		canvas.addKeyListener(keyboard);
	}
	
	/**
	 * update the controller
	 */
	public void update(long updateTime, long frameCount){
		double s = 0.1;
		
		// super-fast :)
		if(keyboard.isKeyPressed(KeyEvent.VK_CONTROL)){
			s *= 10;
		}
		
		Vector3d force = new Vector3d();
		
		// movement
		if(keyboard.isKeyPressed(KeyEvent.VK_LEFT)){
			force.add(new Vector3d(s, 0, 0));				
		}
		if(keyboard.isKeyPressed(KeyEvent.VK_RIGHT)){				
			force.add(new Vector3d(-s, 0, 0));
		}
		if(keyboard.isKeyPressed(KeyEvent.VK_UP)){				
			force.add(new Vector3d(0, -s, 0));
		}
		if(keyboard.isKeyPressed(KeyEvent.VK_DOWN)){				
			force.add(new Vector3d(0, s, 0));
		}
		
		// z-axis
		if(keyboard.isKeyPressed(KeyEvent.VK_PAGE_UP)){				
			grid.addForce(new Vector3d(0, 0, s));
		}
		if(keyboard.isKeyPressed(KeyEvent.VK_PAGE_DOWN)){				
			grid.addForce(new Vector3d(0, 0, -s));
		}
		
		// transform into grids system
		Matrix4d mat = grid.getGlobalMatrix();
		mat.transform(force);		
		grid.addForce(force);
		
		// rotation
		if(keyboard.isKeyPressed(KeyEvent.VK_A)){				
			grid.t().rotateY(s);
		}
		if(keyboard.isKeyPressed(KeyEvent.VK_D)){				
			grid.t().rotateY(-s);
		}
		if(keyboard.isKeyPressed(KeyEvent.VK_W)){				
			grid.t().rotateX(s);
		}
		if(keyboard.isKeyPressed(KeyEvent.VK_S)){				
			grid.t().rotateX(-s);
		}
		
		// reset position
		if(keyboard.isKeyPressed(KeyEvent.VK_SPACE)){
			grid.stopMovement();
			grid.t().reset();
			grid.t().setPosition(0, 0, -10);			
		}
		
		// reload image toggle
		/*if(keyboard.isKeyPressed(KeyEvent.VK_L)){
			if(keyboard.isKeyPressed(KeyEvent.VK_CONTROL)){
				app.setReloadImages(false);
			}else{
				app.setReloadImages(true);
			}
		}*/	
		
		// reload image toggle
		Camera c =  Application.getInstance().getCamera();
		
		
		
		if(keyboard.isKeyPressed(KeyEvent.VK_NUMPAD9)){			
			c.setFOV(c.getFOV()+0.1f);
		}else if(keyboard.isKeyPressed(KeyEvent.VK_NUMPAD3)){			
			c.setFOV(c.getFOV()-0.1f);
		}else if(keyboard.isKeyPressed(KeyEvent.VK_NUMPAD6)){			
			c.setFOV(45);
		}
		
		if(c instanceof Camera3D){
			if(keyboard.isKeyPressed(KeyEvent.VK_NUMPAD8)){
				Camera3D cam = ((Camera3D)c); 
				cam.setEyeDistance(cam.getEyeDistance()+0.01);				
			}else if(keyboard.isKeyPressed(KeyEvent.VK_NUMPAD2)){
				Camera3D cam = ((Camera3D)c); 
				cam.setEyeDistance(cam.getEyeDistance()-0.01);	
			}else if(keyboard.isKeyPressed(KeyEvent.VK_NUMPAD5)){
				Camera3D cam = ((Camera3D)c); 
				cam.setEyeDistance(0);	
			}
		}
		
		if(keyboard.isKeyPressed(KeyEvent.VK_G ) && frameCount % 100 == 0){
			KNode n = Application.getInstance().getGui();
			n.setVisible(!n.isVisible());
		}
	}
}
