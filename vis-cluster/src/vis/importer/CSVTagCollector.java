package vis.importer;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * tag collector
 * @author timo
 *
 */
public class CSVTagCollector implements CSVCallback {
	private float minDocFreq;
	
	private HashMap<String,Integer> tagMap = new HashMap<String,Integer>();
	private Set<String> finalTagMap = new HashSet<String>();	
	private int max = 0;
	
	private Logger log = Logger.getLogger(this.getClass());
	
	public CSVTagCollector(float minDocFreq) {
		this.minDocFreq = minDocFreq;
		init();
	}
	

	
	private void init(){
		log.info("Created tag collector with minDocFreq=" + minDocFreq);
	}

	@Override
	public void onImage(String id, String url, String tags) {
		StringTokenizer tk = new StringTokenizer(tags, ",");
		while(tk.hasMoreTokens()){
			addTag(tk.nextToken());
		}
	}
	
	private void addTag(String tag){
		tag = tag.toLowerCase().trim();
		if(tagMap.containsKey(tag)){
			int v = tagMap.get(tag);
			if(v > max){max = v;  }
			tagMap.put(tag, v+1);
		}else{
			tagMap.put(tag, 1);
		}
	}	
	
	private void reduce(){
		log.info("Reduceing " + tagMap.size() + " keywords ...");
		finalTagMap.clear();
		int total = tagMap.size();
		double minFreq = minDocFreq;		
		for(Entry<String, Integer> e : tagMap.entrySet()){
			double freq = ((double)e.getValue())/((double)total);			
			if(freq >= minFreq){
				finalTagMap.add(e.getKey());
			}
		}
		
		double reduction = (100*(finalTagMap.size()/(double)total));
		log.info("Reduced from " + total + " to " + finalTagMap.size() + " (" + Math.round(reduction*100)*0.01 +  " %)");
		
		// clean up
		tagMap.clear();
	}
	
	public boolean hasTag(String t){
		return finalTagMap.contains(t);
	}

	@Override
	public void onFileDone(File file) {	
	}

	@Override
	public void onDone() {
		// reduce
		log.info("Collected " + tagMap.size() + " keywords.");
		reduce();
	}

	@Override
	public void onStart() {
		log.info("Collecting ...");
	}

}
