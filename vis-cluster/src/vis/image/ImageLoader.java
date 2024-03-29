package vis.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class ImageLoader {
	private final BlockingQueue<Runnable> urlQueue = new LinkedBlockingQueue<Runnable>(new PriorityBlockingQueue<Runnable>());
	private ThreadPoolExecutor urlPool;
	
	private final BlockingQueue<Runnable> fileQueue = new LinkedBlockingQueue<Runnable>(new PriorityBlockingQueue<Runnable>());
	private ThreadPoolExecutor filePool;
	
	private HashMap<String,CacheObject> cache = new HashMap<String,CacheObject>();
	private Logger log = Logger.getLogger(this.getClass());
	
	LinkedList<LoadCallback> callbacks = new LinkedList<LoadCallback>();
	
	public void addCallback(LoadCallback cb){
		callbacks.add(cb);
	}
	
	
 
    

	public ImageLoader(){
		int corePoolSize = 2;
		int maxPoolSize = 4;
		int keepAliveTime = 10; // very long
		
		urlPool = new ThreadPoolExecutor(corePoolSize, maxPoolSize,
                keepAliveTime, TimeUnit.HOURS, urlQueue);		
		
		filePool = new ThreadPoolExecutor(corePoolSize, maxPoolSize,
                keepAliveTime, TimeUnit.HOURS, fileQueue);
	}
	
	public void clearCache(){
		cache.clear();
	}
	
	public BufferedImage getImageForUrl(String url){
		CacheObject cached = cache.get(url);
		if(cached != null)return cached.bi;							
		return null;
	}
	
	public void preload(String url){
		if(cache.containsKey(url)){
			return;
		}
		
		// queue in file pool
		File diskFile = new File(getImagePathForUrl(url));
		if(diskFile.exists()){
			filePool.execute(new LoadFromFileTask(diskFile, url));
			return;
		}
		
		// preload by url
		urlPool.execute(new LoadFromUrlTask(url));		
	}	
	
	public int remaining(){
		return urlQueue.size() + fileQueue.size();
	}
	
	public String getImagePathForUrl(String url){
		return "../../" + url.substring(20);
	}
	
	private void _loadFromFile(File file, String url){
		if(cache.containsKey(url) == true){			
			return;
		}
		
		log.debug("Loading " + file);
		
		BufferedImage bi;
		try {
			bi = ImageIO.read(file);
			if(bi != null){
				cache(url, bi);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void cache(String url, BufferedImage bi){
		// inform callback
		cache.put(url, new CacheObject(url , bi));
		for(LoadCallback lb : callbacks){
			lb.onImage(cache.get(url));
		}
	}
	
	private void _loadFromUrl(String url){
		if(cache.containsKey(url) == true){			
			return;
		}
		
		BufferedImage bi;
		try {
			log.debug("Preloading ... " + url);
			
			
			
			
			// read from url
			bi = ImageIO.read(new URL(url));
			if(bi != null){
				// write to temp file
				File tmpFile = File.createTempFile("preload", "");										
				ImageIO.write(bi, "jpg", tmpFile);
				
				// move to folder
				File diskFile = new File(getImagePathForUrl(url));
				File parent = diskFile.getParentFile();
				if(parent.exists() == false){
					if(!parent.mkdirs()){
						log.error("Could not create dir: " + parent);
					}
				}
				
				try{				
					FileUtils.moveFile(tmpFile, diskFile);
					cache(url, bi);					
				}catch(Exception e){
					log.error(e.getMessage());
				}finally{
					if(tmpFile.exists()){
						tmpFile.delete();
					}
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	private class LoadFromUrlTask implements Runnable , Comparable<LoadFromUrlTask>{
		private String url;
		private Long timestamp;
		public LoadFromUrlTask(String url){
			this.url = url;
			this.timestamp = new Date().getTime();
		}

		@Override
		public void run() {
			_loadFromUrl(url);				
		}
		
		@Override
		public int compareTo(LoadFromUrlTask o) {
			return timestamp.compareTo(o.timestamp);
		}
		
	}
	
	private class LoadFromFileTask implements Runnable, Comparable<LoadFromFileTask>{
		public String url;
		private File file;
		public Long timestamp;
		
		public LoadFromFileTask(File file, String url){
			this.url = url;
			this.file = file;
			this.timestamp = new Date().getTime();
		}

		@Override
		public void run() {
			_loadFromFile(file, url);				
		}

		@Override
		public int compareTo(LoadFromFileTask o) {
			return timestamp.compareTo(o.timestamp)*-1;
		}
		
	}
	
	public class CacheObject {
		public BufferedImage bi;
		public String url;
		
		public CacheObject(String url, BufferedImage image){
			this.bi = image;
			this.url = url;
		}
	}
	
	public interface LoadCallback {
		public void onImage(CacheObject o);
	}

}
