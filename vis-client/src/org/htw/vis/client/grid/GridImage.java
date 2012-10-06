package org.htw.vis.client.grid;

import java.net.MalformedURLException;
import java.net.URL;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.vecmath.Vector3d;

import org.htw.vis.api.ApiImage;
import org.htw.vis.client.main.Application;
import org.htw.vis.helper.ImageAccess;
import org.htw.vis.layer.INode;
import org.htw.vis.server.context.ContainerImage;

import com.klarshift.kool.animation.Animator;
import com.klarshift.kool.animation.FadeAnimator;
import com.klarshift.kool.animation.MovementAnimator;
import com.klarshift.kool.appearance.Appearance;
import com.klarshift.kool.appearance.KTexture;
import com.klarshift.kool.appearance.TextureManager;
import com.klarshift.kool.geometry.Geometry;
import com.klarshift.kool.geometry.PlaneGeometry;
import com.klarshift.kool.render.RenderEngine;
import com.klarshift.kool.scenegraph.VisualNode;

/**
 * grid image
 * @author timo
 *
 */
public class GridImage extends VisualNode implements INode {
	
	/* static geometry */
	
	private static Geometry geo = new PlaneGeometry(1.0f, 1.0f);
	private ApiImage apiImage;
	private ContainerImage containerImage;
		
	private int x = -1, y = -1;
	
	public boolean selected = false;
	
	Appearance a = new Appearance();	
	KTexture tex;

	private FadeAnimator fadeAnimator;

	private double zIndex = 0.1+Math.random()*1;
	
	static ImageAccess access = new ImageAccess();
	static TextureManager texMgr = TextureManager.getInstance(); 
	
	private String url = null;
	private Integer id = -1;

	
	
	/**
	 * create an grid image from api image
	 * @param ai
	 */
	public GridImage(ApiImage ai){
		setImage(ai);
		init();
	}
	
	public GridImage(ContainerImage cc){
		setImage(cc);	
		init();
	}
	
	public void loadTexture(int prio){			
		// load texture
		try {
			texMgr.addTexture(id.toString(), new URL(url), "jpg", prio);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private void init(){
		
		
		setGeometry(geo);
		//setPosition(x, y);
		
		double d45 = Math.PI/8;
		t().rotateZ(Math.random()*d45-d45/2);
		t().scale(0.8f);
		
	
		
		a.setTransparency(0.995f);
		a.setTransparency(0);
		fadeAnimator = new FadeAnimator(a);
		setAppearance(a);
	}
	
	public void setImage(ApiImage ai){
		this.apiImage = ai;	
		this.url = apiImage.getImageSource();
		this.id = ai.getFotoliaId();
		x = apiImage.getX();
		y = apiImage.getY();
		setPosition(x, y);		
	}
	
	public void setImage(ContainerImage containerImage){		
		this.containerImage = containerImage;
		this.url = containerImage.getImageSource();		
		this.id = containerImage.getFotoliaId();
		
		x = containerImage.getX();
		y = containerImage.getY();
		setPosition(x, y);					
	}
	
	public int getX(){return x;}
	public int getY(){return y;}
	
	void setPosition(int x, int y){
		this.x = x;
		this.y = y;
		t().setPosition(x, -y, zIndex );
	}
	
	@Override
	public void renderNode(GL2 gl, RenderEngine engine) {
		
		
		if(tex == null || tex.isLoaded() == false){
			//super.renderNode(gl, engine);
			return;
		}
		
		if(a.getTransparency() > 0.95)
			return;
		
		super.renderNode(gl, engine);
		
		
								
		if(false){			
			gl.glDisable(GL.GL_TEXTURE_2D);
			gl.glColor3f(0, 1, 0);
			gl.glBegin(GL2.GL_LINES);
			gl.glLineWidth(2f);
				gl.glVertex3f(-0.5f, -0.5f, 0.2f);
				gl.glVertex3f(0.5f, -0.5f, 0.2f);				
				gl.glVertex3f(0.5f, -0.5f, 0.2f);
				gl.glVertex3f(0.5f, 0.5f, 0.2f);				
				gl.glVertex3f(0.5f, 0.5f, 0.2f);
				gl.glVertex3f(-0.5f, 0.5f, 0.2f);				
				gl.glVertex3f(-0.5f, 0.5f, 0.2f);
				gl.glVertex3f(-0.5f, -0.5f, 0.2f);
			gl.glEnd();
		}
	}
	
	public void fadeIn(){
		getAppearance().setTransparency(1);
		fadeAnimator.stop().setDuration((long) (1000 + Math.random()*2000)).start();
	}
	
	@Override
	public void update(long updateTime, long frameCount, RenderEngine engine) {	
		super.update(updateTime, frameCount, engine);		
		
		// set texture
		if(tex == null){
			tex = texMgr.getTexture(getFotoliaId().toString());
			
			if(tex != null){				
				a.setTexture(tex);
								
				
				if(Application.getInstance().getCamera().isVisible(this))
					fadeIn();
				else
					a.setTransparency(0);
			}					
		}
		
		ImageGrid grid = Application.getInstance().getGrid();
		Vector3d inter = grid.getIntersectionPoint();
		if(inter != null && false){
			double max = 15;
			Vector3d f = new Vector3d();
			f.sub(inter, getGlobalPosition());
			double distance = f.length();
			float s;
			if(distance > max){
				s = 0.3f;
			}else{
				s = (float) (1-(distance / max));
				s *= s;				
			}	
			
			//t().setZ(s - 1);
			
			t().scale(s*0.8f);			
		}
		
		/*if(!fadeAnimator.isAnimating()){
			Vector3d cd = Application.getInstance().getCamera().getGlobalPosition();
			Vector3d dist = new Vector3d();
			dist.sub(cd, getGlobalPosition());
			double len = dist.length();
			getAppearance().setTransparency((float) (len/30.0));
		}*/
	}
	
	/*public ApiImage getApiImage(){
		return apiImage;
	}*/

	public String toString(){
		return "GridImage " + id + " :. " + x + "/" + y;
	}

	@Override
	public Integer getFotoliaId() {
		return id;
	}

	@Override
	public String getWords() {
		return null;
	}

	public void flyTo(double x, double y) {
		flyTo(x, y, t().getPosition().z);		
	}
	
	public void flyTo(double x, double y, double z) {
		Animator a = new MovementAnimator(this, new Vector3d(x, y, z));
		a.start();		
	}
}
