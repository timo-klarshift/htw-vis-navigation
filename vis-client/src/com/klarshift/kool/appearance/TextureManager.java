package com.klarshift.kool.appearance;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.media.opengl.GLProfile;

import org.apache.log4j.Logger;

import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.TextureIO;

/**
 * texture manager
 * @author timo
 *
 */
public class TextureManager {
	/* instance */
	private static TextureManager instance;
	
	static GLProfile profile = GLProfile.getDefault();

	private PriorityBlockingQueue<Runnable> loadQueue = new PriorityBlockingQueue<Runnable>();
	private HashMap<String,Runnable> loadMap = new HashMap<String, Runnable>();
	
	private final ThreadPoolExecutor executor;
	
	private static final Logger log = Logger.getLogger("TextureManager");
	
	private HashMap<String,KTexture> textures = new HashMap<String,KTexture>(5000);

	private TextureManager() {
		// create executor
		executor = new ThreadPoolExecutor(32, 64, 15, TimeUnit.SECONDS,
				loadQueue);
	}

	public static TextureManager getInstance() {
		if (instance == null) {
			instance = new TextureManager();
		}
		return instance;
	}
	
	public void addTexture(String id, URL url, String ex){
		if(!hasTexture(id))
			executor.execute(new TextureTask(id, url, ex, 0));
	}
	
	public void addTexture(String id, URL url, String ex, Integer priority){
		if(!hasTexture(id))
			executor.execute(new TextureTask(id, url, ex, priority));
	}
	

	private void onTextureLoaded(String id, KTexture texture) {
		textures.put(id, texture);
		loadMap.remove(id);
		log.debug("Texture loaded " + id + " / Having " + textures.size());
	}
	
	public boolean hasTexture(String id){
		return textures.containsKey(id);
	}
	
	public KTexture removeTexture(String id){
		return textures.remove(id);
	}
	
	public KTexture getTexture(String id){
		return textures.get(id);
	}

	public TextureData loadTextureDataFromFile(File file, String extension) {
		try {
			log.debug("Load " + file);
			return TextureIO.newTextureData(profile, file, false, extension);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public TextureData loadTextureDataFromURL(URL url, String extension) {
		try {
			log.debug("Load " + url);
			return TextureIO.newTextureData(profile, url, false, extension);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private class TextureTask implements Runnable, Comparable<TextureTask> {
		private static final int URL = 0;
		private static final int FILE = 1;
		private int type;
		private File file;
		private String extension;
		private URL url;
		private String id;
		private Integer priority = 0;
		
		public Integer getPriority(){
			return priority;
		}

		public TextureTask(String id, File file, String ex) {
			this.id = id;
			this.file = file;
			this.extension = ex;
			type = FILE;
			
			loadMap.put(id, this);
		}

		public TextureTask(String id, URL url, String ex) {
			this.id = id;
			type = URL;
			this.extension = ex;
			this.url = url;
			
			loadMap.put(id, this);
		}
		
		public TextureTask(String id, URL url, String ex, int priority) {
			this.id = id;
			this.priority = priority;
			type = URL;
			this.extension = ex;
			this.url = url;
			
			loadMap.put(id, this);
		}

		@Override
		public void run() {
			// texture has been removed
			// dont load it
			if(loadMap.containsKey(id) == false){
				
				return;
			}
			
			KTexture texture = new KTexture();
			TextureData td = null ;
			if(type == FILE){
				td = loadTextureDataFromFile(file, extension);
				
			}else if(type == URL){
				td = loadTextureDataFromURL(url, extension);				
			}
			
			if(td != null){
				texture.setTextureData(td);			
				onTextureLoaded(id, texture);
				
			}else{
				log.error("Could not load texture: " + id + "/" + file + "/" + url);
			}
			
			loadMap.remove(id);			
		}

		@Override
		public int compareTo(TextureTask o) {
			return priority.compareTo(o.getPriority());
		}
	}

	public int count() {
		return textures.size();
	}

	
}
