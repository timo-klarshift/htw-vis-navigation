package org.htw.vis.setup;

import java.util.Date;

import org.htw.vis.layer.ZoomLayer;
import org.htw.vis.layer.ZoomWorld;
import org.htw.vis.lucene.LuceneQueryCallback;
import org.htw.vis.lucene.SearchResult;

/**
 * index searcher 
 * command line tool
 * 
 * @author timo
 *
 */
public class IndexSearcher {
	public static void main(String[] args){
		// get query
		String query = "green apple isolated";
		if(args.length > 0){
			query = args[0];
		}	
		
		// layer id
		int layerId = 0;		
		if(args.length > 1){
			layerId = Integer.parseInt(args[1]);
		}
		
		// max results
		int max = 1000;
		if(args.length > 2){
			max = Integer.parseInt(args[2]);
		}
					
		// min similarity
		float minSim = 0.5f;		
		if(args.length > 3){
			minSim = Float.parseFloat(args[3]);
		}
				
		// 
		System.out.println("Search for `" + query + "` on layer " + layerId + " with minSim=" + minSim);		
		
		// get layer		
		ZoomLayer layer = ZoomWorld.create(1).getLayer(layerId);
		
		// perform search
		long startTime = new Date().getTime();			
		layer.searcher().search(query, max, new LuceneQueryCallback.SimpleQueryCallback(minSim) {
			
			@Override
			public void onSearchResult(SearchResult sr) {
				System.out.println(sr + " :: " + sr.getWords());				
			}
		});		
		long duration = (new Date().getTime() - startTime);
			
		// some stats		
		System.out.println("Took " + duration + " ms");
	}
}
