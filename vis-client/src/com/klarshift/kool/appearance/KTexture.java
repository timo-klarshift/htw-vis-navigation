package com.klarshift.kool.appearance;

import javax.media.opengl.GL2;
import javax.media.opengl.GLProfile;

import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

/**
 * texture
 * @author timo
 *
 */
public class KTexture {
	private TextureData textureData;
	private boolean loaded = false, enabled = true;
	static GLProfile profile = GLProfile.getDefault();
	private Texture texture;	
	
	/**
	 * create texture
	 */
	public KTexture(){
				
	}
	
	/**
	 * get the texture
	 * @return
	 */
	private Texture getTexture(){
		if(texture == null && textureData != null){
			texture = TextureIO.newTexture(textureData);
		}
		return texture;
	}
	
	/**
	 * create texture from texture data
	 * @param data
	 */
	public KTexture(TextureData data){
		setTextureData(data);		
	}
		
	/**
	 * enable texture
	 * @param e
	 */
	public void setEnabled(boolean e){
		this.enabled = e;
	}	
	
	/**
	 * enable state
	 * @return
	 */
	public boolean isEnabled(){
		return enabled;
	}
	
	/**
	 * loaded state
	 * @return
	 */
	public boolean isLoaded(){
		return loaded;
	}
	
	/**
	 * set texture data to texture
	 * @param data
	 */
	public void setTextureData(TextureData data){
		if(data != null){
			textureData = data;
			loaded = true;
		}
	}
	
	/**
	 * apply the texture
	 * @param gl
	 */
	public void apply(GL2 gl){
		if(loaded && enabled){	
			// enable texture mode
			gl.glEnable(GL2.GL_TEXTURE_2D);
			gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE,
					GL2.GL_REPLACE);
			
			// get texture, enable and bind it
			texture = getTexture();
			texture.enable(gl);
			texture.bind(gl);
		}
	}
}
