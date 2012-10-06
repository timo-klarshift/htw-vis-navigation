package org.htw.vis.server.context;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.htw.vis.clustering.TrainSet;
import org.htw.vis.clustering.feature.ByteFeatureFactory;
import org.htw.vis.clustering.feature.Feature;
import org.htw.vis.clustering.som.SOM;
import org.htw.vis.clustering.som.SOMNode;
import org.htw.vis.helper.FeatureAccess;
import org.htw.vis.layer.NetworkNode;
import org.htw.vis.layer.ZoomLayer;

/**
 * image container holding the current images in container
 * 
 * @author timo
 * 
 */
public class ImageContainer {
	
	/* image holder */
	private final LinkedHashMap<Integer, ContainerImage> images = new LinkedHashMap<Integer, ContainerImage>();
	private ContainerImage[][] imageMap;
	
	private final LinkedList<Integer> removed = new LinkedList<Integer>();
	
	
	//private final LinkedHashMap<String, ContainerImage> map = new LinkedHashMap<String, ContainerImage>();
	private final TrainSet trainSet = new TrainSet();
	private LinkedList<ContainerImage> contextImages = new LinkedList<ContainerImage>();
	
	private final FeatureAccess featureAccess = new FeatureAccess();
	
	/* logging */
	private final Logger log = Logger.getLogger("ImageContainer");
	
	/* dimension */
	private int width;
	private int height;
	
	private SOM som;
	
	private ServerContext ctx;
	

	/**
	 * create container
	 * @param name
	 * @param width
	 * @param height
	 */
	public ImageContainer(ServerContext context, int width, int height) {	
		this.ctx = context;
		setSize(width, height);
	}	
	
	/**
	 * set the container size
	 * @param width
	 * @param height
	 */
	private void setSize(int width, int height){
		this.width = width;
		this.height = height;
		
		imageMap = new ContainerImage[height][width];		
		
		// create the som
		som = SOM.create(width, height, new ByteFeatureFactory(60));
		log.info("Set container size " + width + "x" + height );
	}
	
	public Integer getWidth(){return width;}
	public Integer getHeight(){return height;}
	
	/**
	 * get max capacity
	 * @return
	 */
	public final int getCapacity(){
		return width*height;
	}
	
	/**
	 * get image count
	 * @return
	 */
	public final int count(){
		return images.size();
	}	
	
	public void addImages(List<ContainerImage> images) {
		addImages(images, true);
	}


	/**
	 * add images to container
	 * @param images
	 */
	public void addImages(List<ContainerImage> images, boolean retrain) {
		if(som == null){
			log.error("Cant add images without a size set to container.");
			return;
		}
		
		TrainSet tmpSet = new TrainSet();
		
		// add all images
		for (ContainerImage i : images) {
			if(!hasImage(i.getFotoliaId())){
				tmpSet.addSample(getImageFeature(i.getFotoliaId()));
				addImage(i);
				
				if(count() >= getCapacity()){
					log.info("Stopped adding images, exceeded volume!");
					break;
				}
			}
		}		
		
		//som.train(trainSet, 12, (int)(width), 0.1f);
		if(retrain)
			som.train(tmpSet, 12, (int)(width), 0.1f);
			//som.train(trainSet, 12, (int)(width), 0.1f);
				
		map();
	}
	
	public void shiftZ(int z){
		log.info("Shifting Container Z: " + z);
		
		ContainerImage[][] tmp = new ContainerImage[height][width];
		ContainerImage cur;
					
		ZoomLayer layer = ctx.getLayer();
		
		LinkedList<ContainerImage> results = new LinkedList<ContainerImage>();

		// add children
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				cur = imageMap[y][x];
				if(cur != null){
					if(y % 2 == 0 && x % 2 == 0 || true){
						
						
						// add children
						if(z < 0 && layer.getLOD() > 0){
							for(NetworkNode ni : layer.getChildren(cur.getFotoliaId())){
								if(ni.getFotoliaId() != cur.getFotoliaId())									
									results.add(new ContainerImage(ni));														
							}
						}
						
						// add parents
						if(z > 0 && layer.higher() != null){
							NetworkNode child = layer.getNodeById(cur.getFotoliaId());
							if(child != null){
								Integer pId = child.getParentId();
								if(pId != null){
										
									NetworkNode parent = layer.higher().getNodeById(pId);								
									if(parent != null){
										results.add(new ContainerImage(parent));
										// remove old when differes from parent
										if(cur.getFotoliaId() != parent.getFotoliaId())
											removeImage(cur);
									}
									
									
								}
							}
							
						}
						
						
						
					}
				}
			}
		}
		
		addImages(results, true);
		
		
		
		map();
	}
	
	/**
	 * shift the container
	 * @param x
	 * @param y
	 */
	public void shift(int dx, int dy){ // screen coords
		log.info("Shifting Container " + dx + "/" + dy);
		dy *= -1;
					
		ContainerImage[][] tmp = new ContainerImage[height][width];
		ContainerImage cur;
					
		// loop over imageMap
		int tx, ty;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				cur = imageMap[y][x];
				if(cur != null){
				
					// get position in tmp
					tx = x + dx;
					ty = y + dy;
					
					if (tx >= 0 && ty >= 0 && tx < width && ty < height) {	
						tmp[ty][tx] = imageMap[y][x];
						tmp[ty][tx].map(tx, ty);
					} else{						
						removeImage(cur);						
					}	
				}
			}
		}
		
		imageMap = tmp;
		
		
	
		
		// shift som & remap
		som.shift(dx, dy);		
		
		
		map();
		
		
	}
	

	
	/**
	 * get context images
	 * @return
	 */
	public LinkedList<ContainerImage> getContextImages(){		
		return contextImages;
	}
	
	/**
	 * update context
	 */
	public void updateContext(){
		int w2 = ctx.x != -1 ? ctx.x : width/2;
		int h2 = ctx.y != -1 ? ctx.y : height/2;
		
		int r = 1;
		
		LinkedList<ContainerImage> newContext = new LinkedList<ContainerImage>();
		
		log.info("Updating context.");
		for(int y=h2-r; y<=h2+r; y++){
			for(int x = w2-r; x<w2+r; x++){
				if(x >= 0 && y >= 0 && x < width && y < height){
					double d = Math.sqrt((w2-x)*(w2-x)+(h2-y)*(h2-y));
					if(d <= r){					
						ContainerImage ci = imageMap[y][x];
						if(ci != null){
							log.info("\t + " + ci.getWords());
							newContext.add(ci);
						}
					}
				}
			}
		}
		
		if(newContext.size() > 0){
			contextImages = newContext;
		}
	}
	
	
	/**
	 * map all images
	 */
	public void map(){
		// mapping means retrieving positions
		// for all container images
		
		log.info("Mapping images.");
		log.info("\tCount: " + images.size());
		log.info("\tLOCK: " + som.getLockCount());
		
		
		
		for(ContainerImage ci : images.values()){
			if(ci.isMapped() == false){				
				SOMNode bmu = som.getFreePosition(getImageFeature(ci.getFotoliaId()));
				if(bmu != null){
					int x = bmu.getX();
					int y = bmu.getY();
					
					som.lock(x, y);
					
					ci.map(x, y);
					
					assert imageMap[y][x] == null; 
					imageMap[y][x] = ci;
					
					//log.info("MAPPED " + ci);
				}else{
					log.error("NO BMU=?");
					//removeImage(ci);
				}
			}
			
		}			
		
		log.info("Mapped images:");
		log.info("\tCount: " + images.size());
		log.info("\tLOCK: " + som.getLockCount());
		
				
		
		// update context
		updateContext();
	}
	
	


	/**
	 * check for image by its id
	 * @param id
	 * @return
	 */
	public boolean hasImage(Integer id) {
		return images.containsKey(id);
	}

	/**
	 * internal image setter
	 * @param i
	 */
	private void addImage(ContainerImage i) {		
		if (images.containsKey(i.getFotoliaId()) == false) {
			
			if(images.size() < getCapacity()){
				log.debug("Add image to container. ts=" + trainSet.size() + " / container=" + images.size());
				images.put(i.getFotoliaId(), i);
				trainSet.addSample(getImageFeature(i.getFotoliaId()));
			}else{
				log.info("Reached Capacity. Cant add image " + i);
			}
		}
	}
	
	private Feature getImageFeature(Integer id){
		Feature f = featureAccess.getFeature(id);
		f.setId(id.toString());
		return f;
	}
	
	private Feature getImageFeature(ContainerImage image){
		return getImageFeature(image.getFotoliaId());
	}
	
	/**
	 * remove image by id
	 * @param i
	 */
	public void removeImage(ContainerImage image){
		Feature feature = getImageFeature(image);
		if(feature != null)
			trainSet.removeSample(feature);
		
		images.remove(image.getFotoliaId());
		
		
		
		// UNLOCK
		som.unlock(image.getX(), image.getY());
		
		removed.add(image.getFotoliaId());
		
		log.info("Removed image " + image);			
		
	}
	
	public LinkedList<Integer> getRemovedImages(){
		return removed;
	}
	
	

	/**
	 * get all images
	 * @return
	 */
	public Collection<ContainerImage> getImages() {
		return  images.values();
	}

	/**
	 * clear container
	 */
	public void clear() {
		log.info("CLEARED");
		trainSet.clear();
		images.clear();
		
		
		som.resetMapping();		
		som.randomize();
		imageMap = new ContainerImage[height][width];
		
		assert trainSet.size() == 0;
		assert som.getLockCount() == 0;
	}
}
