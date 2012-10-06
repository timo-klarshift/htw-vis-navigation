package org.htw.vis.clustering.som;

import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.htw.vis.clustering.TrainSet;
import org.htw.vis.clustering.feature.Feature;
import org.htw.vis.clustering.feature.FeatureFactory;

/**
 * self organizing map
 * 
 * @author timo
 * 
 */
public class SOM {
	/* dimension */
	private final int width, height;
	private int mapRadius;
	private final float startLearningRate = 0.2f;

	/* features */
	private final FeatureFactory featureFactory;

	/* nodes */
	// private final LinkedHashMap<String, SOMNode> nodeList = new
	// LinkedHashMap<String, SOMNode>();

	private SOMNode[][] nodes;
	private byte[][] lock;

	/* logging */
	private final Logger log = Logger.getLogger(this.getClass());

	/* listeners */
	private final LinkedList<SOMListener> listeners = new LinkedList<SOMListener>();

	/* mapping */
	// private HashMap<String, Feature> lockMap = new HashMap<String,
	// Feature>();
	// private HashMap<String, SOMNode> map = new HashMap<String, SOMNode>();

	/**
	 * som constructor
	 * 
	 * @param width
	 * @param height
	 * @param featureFactory
	 */
	private SOM(int width, int height, FeatureFactory featureFactory) {
		// store properties
		this.width = width;
		this.height = height;
		this.featureFactory = featureFactory;

		// create node array
		nodes = new SOMNode[height][width];
		lock = new byte[height][width];

		// init som
		init();
	}

	/**
	 * init the som
	 */
	private void init() {
		log.info("Init SOM :: " + width + "x" + height);

		// clear lists
		// nodeList.clear();

		// calculate map radius
		mapRadius = (int) Math.round(Math.max(width, height) * 0.5);

		// create some nodes for each position
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				nodes[y][x] = new SOMNode(x, y, featureFactory.generate());
				lock[y][x] = 0;
			}
		}
	}

	public int getMapRadius() {
		return mapRadius;
	}

	/**
	 * randomize som
	 */
	public void randomize() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				nodes[y][x].randomize();
			}
		}
	}

	/**
	 * add som listener
	 * 
	 * @param listener
	 */
	public void addListener(SOMListener listener) {
		listeners.add(listener);
	}

	public void shift(int dx, int dy) {
		SOMNode[][] tmpNodes = new SOMNode[height][width];
		byte[][] tmpLocks = new byte[height][width];
		

		// loop over tmpNodes
		int tx, ty;
		SOMNode cur;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				cur = null;
				
				// get original position
				tx = x - dx;
				ty = y - dy;
				
				if (tx >= 0 && ty >= 0 && tx < width && ty < height) {
					cur = nodes[ty][tx];
					tmpLocks[y][x] = lock[ty][tx]; // keep lock
				} else {
					cur = new SOMNode(x, y, featureFactory.generate());					
					tmpLocks[y][x] = 0;	
				}
				
				

				// store at new position
				
				cur.setPosition(x, y);
				
				tmpNodes[y][x] = cur;
				assert x == cur.getX();
				assert y == cur.getY();
			}
		}
		
		nodes = tmpNodes;
		lock = tmpLocks;
		
		for(int y=0; y<height; y++){
			for(int x=0; x<width; x++){
				//System.out.println(getNode(x,  y));
			}
		}

		
		
		//printLocks();
	}
	
	public void printLocks(){
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				System.out.print(lock[y][x]);
			}
			
			System.out.println("");
		}
	}
	
	public int getLockCount(){
		int lc=0;
		for(int y=0; y<height; y++){
			for(int x=0; x<width; x++){
				if(lock[y][x] == 1)
					lc++;
			}
		}
		return lc;
	}

	/**
	 * create a som
	 * 
	 * @param width
	 * @param height
	 * @param ff
	 * @return
	 */
	public static SOM create(int width, int height, FeatureFactory ff) {
		return new SOM(width, height, ff);
	}

	/**
	 * get width
	 * 
	 * @return
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * get height
	 * 
	 * @return
	 */
	public int getHeight() {
		return height;
	}

	public void unlockAll() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				unlock(nodes[y][x]);
			}
		}
	}

	/**
	 * train the som with a given train set and specified iterations
	 * 
	 * @param set
	 * @param iterations
	 */
	public void train(TrainSet set, int iterations) {
		train(set, iterations, mapRadius, startLearningRate);
	}

	/**
	 * train the som
	 * 
	 * @param set
	 * @param iterations
	 * @param startLearningRate
	 */
	public void train(TrainSet set, int iterations, int radiance,
			float startLearningRate) {
		log.info("Training SOM with " + set.getSamples().size() + " ...");

		radiance = Math.max(radiance, 1);

		double maxDistance;
		float learnRate;
		SOMNode bmu, next;

		double distance, influence;

		int ux, uy;

		Feature currentSample;

		// get current time constant
		double timeConstant = iterations / Math.log(radiance);

		// main iteration loop
		for (int e = 0; e < iterations; e++) {
			learnRate = (float) (startLearningRate * Math
					.exp(-(double) (e / (double) iterations)));

			int r = (int) (radiance * Math.exp(-(double) e / timeConstant)); // radius

			// get max distance
			maxDistance = Math.sqrt(2 * r * r);

			log.debug("** Iteration " + e + " :: lr=" + learnRate + " :: r="
					+ r);

			for (int s = 0; s < set.getSamples().size(); s++) {
				// get current sample
				// and its best matching unit
				currentSample = set.get(s);
				bmu = getBMU(currentSample);

				// get position
				ux = bmu.getX();
				uy = bmu.getY();

				// adjust neighbors
				for (int y = uy - r; y <= uy + r; y++) {
					for (int x = ux - r; x <= ux + r; x++) {
						if (x >= 0 && x < width && y >= 0 && y < height) {
							// get node
							next = nodes[y][x];

							// dont influence nodes which are
							// locked
							if (!isLocked(next)) {

								// get euclid. space distance
								distance = Math.sqrt((ux - x) * (ux - x)
										+ (uy - y) * (uy - y));

								// the farer away the node is the
								// less influence it has
								influence = 1 - (distance / maxDistance);

								// adjust node
								next.getWeight().adjust(currentSample,
										(float) (learnRate * influence));
							}
						}
					}

				}
			}

			// inform listeners about
			// finishing iteration
			onIterationFinished(set);
		}

		onTrainingFinished(set);
	}

	/**
	 * iteration finished
	 */
	private void onIterationFinished(TrainSet set) {
		for (SOMListener l : listeners) {
			l.onIterationFinished(set);
		}
	}

	private void onTrainingFinished(TrainSet set) {
		for (SOMListener l : listeners) {
			l.onTrainingFinished(set);
		}
	}

	/**
	 * 
	 * @param sample
	 * @param single
	 * @return
	 */
	public SOMNode getBMU(Feature sample) {
		double minD = Double.MAX_VALUE, d;
		SOMNode bmu = null, n = null;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				n = nodes[y][x];
				
				assert n.getX() == x;

				d = sample.getDistance(n.getWeight());
				if (d < minD) {
					minD = d;
					bmu = n;
				}
			}
		}
		return bmu;
	}
	
	public SOMNode getFreePosition(Feature sample) {
		double minD = Double.MAX_VALUE, d;
		SOMNode bmu = null, n = null;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if(lock[y][x] == 0){
					n = nodes[y][x];	
					d = sample.getDistance(n.getWeight());
					if (d < minD) {
						minD = d;
						bmu = n;
					}
				}
			}
		}
		return bmu;
	}



	/**
	 * map features from train set to som node
	 * 
	 * @param set
	 */
	/*
	 * public void map(TrainSet set) { log.info("Mapping set with " +
	 * set.getSamples().size());
	 * 
	 * // iterate over all samples Iterator<Feature> iter =
	 * set.getSamples().iterator(); while (iter.hasNext()) { Feature f =
	 * iter.next(); if (!map.containsKey(f.getId())) {
	 * 
	 * // find non mapped node double d, minD = Double.MAX_VALUE; SOMNode bmu =
	 * null; Iterator<SOMNode> nodeIter = nodeList.values().iterator(); while
	 * (nodeIter.hasNext()) { SOMNode n = nodeIter.next(); if (!isLocked(n)) { d
	 * = f.getDistance(n.getWeight()); if (d < minD) { bmu = n; minD = d; } } }
	 * 
	 * // store mapping if (bmu != null) { map.put(f.getId(), bmu);
	 * 
	 * // lock to feature lock(f, bmu); } } }
	 * 
	 * log.info("Mapping done. " + lockMap.size() + " nodes locked."); }
	 */

	public void resetMapping() {
		unlockAll();
	}

	public void lock(int x, int y) {
		if(lock[y][x] == 0){
			lock[y][x] = 1;
		}else{
			log.error("Still locked " + x + "/" + y);
		}
	}

	public void unlock(int x, int y) {
		if(lock[y][x] == 1){
			lock[y][x] = 0;
		}else{
			log.warn("Still unlocked " + x + "/" + y);
		}
	}

	public void lock(SOMNode node) {
		lock(node.getX(), node.getY());
	}

	public void unlock(SOMNode node) {
		unlock(node.getX(), node.getY());
	}

	public boolean isLocked(SOMNode n) {
		return isLocked(n.getX(), n.getY());
	}

	public boolean isLocked(int x, int y) {
		return lock[y][x] == 1;
	}

	/**
	 * get som node for position
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public SOMNode getNode(int x, int y) {
		return nodes[y][x];
	}

	/**
	 * get all nodes
	 * 
	 * @return
	 */
	public SOMNode[][] getNodes() {
		return nodes;
	}
}
