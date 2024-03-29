package vis.clustering.quad;

import java.util.Random;

import org.apache.log4j.Logger;

import vis.db.VisualFeatures;
import vis.metric.Metric;

/**
 * quadrator quad matcher
 * 
 * finds a quadruple of images in a set of images
 * using evolutionary strategy
 * 
 * @author timo
 *
 */
public class Quadrator {
	/* config */
	private int maxIterations;
	
	/* distance matrix */
	private byte[][] finalDMatrix;
	
	/* node and quad holder */
	private int nodeCount, quadCount;
	private int[] nodeList;
	private int[] quadList;
	
	/* quad score */
	private double[] quadScore;
	private double[] nodeDistances;
	
	public static final int CLUSTER = 4;
	
	private Random random = new Random();
	
	private double totalScore = 0;
	private VisualFeatures features = new VisualFeatures();
	
	
	private Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * create quadrator
	 */
	public Quadrator(){
	}
	
	/**
	 * get current id ordering
	 * @return
	 */
	public int[] getIds(){
		int[] finalIds = new int[nodeCount];
		for(int i=0; i<nodeCount; i++){
			finalIds[i] = nodeList[quadList[i]];
		}
		return finalIds;
	}	
	
	/**
	 * retrieve quads score normalized
	 * @return
	 */
	public double[] getQuadScore(){
		return quadScore;
	}
	
	/**
	 * match 
	 * @param imageIds
	 */
	public void match(int[] imageIds){
		
		// init
		nodeCount = imageIds.length;
		quadCount = nodeCount / CLUSTER;
		if(quadCount < 1 || (nodeCount % CLUSTER != 0) ){
			log.error("ERROR: Invalid node count. = " + nodeCount);
			return;
		}
		
		// data holders		
		finalDMatrix = new byte[nodeCount][nodeCount];
		nodeList = new int[nodeCount];
		quadList = new int[nodeCount];
		nodeDistances = new double[nodeCount];
		quadScore = new double[nodeCount/CLUSTER];
		
		// store node ids in row		
		for(int g=0; g<nodeCount; g++){
			nodeList[g] = imageIds[g];
			quadList[g] = g;
		}		
		
		// calculate 		
		precalculation();
		
		// calculate score first time
		calculateScore();
		
		if(quadCount < 2){
			log.warn("Only on quad ...");
		}else{
			this.generate();	
		}						
	}
	
	/**
	 * get current total score
	 * @return
	 */
	public double getTotalScore(){
		return totalScore;
	}
	
	/**
	 * do a full score calculation
	 */
	private void calculateScore(){	
		totalScore = 0;		
		for(int q=0; q<quadCount; q++){
			quadScore[q] = getQuadScore(q);			
			totalScore += quadScore[q];	// sum to total
		}
	}	
	
	/**
	 * retrieve double encoded score
	 * @param a
	 * @param b
	 * @return
	 */
	private double _dist(int a, int b){
		double d = ((int)(finalDMatrix[a][b])+128)/255.0;		
		return (d); //[0-255]
	}
	
	/**
	 * calculate and store the quad count
	 * for a quad with given id
	 * @param i
	 * @return
	 */
	private double getQuadScore(int i){
		// get distances from all nodes to each other
		double[] meanN2M = new double[CLUSTER];
		
		// mean n-to-m distances
		int o = i*CLUSTER;
		for(int n=0; n<CLUSTER; n++){
			int ida = quadList[o+n]; // image index in image list
							
			double td = 0;	// total distance
			for(int m=0; m<CLUSTER; m++){
				int idb = quadList[o+m];
				if(ida != idb)
					td += _dist(ida, idb);
			}	
			
			td /= CLUSTER-1;
			meanN2M[n] = td;
			nodeDistances[i*CLUSTER+n] = td;
		}
		
		// mean mean n-to-m distances
		double mmeanN2M = 0;
		for(int n=0; n<CLUSTER; n++){
			mmeanN2M += meanN2M[n];
		}
		mmeanN2M /= CLUSTER;
		
		return 1.0-mmeanN2M;
	}
	
	/**
	 * select a quad group by random
	 * unequal to another index
	 * @param unequal
	 * @return
	 */
	private int selectQuad(int unequal){
		int r = unequal;	
		while(r == unequal){ r = random.nextInt(quadCount);	}		
		return r;			
	}	
		
	/**
	 * the generation loop
	 */
	private void generate(){
		maxIterations = nodeCount*nodeCount;
		
		int q1, q2;
		for(int i=0; i<maxIterations; i++){
			// randomly select two different quads
			q1 = selectQuad(-1);
			q2 = selectQuad(q1);
					
			// mutate
			mutateSwap(q1, q2);			
		}
		
		updateUI();
	}
	
	/**
	 * mutate two quads
	 * @param row
	 * @param q1
	 * @param q2
	 */
	private void mutateSwap(int q1, int q2){
		// select one node in each quad
		int n1 = q1*CLUSTER + random.nextInt(CLUSTER);
		int n2 = q2*CLUSTER + random.nextInt(CLUSTER);
		
		// swap nodes
		swapNodes(quadList, n1, n2);
		
		// calculate new score for
		// mutated quads
		double s1 = getQuadScore(q1);
		double s2 = getQuadScore(q2);
		double scoreGain = (s1+s2) - (quadScore[q1]+quadScore[q2]); 
		if(scoreGain > 0 ){ 
			quadScore[q1] = s1;
			quadScore[q2] = s2;
			totalScore += scoreGain;
		}else{
			// undo
			swapNodes(quadList, n1, n2);
		}				
	}	
	
	/**
	 * update the quad inspector when 
	 * available
	 */
	private void updateUI(){
		if(QuadInspector.instance != null){
			QuadInspector.instance.repaint();
		}
	}
	
	

	/**
	 * swap two nodes
	 * @param list
	 * @param a
	 * @param b
	 */
	private void swapNodes(int[] list, int a, int b){
		int buf = list[a];
		list[a] = list[b];
		list[b] = buf;
	}
	
	/**
	 * calculate distance matrix
	 */
	private void precalculation(){		
		// calculate max visual distance
		// this seems bad, as we do the same 
		// call below to calculate final distance matrix
		// this is only needed for normalizing visual distance 
		// to make further calculations more easier. 
		// Since this only happens once (precalculation) its no that bad.
		// reason: we want to avoid to load a double distance matrix 
		// into ram -> sequential processing
		double maxDV = 0, d;
		for(int m=0; m<nodeCount; m++){
			for(int n=m; n<nodeCount; n++){
				d = getVisualDistance(m, n, 0);
				if(d > maxDV) {
					maxDV = d;
				}
			}
		}
					
		// calculate final distance matrix
		// with normalized and byte encoded values
		double norm = 1.0/maxDV;
		for(int m=0; m<nodeCount; m++){
			for(int n=m; n<nodeCount; n++){
				d = norm * getVisualDistance(m, n, maxDV);
				finalDMatrix[n][m] = finalDMatrix[m][n] = ((byte)(d*255-128));
			}
		}	
	}
	
	/**
	 * get direct visual distance
	 * by node id (n-th node in list)
	 * @param a
	 * @param b
	 * @return
	 */
	private double getVisualDistance(int a, int b, double defaultValue){
		int ia = nodeList[a]; // get ids
		int ib = nodeList[b];
		byte[] f1 = features.getFeature(ia);
		byte[] f2 = features.getFeature(ib);
		if(f1 != null && f2 != null){
			return Metric.distance(f1, f2);
		}		
		return defaultValue; // might by bad, happens when feature not available in db 
	}
	
	/**
	 * dump a row
	 * @param row
	 */
	public void dumpRow(int row[]){
		for(int r=0; r<row.length; r++){
			System.out.print(row[r]);
			System.out.print(" - ");
		}
		System.out.println("");
	}

	/**
	 * retrieve all mean distances for
	 * every node
	 * @return
	 */
	public double[] getNodeMeanDistances() {
		return nodeDistances;
	}

}
