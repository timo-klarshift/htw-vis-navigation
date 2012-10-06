package com.klarshift.kool.scenegraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import org.apache.log4j.Logger;

import com.klarshift.kool.render.RenderEngine;
import com.klarshift.kool.render.Transformation;

/**
 * base node
 * 
 * @author timo
 * 
 */
public class KNode {
	/* logging */
	protected final static Logger log = Logger.getLogger("KNode");

	/* leafs */
	private HashMap<String, KNode> leafs = new HashMap<String, KNode>();

	/* identity */
	private String name = "_unnamed";
	private final String id = UUID.randomUUID().toString();

	/* structure */
	private KNode parent;

	/* transformation */
	protected Transformation transformation;
	protected Matrix4d globalMatrix = new Matrix4d();

	/* axis */
	protected Vector3d lookAt = new Vector3d(0, 0, -1);
	protected Vector3d upVector = new Vector3d(0, 1, 0);
	protected Vector3d sideVector = new Vector3d(-1, 0, 0);

	/* state */
	private boolean visible = true;

	/**
	 * create new node
	 */
	public KNode() {

	}

	/* axis vectors */
	public Vector3d getLookAtVector() {
		return lookAt;
	}

	public Vector3d getUpVector() {
		return upVector;
	}

	public Vector3d getSideVector() {
		return sideVector;
	}

	/**
	 * get parent branch
	 * 
	 * @return
	 */
	public KNode getParent() {
		return parent;
	}

	/**
	 * set node visible state
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * is node visible
	 * 
	 * @return
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * set a current transformation
	 * 
	 * @param transformation
	 */
	public void setTransformation(Transformation transformation) {
		this.transformation = transformation;
	}

	/**
	 * get the transformation create object when not exists
	 * 
	 * @return
	 */
	public Transformation t() {
		if (transformation == null) {
			transformation = new Transformation();
		}
		return transformation;
	}

	/**
	 * get transformation
	 * 
	 * @return
	 */
	public Transformation getTransformation() {
		return transformation;
	}

	/**
	 * get the nodes id
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * create a node with given name
	 * 
	 * @param name
	 */
	public KNode(String name) {
		setName(name);
	}

	/**
	 * set the name
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * set the parent
	 * 
	 * @param parent
	 */
	public void setParent(KNode parent) {
		this.parent = parent;
	}

	/**
	 * get nodes name
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * get string representation
	 */
	public String toString() {
		String o = id;
		if (name != null) {
			o = name + " (" + o + ")";
		}
		return o;
	}

	/**
	 * get the current global Matrix
	 * 
	 * @return
	 */
	public Matrix4d getGlobalMatrix() {
		updateMatrix();
		return globalMatrix;
	}

	/**
	 * read global position
	 * 
	 * @return
	 */
	public Vector3d getGlobalPosition() {
		/*if (globalMatrix == null) {
			updateMatrix();
		}*/
				

		//if (globalMatrix != null) {
			double[] pos = new double[4];
			getGlobalMatrix().getColumn(3, pos);
			return new Vector3d(pos);
		//}
		//return null;
	}

	/**
	 * update the node
	 * 
	 * @param updateTime
	 * @param frameCount
	 * @param engine
	 */
	public void update(long updateTime, long frameCount, RenderEngine engine) {
		// update matrix
		updateMatrix();

		// update children
		Iterator<KNode> nodeIterator = leafs.values().iterator();
		KNode n = null;
		while (nodeIterator.hasNext()) {
			n = nodeIterator.next();
			n.update(updateTime, frameCount, engine);
		}	
	}	

	/**
	 * update the transformation matrix calculates the matrix given by parents
	 */
	public void updateMatrix() {
		// get local matrix
		Matrix4d localMatrix = t().getLocalMatrix();

		// get parent
		KNode parent = getParent();
		if (parent != null) {
			Matrix4d parentGlobal = parent.getGlobalMatrix();
			globalMatrix.mul(parentGlobal, localMatrix);
		} else {
			globalMatrix = localMatrix;
		}

		// update axis
		updateAxis();
	}

	/**
	 * update vectors
	 */
	private void updateAxis() {
		// update axis
		lookAt = new Vector3d(0, 0, -1);
		globalMatrix.transform(lookAt);
		lookAt.normalize();

		upVector = new Vector3d(0, 1, 0);
		globalMatrix.transform(upVector);
		upVector.normalize();

		sideVector = new Vector3d(-1, 0, 0);
		globalMatrix.transform(sideVector);
		sideVector.normalize();

	}

	/**
	 * add a child
	 * 
	 * @param child
	 */
	public void add(KNode child) {
		leafs.put(child.getId(), child);				
		child.setParent(this);
	}

	/**
	 * remove a child by its instance
	 * 
	 * @param child
	 * @return
	 */
	public void remove(KNode child) {
		remove(child.getId());
	}

	/**
	 * remove an id by its id
	 * 
	 * @param id
	 * @return
	 */
	public void remove(String id) {				
		leafs.remove(id);			
	}

	/**
	 * get a node by its id
	 * 
	 * @param id
	 * @return
	 */
	public KNode get(String id) {
		return leafs.get(id);
	}

	/**
	 * get a node by its name
	 * 
	 * @TODO: keep an index, otherwise this might be slow
	 * @param name
	 * @return
	 */
	public KNode getByName(String name) {
		for (KNode n : leafs.values()) {
			if (n.getName().equals(name)) {
				return n;
			}
		}
		return null;
	}

	/**
	 * get the leafs collection of nodes
	 * 
	 * @return
	 */
	public Collection<KNode> getLeafs() {
		return leafs.values();
	}
}
