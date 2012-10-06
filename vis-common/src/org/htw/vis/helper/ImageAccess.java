package org.htw.vis.helper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

import org.htw.vis.layer.NetworkNode;

import com.spaceprogram.kittycache.KittyCache;

public class ImageAccess {
	/* feature cache */
	private static final int CACHE_SIZE = 1024;
	private static final int CACHE_TIME = 30;

	private static final KittyCache<Integer, BufferedImage> cache = new KittyCache<Integer, BufferedImage>(
			CACHE_SIZE);
	private static final File cachePath = new File("../cache/images/");

	/* executor */
	private LinkedBlockingQueue<Runnable> loadQueue = new LinkedBlockingQueue<Runnable>();
	private final ThreadPoolExecutor executor;
	private final AtomicInteger imagesLeft = new AtomicInteger();

	public ImageAccess() {
		executor = new ThreadPoolExecutor(12, 12, 15, TimeUnit.SECONDS, loadQueue);

		// init
		if (cachePath.exists() == false) {			
			cachePath.mkdirs();
		}
	}

	public void preload(NetworkNode node) {
		//
		if (hasImage(node.getFotoliaId()) == true) {

		} else {
			imagesLeft.incrementAndGet();
			executor.execute(new LoadTask(node));
		}
	}

	public String getImagePath(int id) {
		int n = 8;
		int m = (int) Math.pow(2, n);

		int a = id >> n;
		int b = a >> n;
		int j = a > m ? a % m : a;

		return (cachePath.getAbsolutePath() + "/" + b + "/" + j + "/" + id + ".jpg");
	}

	public boolean hasImage(int id) {
		// TODO: check cache
		if (cache.get(id) != null) {
			return true;
		}

		// check file
		return getImageFile(id).exists();
	}

	public File getImageFile(int id) {
		return new File(getImagePath(id));
	}
	
	public BufferedImage getImage(Integer id, String path){
		// check cache
		BufferedImage bi = cache.get(id);
		if (bi != null) {
			return bi;
		}

		// check disk
		File f = getImageFile(id);
		if (f.exists()) {
			try {
				bi = ImageIO.read(f);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (bi != null) {
				// cache in memory
				cache.put(id, bi, CACHE_TIME);
				return bi;
			}
		}

		// check online server
		bi = download(id, path);
		if(bi != null){
			// cache in memory
			cache.put(id, bi, CACHE_TIME);
		}

		return bi;
	}

	public BufferedImage getImage(NetworkNode node) {		
		return getImage(node.getFotoliaId(), node.getImageSource());		
	}
	
	public BufferedImage download(Integer id, String url){		
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(new URL(url));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (bi != null) {			
			// cache to disk
			try {
				File ifile = new File(getImagePath(id));
				if(!ifile.getParentFile().exists()){
					ifile.getParentFile().mkdirs();
				}
				ImageIO.write(bi, "jpg", ifile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return bi;
	}

	public void preload(List<NetworkNode> nodes) {
		for (NetworkNode n : nodes) {
			preload(n);
		}
	}

	public void onImageLoaded(NetworkNode n) {
		imagesLeft.decrementAndGet();
		System.out.println("Loaded image " + n);
	}

	public boolean isLoading() {
		return imagesLeft.get() > 0;
	}

	private class LoadTask implements Runnable {
		private NetworkNode node;		

		public LoadTask(NetworkNode node) {
			this.node = node;
		}

		@Override
		public void run() {
			//
			download(node.getFotoliaId(), node.getImageSource());

			// done
			onImageLoaded(node);
		}
	}
}
