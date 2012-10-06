package com.klarshift.kool.render.pass;

import java.nio.ByteBuffer;
import java.util.HashMap;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.GLBuffers;
import com.klarshift.kool.render.ARenderPass;
import com.klarshift.kool.render.SceneRenderer;
import com.klarshift.kool.scenegraph.KNode;
import com.klarshift.kool.scenegraph.VisualNode;

/**
 * region picking pass
 * @author timo
 *
 */
public class RegionPickingPass extends ARenderPass {
	int pickX, pickY;
	
	private final HashMap<String,Integer> nodeColorMap = new HashMap<String,Integer>();
	private final HashMap<Integer,String> colorNodeMap = new HashMap<Integer,String>();
	private ByteBuffer color;
	private int selectedColor;
	
	private int nextColor = 1;
	
	public RegionPickingPass(){
		super(ARenderPass.PASS_PICKING);
		setPickArea(10, 10);
	}
	
	/**
	 * define the pick area
	 * @param x
	 * @param y
	 */
	public void setPickArea(int x, int y){
		pickX = x;
		pickY = y;		
		color = GLBuffers.newDirectByteBuffer(4);
	}
	
	/**
	 * set the current color for a given visual node
	 * @param gl
	 * @param node
	 */
	public void setPickColor(GL2 gl, VisualNode node){
		String id = node.getId();		
		Integer i = nodeColorMap.get(id);
		
		if(i == null){
			i = nextColor++;
			nodeColorMap.put(id, i);
			colorNodeMap.put(i, id);
		}
		
		// set pick color
        gl.glColor3ub((byte) ((i >> 0) & 0xff), (byte) ((i >> 8) & 0xff), (byte) ((i >> 16) & 0xff));		
	}
	

	@Override
	public void enable(GL2 gl) {		
		// enable scissoring
		gl.glEnable(GL2.GL_SCISSOR_TEST);
        gl.glScissor(pickX, pickY, 1, 1);
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void disable(GL2 gl) {
		// disable scissoring
		gl.glDisable(GL2.GL_SCISSOR_TEST);
        gl.glReadPixels(pickX, pickY, 1, 1, GL2.GL_RGB, GL2.GL_UNSIGNED_BYTE, color);        
        selectedColor = color.getInt(0);        
	}
	
	/**
	 * get the id of the selected node
	 * @return
	 */
	public String getSelectedNodeId(){
		return selectedColor != 0 ? colorNodeMap.get(selectedColor) : null;
	}
}
