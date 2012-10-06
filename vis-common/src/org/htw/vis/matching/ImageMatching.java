package org.htw.vis.matching;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.htw.vis.db.big.BigMap;
import org.htw.vis.db.big.BigMapIteratorCallback;

/**
 * image matching
 * @author timo
 *
 */
public class ImageMatching {
	//private final HashMap<Integer,MatchParty> partyMap = new HashMap<Integer,MatchParty>(100000);
	private BigMap<MatchParty> partyMap;
	private BigMap<Integer> freeParties; 
	
	private final Logger log = Logger.getLogger(this.getClass());
	
	public final static byte RANK_LOWEST = -128;
	
	private int iterationCount = 0;
	
	int pairCount = 0;
	
	/**
	 * create image matching
	 */
	public ImageMatching(){
		String name = ""+new Date().getTime();
		partyMap = new BigMap<MatchParty>("MATCH" + name);
		freeParties = new BigMap<Integer>("MATCHFREE" + name);
		
		partyMap.clear();
		freeParties.clear();
		
		log.info("Having " + partyMap.size() + " parties.");
		
		
	}
	
	public void iterateParties(BigMapIteratorCallback<MatchParty> callback){
		partyMap.iterate(callback);
	}
	
	public void iterateFreeParties(BigMapIteratorCallback<Integer> callback){
		freeParties.iterate(callback);
	}
	
	
	
	
	
	/**
	 * get party
	 * @param id
	 * @return
	 */
	public MatchParty getParty(Integer id){
		return partyMap.get(id);
	}
	
	/**
	 * has party
	 * @param id
	 * @return
	 */
	public boolean hasParty(Integer id){
		return partyMap.get(id) != null;
	}
	
	/**
	 * has party
	 * @param p
	 * @return
	 */
	public boolean hasParty(MatchParty p){
		return partyMap.get(p.getId()) != null;
	}
	
	/**
	 * add party
	 * @param party
	 * @return
	 */
	public MatchParty addParty(MatchParty party){
		if(!hasParty(party)){
			// add
			partyMap.put(party.getId(), party);	// persist
			//partyList.add(party);		
			
			log.debug("Added party " + party);
		}
		
		return party;
	}	
	
	/**
	 * rank encoding
	 * @param rank
	 * @return
	 */
	public static byte encodeRank(float rank){
		byte b = (byte)(rank*255f-128f);
		return b;
	}
	
	// TODO bad when used
	public Set<Integer> getFreeParties(){
		final Set<Integer> free = new HashSet<Integer>();
		freeParties.iterate(new BigMapIteratorCallback<Integer>() {

			@Override
			public void onKeyValue(Integer key, Integer value) {
				free.add(key);
				
			}
			
		});
		
		return free;
	}
	
	/**
	 * match
	 */
	public void match(){
		final int partyCount = partyMap.size();
		
		int prevPairCount = 0, i=0;
		
		boolean running = true;
		
		final AtomicBoolean changed = new AtomicBoolean(true);
		
		log.info("Matching. Having " + partyCount + " parties.");
		
		while(changed.get()){
			changed.set(false);
			
			i++;
			log.info("** ITERATION " + i + " / " + freeParties.size());
			
			final AtomicInteger processed = new AtomicInteger(0);
			
			// iterate over parties
			iterateParties(new BigMapIteratorCallback<MatchParty>() {
				
				@Override
				public void onKeyValue(Integer key, MatchParty party) {
					int p = processed.incrementAndGet();
					
					if(!party.isPaired() ){
						MatchParty prefParty;
						ArrayList<Integer> rejectedList = new ArrayList<Integer>();
						
						// check all preferred parties 
						for(Integer prefId : party.getPreferences().keySet()){
							iterationCount++;
														
							// get preferred party
							prefParty = getParty(prefId);
							
							changed.set(true);
							
							// propose
							if(prefParty.propose(party.getId())){
								pair(party, prefParty);
								
								break;
							}else{
								// keep rejecting party
								rejectedList.add(prefId);
							}
							
							
						}
						
						// remove rejected from prefer list
						for(Integer r : rejectedList){
							party.unprefer(r);
							persist(party);
						}
						
						if(!party.isPaired()){
							freeParties.put(party.getId(), -1);
						}
					}
					
					if(p % 10000 == 0){
						log.info("Processed: " + Math.round(100*(double)p / partyCount) + " %");
					}
				}
			});
			
			int cpc = getPairCount();
			int d = cpc - prevPairCount;
			if(d < 1 ){				
				log.info("Convergence : " + d);
			}
			
			prevPairCount = cpc;															
		}			
		
		log.info("Matching done with " + 0.01*Math.round(10000*(double)freeParties.size() / (double)partyCount) + " % unmatched parties.");
	}
	
	public int getIterationCount(){
		return iterationCount;
	}
	
	private void persist(MatchParty p){
		partyMap.put(p.getId(), p);		
	}
		
	public void unpair(MatchParty p){
		if(p.isPaired()){
			MatchParty paired = getParty(p.getPairId()); 
			paired.unpair();
			p.unpair();
			
			persist(p);
			persist(paired);
			
			freeParties.put(paired.getId(), -1);
			freeParties.put(p.getId(), -1);			
			pairCount--;
			
			log.debug("Unpaired " + paired + "/" + p);
		}
	}
	
	/**
	 * get pair count
	 * @return
	 */
	public int getPairCount(){
		return pairCount;
	}
	
	/**
	 * get free count
	 * @return
	 */
	public int getFreeCount(){
		return freeParties.size();
	}
	
	/**
	 * pair parties
	 * @param p1
	 * @param p2
	 */
	public void pair(MatchParty p1, MatchParty p2){
		unpair(p1); unpair(p2);
		
		// pair
		p1.pair(p2.getId());
		p2.pair(p1.getId());
		
		persist(p1);
		persist(p2);
		
		freeParties.remove(p1.getId());
		freeParties.remove(p2.getId());
				
		pairCount++;
		
		log.debug("Paired " + p1 + "/" + p2);
	}
}
