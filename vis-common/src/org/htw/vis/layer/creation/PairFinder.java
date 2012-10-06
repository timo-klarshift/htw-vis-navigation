package org.htw.vis.layer.creation;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.htw.vis.db.big.BigMap;
import org.htw.vis.db.big.BigMapIteratorCallback;
import org.htw.vis.helper.FeatureAccess;
import org.htw.vis.layer.NetworkNode;
import org.htw.vis.lucene.ImageRetriever;
import org.htw.vis.lucene.LuceneIndex;
import org.htw.vis.lucene.LuceneSearch;
import org.htw.vis.lucene.SearchResult;
import org.htw.vis.matching.ImageMatching;
import org.htw.vis.matching.MatchParty;

/**
 * pair finder
 * @author timo
 *
 */
public class PairFinder {		
	
	private ImageMatching matching1;
	
	
	private final int nodeCount;
	private LuceneSearch searcher;
	private ImageRetriever retrieval;
	
	private final Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * create a pair finder
	 * @param index
	 */
	public PairFinder(LuceneIndex index){				
		searcher = new LuceneSearch(index);
		retrieval = new ImageRetriever(searcher);
		nodeCount = searcher.maxDoc();
	}
	
	/**
	 * find pairs
	 * @throws Exception 
	 */
	public void findPairs() throws Exception{			
		matching1 = new ImageMatching();
		
		// populate parties
		for(int di=0; di<nodeCount; di++){
			Document doc = searcher.getDoc(di);
			NetworkNode node = new SearchResult(doc, di, 0f).getNode();
					
			MatchParty p = new MatchParty(node.getFotoliaId());
			ArrayList<SearchResult> similar = retrieval.getSimilarImages(node, 250, 0.01f, 0.01f, 1f) ;
			
			for(SearchResult o : similar){				
				p.prefer(o.getFotoliaId(), o.getSimilarity());
			}
			
			matching1.addParty(p);		
			System.out.println(".");
		}
		
		// match parties
		matching1.match();
		
		// get free parties for phase II
		/*matching2 = new ImageMatching();		
		for(Integer p1 : matching1.getFreeParties()){			
			MatchParty p = new MatchParty(p1);
			
			for(Integer p2 : matching1.getFreeParties()){				
				if(!p1.equals(p2))
					p.prefer(p2, getPhase2Rank(p1, p2));
			}	
			
			matching2.addParty(p);
		}
		
		// phase II match
		matching2.match();
		
		// PHASE III match remaining
		final ArrayList<Integer> left = new ArrayList<Integer>();
		matching2.iterateFreeParties(new BigMapIteratorCallback<Integer>() {

			@Override
			public void onKeyValue(Integer key, Integer value) {
				left.add(key);
				
			}
		});
		
		int prev = -1;
		for(Integer i : left){
			if(prev != -1){
				log.info("Phase III Fallback Pairing: " + i + "/" + prev);
				matching2.pair(matching2.getParty(prev), matching2.getParty(i));
			}
			prev = i;
		}
		
		if(matching2.getFreeCount() > 0){
			log.error("Matching 1: " + matching1.getFreeCount());
			log.error("Matching 2: " + matching2.getFreeCount());
			throw new Exception("Could not pair all nodes.");
		}
		*/
	}	
	
	public Map<Integer,Integer> createMatrix(){	
		final BigMap<Integer> resultMap = new BigMap<Integer>("a"+UUID.randomUUID().toString().replaceAll("-", ""));
					
		matching1.iterateParties(new BigMapIteratorCallback<MatchParty>() {
			private int i=0;
			
			@Override
			public void onKeyValue(Integer key, MatchParty p) {
				if(p.isPaired() && resultMap.containsKey(p.getPairId()) == false){
					resultMap.put(p.getId(), p.getPairId());
				}
			}
		});	
		
		System.out.println("XXXXXXX" + resultMap.size());
		
	
		return resultMap;
	}
	
	public Set<Integer> getFree(){
		return matching1.getFreeParties();
	}


}
