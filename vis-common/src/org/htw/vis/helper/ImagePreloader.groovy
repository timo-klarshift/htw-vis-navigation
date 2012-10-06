package org.htw.vis.helper

import java.awt.image.BufferedImage
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO

import org.htw.vis.gui.Image




/**
 * image preloader
 * 
 * is able to preload images with several 
 * threads
 * 
 * @author timo
 *
 */
class ImagePreloader {
	/* thread count */
	static final int THREAD_COUNT = 24
	
	private File cacheDir = null;
	
	/**
	 * create image preloader
	 */
	public ImagePreloader(){
		cacheDir = new File(getCacheBase())
		if(cacheDir.exists() == false){
			cacheDir.mkdirs();
		}
	}
	
	/**
	 * get the cache base
	 * @return
	 */
	public String getCacheBase(){
		return "/tmp"
	}
	
	public File getImageFile(Image image){
		return getImageFile(image.id);
	}
	
	public File getImageFile(Long id){
		return new File("${getCacheBase()}/fotolia-$id")
	}
	
	public void downloadImage(URL imageUrl, File file){		
		try{
			BufferedImage img = ImageIO.read(imageUrl)
			ImageIO.write(img, "jpg", file);			
		}catch(Exception e){
			e.printStackTrace()
		}
	} 	
	 
	public void preload(ArrayList<Image> images){
		try{
			System.out.println("DOWNLOAD " + images.size());
			int threads = Math.min(THREAD_COUNT, images.size())
			
			// split images into equal bins (one bin per thread)
			def imageMap = [:]		
			for(int i=0; i<images.size(); i++){
				int x = i % threads
				if(!imageMap[x]){
					imageMap[x] = []
				}
				imageMap[x] << images[i]
			}		
			
			Integer td = 0
			int total = 0
			(0..(threads-1)).each{ final int ti ->
				Thread.start{
					imageMap[ti].each{ Image image ->
						File imageFile = getImageFile(image);
						//BufferedImage img = null
						if(!imageFile.exists()){
							// download image
							downloadImage(new URL(image.getUrl()), imageFile);												
						}
						
						total ++					
					}
					
					imageMap[ti] = null
					
					td++
				}
			}
			
			while(imageMap.findAll{it.value != null}.size() > 0){
				Thread.sleep(100)						
			}	
			
			println "DONE"	
		}catch(Exception e){
			println e.message
		}
	}
}
