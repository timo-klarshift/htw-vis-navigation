package org.htw.vis.layer.creation;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.htw.vis.helper.FeatureAccess;
import org.htw.vis.layer.NetworkNode;
import org.htw.vis.layer.ZoomLayer;
import org.htw.vis.lucene.LuceneIndex;
import org.htw.vis.lucene.LuceneSearch;

/**
 * quad finder
 * @author timo
 *
 */
public class QuadFinder {
	private LuceneIndex index;
	private final int nodeCount;
	private final LuceneSearch searcher;
	private final ZoomLayer layer;
	private final Logger log = Logger.getLogger(this.getClass());
	
	private FeatureAccess features = new FeatureAccess();

	/**
	 * create a quad finder
	 * @param layer
	 */
	public QuadFinder(ZoomLayer layer) {
		// store layer and index
		this.layer = layer;
		this.index = layer.getIndex();

		searcher = new LuceneSearch(index);
		nodeCount = searcher.maxDoc();
	}

	public void findQuads(QuadCallback callback) throws Exception {
		if(nodeCount < 4){
			log.error("Layer requires at least 4 nodes to form quads. You have: " + nodeCount);
			return;
		}
		log.info("Finding Quads for " + nodeCount + " nodes ...");
		
		// PHASE I //////////////////////////////////////////
		
		// find pairs
		PairFinder pf = new PairFinder(index);
		pf.findPairs();
		Map<Integer, Integer> pairMap1 = pf.createMatrix();
		
		log.info("\tPHASE 1 done.");
				
		// create new nodes and index them
		LuceneIndex tmp = LuceneIndex.register(UUID.randomUUID().toString(), "/tmp/test");
		tmp.clear();
		
		// index pairs
		for (Integer i1 : pairMap1.keySet()) {
			NetworkNode n1 = layer.getNodeById(i1);
			NetworkNode n2 = layer.getNodeById(pairMap1.get(i1));
			if (n2 != null) {		
				try {
					tmp.addDoc(n1.getFotoliaId(), "", createCombinedWords(n1, n2));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				System.out.println("ERROR n2 = null / n1=" + n1);
				System.exit(5);
			}
		}
		
		// index free
		// ADDED
		/*for(Integer f1 : pf.getFree()){
			NetworkNode n1 = layer.getNodeById(f1);
			try {
				System.out.println("FREE " + n1);
				tmp.addDoc(n1.getFotoliaId(), "", n1.getWords());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/

		// finalize index
		tmp.optimize();		
		tmp.close();
		
		// validate
		LuceneSearch tmpSearch =  new LuceneSearch(tmp);
		int nc = tmpSearch.maxDoc();
		log.info("New index has " + nc + " nodes.");
		
		
		// PHASE II //////////////////////////////////////////////
		
		// find pairs for second phase
		PairFinder pf2 = new PairFinder(tmp);
		pf2.findPairs();
		
		Map<Integer, Integer> pairMap2 = pf2.createMatrix();

		// ADDED
		for(Integer f1 : pf2.getFree()){
			NetworkNode n1 = layer.getNodeById(f1);
			System.out.println("Next layer " + n1);
			callback.onQuadEmit(n1, n1, null, null, null);
		}
		
		for(Integer f2 : pf.getFree()){
			NetworkNode n2 = layer.getNodeById(f2);
			System.out.println("Next layer " + n2);
			callback.onQuadEmit(n2, n2, null, null, null);
		}
		
		for (Integer i2 : pairMap2.keySet()) {		
			NetworkNode n1 = layer.getNodeById(i2);
			NetworkNode n2 = layer.getNodeById(pairMap2.get(i2));
			NetworkNode n3 = (n1 != null ? layer.getNodeById(pairMap1.get(n1.getFotoliaId())) : null);
			NetworkNode n4 = (n2 != null ? layer.getNodeById(pairMap1.get(n2.getFotoliaId())) : null);
			
			NetworkNode r = getMeanNode(n1, n2, n3, n4);
			callback.onQuadEmit(r, n1, n2, n3, n4);
		}
		
		
	}

	/**
	 * get a representative node 
	 * for 4 given nodes
	 * TODO: calculate minimal distance node
	 * @param n1
	 * @param n2
	 * @param n3
	 * @param n4
	 * @return
	 */
	private NetworkNode getMeanNode(NetworkNode ... nodes) {
		
		// choose
		NetworkNode meanNode = getVisualMeanNode(nodes);		
		meanNode.setWords(createCombinedWords(nodes));
		
		return meanNode;						
	}
	
	/**
	 * get the visual mean node
	 * @param nodes
	 * @return
	 */
	public NetworkNode getVisualMeanNode(NetworkNode ... nodes){
		double minD = Double.MAX_VALUE, d=0;
		NetworkNode w = null;
		for(int n=0; n<nodes.length; n++){			
			d = getMeanDistance(nodes[n], nodes);			
			if(d < minD){				
				minD = d;
				w = nodes[n];
			}
		}
		return w;
	}
	
	public double getMeanDistance(NetworkNode a, NetworkNode ... others){
		if(a == null){
			return Double.MAX_VALUE;
		}
		
		double totalD = 0;
		for(int n=0; n<others.length; n++){
			NetworkNode c = others[n];
			if(c != null){
				if(!c.getFotoliaId().equals(a.getFotoliaId())){
					totalD += features.getDistance(a.getFotoliaId(), c.getFotoliaId());
				}
			}
			
		}
		
		return totalD;
	}
	
	/**
	 * create a text from several nodes
	 * @param nodes
	 * @return
	 */
	public String createCombinedWords(NetworkNode ... nodes){	
		HashSet<String> words = new HashSet<String>();		
		StringBuffer buffer = new StringBuffer();
		
		// write all to string buffer		
		for(NetworkNode n : nodes){
			if(n != null){
			//buffer.append(n.getWords());
			//buffer.append(',');
			
				StringTokenizer t = new StringTokenizer(n.getWords(), ",");
				while(t.hasMoreTokens()){
					String w = t.nextToken();
					//if(words.contains(w) == false){
						//words.add(w);
						buffer.append(w + ",");
					//}
				}
			}
		}
		
		// remove last comma
		buffer.deleteCharAt(buffer.length()-1);				
		return buffer.toString();
	}
}
