package com.klarshift.kool.render;

import java.util.Iterator;
import java.util.LinkedList;

import javax.media.opengl.GL2;
import javax.vecmath.Matrix4d;

import com.klarshift.kool.camera.Camera;
import com.klarshift.kool.scenegraph.KNode;
import com.klarshift.kool.scenegraph.VisualNode;

/**
 * scene renderer
 * 
 * @author timo
 * 
 */
public class SceneRenderer {

	private RenderEngine engine;

	/* visual node keeper */
	private LinkedList<VisualNode> visuals = new LinkedList<VisualNode>();

	private boolean updateNeeded = true;

	public SceneRenderer(RenderEngine engine) {
		this.engine = engine;
	}

	public void update() {
		updateNeeded = true;
	}

	public void renderScene(GL2 gl, KNode branch) {
		// enter model view matrix
		gl.glMatrixMode(GL2.GL_MODELVIEW);

		if (branch.isVisible() == false)
			return;

		if (updateNeeded) {
			// collect nodes
			visuals.clear();
			collectNodes(branch);
		}

		// TODO: sort nodes, when transformations changed
		// TODO: split in transparent / opaque

		// render nodes
		for (VisualNode n : visuals) {
			gl.glPushMatrix();
				renderVisualNode(gl, n);
			gl.glPopMatrix();
		}
	}

	/**
	 * collect all nodes
	 * 
	 * @param branch
	 */
	private void collectNodes(KNode branch) {
		Camera cam = engine.getCamera();
		
		
		Iterator<KNode> nodeIterator = branch.getLeafs().iterator();
		while (nodeIterator.hasNext()) {
			KNode n = nodeIterator.next();
			
			n.updateMatrix();

			if (n.isVisible()) {
				if (n instanceof VisualNode) {
					if (cam == null
							|| (cam != null && cam.isVisible((VisualNode) n))) {
						visuals.add((VisualNode) n);
					}
				}

				collectNodes(n);
			}
		}

	}

	/**
	 * render visual node
	 * 
	 * @param gl
	 * @param node
	 */
	private void renderVisualNode(GL2 gl, VisualNode node) {
		
		// dont render when picking mode is
		// enabled and node is not pickable
		ARenderPass cp = engine.getCurrentPass();
		if (cp.getId() == ARenderPass.PASS_PICKING && !node.isPickable()) {
			return;
		}

		// load current matrix
		Matrix4d m = node.getGlobalMatrix();
		gl.glLoadMatrixd(new double[] { m.m00, m.m10, m.m20, m.m30, m.m01,
				m.m11, m.m21, m.m31, m.m02, m.m12, m.m22, m.m32, m.m03, m.m13,
				m.m23, m.m33, }, 0);

		// render the node with respect to current pass
		node.renderNode(gl, engine);
	}

	public int getVisibleCount() {
		return visuals.size();
	}
}
