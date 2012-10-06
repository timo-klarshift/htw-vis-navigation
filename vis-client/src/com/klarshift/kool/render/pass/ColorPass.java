package com.klarshift.kool.render.pass;

import javax.media.opengl.GL2;

import com.klarshift.kool.camera.Camera;
import com.klarshift.kool.render.ARenderPass;
import com.klarshift.kool.render.SceneRenderer;
import com.klarshift.kool.scenegraph.KNode;

/**
 * color pass
 * default rendering pass
 * @author timo
 *
 */
public class ColorPass extends ARenderPass {
	public ColorPass() {
		super(ARenderPass.PASS_COLOR);
	}

	@Override
	public void enable(GL2 gl) {
		// clear color buffer
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
		gl.glEnable(GL2.GL_DEPTH_TEST);
	}

	@Override
	public void disable(GL2 gl) {
		// nothing to do here ...
	}
}
