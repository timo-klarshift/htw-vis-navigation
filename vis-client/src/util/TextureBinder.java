package util;

import javax.media.opengl.GL2;

import com.jogamp.opengl.util.texture.Texture;

/**
 * texture binder helper
 * 
 * @author timo
 *
 */
public class TextureBinder {
	static int bindings = 0;
	static Texture lastBound = null;
	
	/**
	 * prevent binding a texture twice
	 * in two sequential calls
	 * 
	 * @param gl
	 * @param texture
	 */
	public static void bind(GL2 gl, Texture texture){
		if(lastBound != null && lastBound == texture){
			return;
		}
		
		lastBound = texture;		
		texture.bind(gl);
	}
}
