package org.htw.vis.client.grid;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import org.htw.vis.client.main.Application;
import org.htw.vis.server.context.ContainerImage;

import com.klarshift.kool.appearance.TextureManager;
import com.klarshift.kool.camera.Camera;
import com.klarshift.kool.math.Plane;
import com.klarshift.kool.math.Ray;
import com.klarshift.kool.math.RayPlaneIntersection;
import com.klarshift.kool.primitive.Triangle;
import com.klarshift.kool.render.RenderEngine;
import com.klarshift.kool.scenegraph.KNode;
import com.klarshift.kool.scenegraph.VisualNode;

/**
 * application image grid
 * 
 * @author timo
 */
public class ImageGrid extends VisualNode {
	// settings
	// TODO: replace with getters and setters
	private static final double FRICTION = 0.85;
	private static final double MAX_CAM_DISTANCE = 50;
	private static final double MIN_CAM_DISTANCE = 3;

	// movement and position
	private final Vector3d initPosition = new Vector3d(0, 0, -5);
	private Vector3d force = new Vector3d();

	/* dimension */
	private final int width, height;

	/* visuals */
	private KNode imageLayer = new KNode("images");
	

	private int focusX = -1;
	private int focusY = -1;
	private int shiftX = 0;
	private int shiftY = 0;
	
	public int getShiftX(){
		return shiftX;
	}
	
	public int getShiftY(){
		return shiftY;
	}

	/* images */
	LinkedHashMap<Integer, GridImage> imageMap = new LinkedHashMap<Integer, GridImage>();

	long updateCounter = 0;

	private Triangle tri = new Triangle(0.2f, 0.2f);

	private Vector3d intersectionPoint;

	private double camDistance;

	/**
	 * create the image grid
	 * 
	 * @param width
	 * @param height
	 */
	public ImageGrid(int width, int height) {
		super("Image Grid");

		// keep dimension
		this.width = width;
		this.height = height;

		// move to side
		imageLayer.t().move(-width / 2 + 0.5, height / 2 - 0.5, 0);

		// add sample node

		// add the layer
		add(imageLayer);

		// set the position
		t().setPosition(initPosition);
	}
	
	public GridImage getImage(Integer id){
		return imageMap.get(id);
	}

	/**
	 * move the grid
	 * 
	 * @param f
	 */
	public void addForce(Vector3d f) {
		force.add(f);
	}

	/**
	 * set current force
	 * 
	 * @param f
	 */
	public void setForce(Vector3d f) {
		force = f;
	}

	/**
	 * stop moving
	 */
	public void stopMovement() {
		log.info("Stop movement.");
		setForce(new Vector3d());
	}
	
	public double getCamDistance(){
		return camDistance;
	}

	@Override
	public void update(long updateTime, long frameCount, RenderEngine engine) {
		super.update(updateTime, frameCount, engine);

		// move grid by force
		double speed = 0.008 * updateTime;
		t().move(force.x * speed, force.y * speed, force.z * speed);
		// slow down force
		force.scale(FRICTION);

		updateCounter += updateTime;

		// get current context position

		Vector3d centerToIntersection = new Vector3d();

		Camera cam = Application.getInstance().getCamera();
		Ray pickRay = cam.getPickRay();

		if (pickRay != null) {

			// get plane
			Plane plane = new Plane(new Vector3f(getGlobalPosition()),
					new Vector3f(getLookAtVector()));

			// intersect
			RayPlaneIntersection rpi = new RayPlaneIntersection(pickRay, plane);
			rpi.intersect();
			Vector3d ip = new Vector3d(rpi.getIntersectionPoint());
			Application.getInstance().triangle.t()
					.setPosition(ip.x, ip.y, ip.z); // global intersection point
			intersectionPoint = new Vector3d(ip);
			
			// get camera distance
			Vector3d cd = new Vector3d();
			cd.sub(intersectionPoint, cam.getGlobalPosition());
			camDistance = cd.length();

			// get connection between local origin and intersection point
			Vector3d gp = getGlobalPosition();
			centerToIntersection.sub(ip, gp);

			// transform this connection into local
			// axis of grid
			Matrix4d m = getGlobalMatrix();
			Matrix4d mi = new Matrix4d(m);
			mi.invert();
			mi.transform(centerToIntersection);

			// get local position of triangle
			// Vector3d t = new Vector3d(t().getPosition());
			// t.add(centerToIntersection);
			// tri.t().setPosition(centerToIntersection.x,
			// centerToIntersection.y, centerToIntersection.z);

			// transform to 2D projection
			// and get grid position
			// then spot images at this position
			double sx = centerToIntersection.x + width / 2;
			double sy = centerToIntersection.y + height / 2;

			// get direction to move
			Vector3d back = new Vector3d(centerToIntersection);
			back.x = Math.round(back.x);
			back.y = Math.round(back.y);
			back.z = Math.round(back.z);

			// get distance from center of grid
			// to intersection point
			double centerDistance = centerToIntersection.length();
			double maxDistance = Math.min(width, height)/5;
			if (centerDistance > maxDistance) {
				// store current offset
				// for next shift
				shiftX = (int) -back.x;
				shiftY = (int) -back.y;							
			} else {
				shiftX = 0;
				shiftY = 0;				
			}

			// update focus position
			int cfx = (int) Math.round(sx);
			int cfy = (int) Math.round(sy);
			if ((cfx != focusX) || (cfy != focusY)) {
				focusX = cfx;
				focusY = cfy;
				log.info("Set Focus: " + focusX + "/" + focusY);
			}
		}
	}
	
	public int getFocusX(){
		return focusX;
	}
	
	public int getFocusY(){
		return focusY;
	}

	/**
	 * add an image
	 * 
	 * @param i
	 */
	private void addImage(GridImage i) {
		// store image
		int id = i.getFotoliaId();
		imageLayer.add(i);
		imageMap.put(id, i);

		// set priority
		int x2, y2;
		x2 = width / 2 - i.getX();
		y2 = height / 2 - i.getY();
		double d = Math.sqrt(x2 * x2 + y2 * y2);

		int prio = (int) (10.0 * (d / (Math.max(width, height))));

		log.debug("Added: " + i + " // PRIO=" + prio);

		// load texture with priority
		i.loadTexture(prio);
	}

	/**
	 * add image
	 * 
	 * @param image
	 */
	public void updateImage(ContainerImage image) {
		// get image
		GridImage gridImage = imageMap.get(image.getFotoliaId());

		// it is an new image
		if (gridImage == null) {
			// create
			gridImage = new GridImage(image);

			setPosition(gridImage, image.getX(), image.getY());

			// add image
			addImage(gridImage);
		} else {			
			setPosition(gridImage, image.getX(), image.getY());
		}

	}

	private void setPosition(GridImage gi, int x, int y) {
		gi.setPosition(x, y);
	}

	/**
	 * remove an image by its id
	 * 
	 * @param id
	 */
	public void removeImage(Integer id) {
		GridImage gi = imageMap.remove(id);
		if (gi != null) {
			imageLayer.remove(gi.getId());
			imageMap.remove(id);

			// remove texture
			TextureManager.getInstance().removeTexture(id.toString());
		}
	}

	public KNode getImageLayer() {
		return imageLayer;
	}

	public Collection<GridImage> getImages() {
		return imageMap.values();
	}

	public boolean hasImage(Integer fotoliaId) {
		return imageMap.containsKey(fotoliaId);
	}

	public Vector3d getIntersectionPoint() {
		return intersectionPoint;
	}
}
