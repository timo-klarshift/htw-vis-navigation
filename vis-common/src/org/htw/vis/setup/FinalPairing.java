package org.htw.vis.setup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.htw.vis.db.ConnectionFactory;
import org.htw.vis.db.Database;
import org.htw.vis.db.QueryCallback;
import org.htw.vis.db.concurrency.ConcurrentQueryExecutor;
import org.htw.vis.layer.NetworkNode;
import org.htw.vis.layer.ZoomLayer;
import org.htw.vis.layer.ZoomWorld;
import org.htw.vis.lucene.LuceneQueryCallback;
import org.htw.vis.lucene.LuceneSearch;

/**
 * semantic clustering of images
 * 
 * @author timo
 * 
 */
public class FinalPairing extends SetupProcess implements ConnectionFactory,
		QueryCallback {

	private static final int RANGE = 64 * 4;
	private static float MIN_S = 0.1f; // 
	public static double MAX_VIS_DISTANCE = 0.7; // increase over time

	Set<Integer> allImages = Collections
			.synchronizedSet(new HashSet<Integer>());	
	
	
	Set<Integer> processedImages = new HashSet<Integer>();
	//Set<Integer> remaining = new HashSet<Integer>(1000);

	ArrayList<Pairer.Quad> quads = new ArrayList<Pairer.Quad>();

	/* database */
	private Database layerDb;
	private ZoomLayer layer;
	private LuceneSearch search;
	private int totalImages;
	private int quadCount = 0;
	
	public FinalPairing() {
		super("Pairing");
		
		
	}

	@Override
	protected void init() {
		registerDatabases();

		try {
			layerDb = Database.get(Database.LAYERS);
			ZoomWorld.create(12);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}

		layer = ZoomWorld.get().getBaseLayer();
		totalImages = layerDb.count("node_" + 0, "1");
		cleanTags();
		
		
		
	}
	
	private void reset(){
		int lid = layer.getLOD();
		totalImages = layerDb.count("node_" + lid, "1");
		
		allImages.clear();
		processedImages.clear();
		quads.clear();
		quadCount = 0;
		
		
		search = layer.searcher();
	}

	private void readSamples() {

		// read all ids
		int layerId = layer.getLOD();
		ConcurrentQueryExecutor e = new ConcurrentQueryExecutor("node_" + layerId,
				"fotoliaId", "id", 4, this);
		e.execute(this);

		// wait for executor finished
		while (e.isRunning()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		log.info("Done.");
	}

	private void cleanTags() {
		log.info("Cleaning tags ...");
		layerDb.execute("DELETE FROM node_0 WHERE words IS NULL");
		log.info("Cleaning tags ... DONE.");
	}

	private void pairingPass() {
		log.info("Pairing pass :: minS=" + MIN_S + " / maxD=" + MAX_VIS_DISTANCE);
		//
		int k = 0;
		int tsc = 0;
		int sc = 0;

		Integer[] copy = new Integer[allImages.size()];
		allImages.toArray(copy);

		
		for(final int id : copy){
			
			
			if (!processedImages.contains(id)) {

				NetworkNode node = layer.getNodeById(id);
				final ArrayList<Integer> candidates = new ArrayList<Integer>();

				search.moreLikeThis(node.getWords(), RANGE,
						new LuceneQueryCallback() {

							@Override
							public boolean onQueryCallback(Integer docId,
									float score, LuceneSearch searcher) {

								// stop when minimum score is reached
								if (score < MIN_S) {
									return false;
								}

								Integer similarId = Integer.parseInt(searcher
										.getDoc(docId).get("fotoliaId"));

								if (processedImages.contains(similarId) == false) {	
									candidates.add(similarId);
								}

								return true;
							}

						});	
				
				if (candidates.size() > 3) {
					pair(candidates);
				} else {
					//allImages.addAll(candidates);
				}

			} else {
				// log.info("Skipped " + id);
				tsc++;
				sc++;
			}

			// stats
			if (k++ % 10 == 0) {
				System.out.println("***");
				System.out.println("Processed: " + processedImages.size() + " / " + totalImages);
				System.out.println("Quads  : " + quads.size() + " // " + (4 * (quads.size() + quadCount)));
				System.out.println("Skipped: " + sc + " | " + tsc);
				System.out.println("Remaining: " + allImages.size());
				sc = 0;
			}					
		}

		
	}

	@Override
	protected void doProcess() {
		
		while(true){
			reset();
			
			ZoomLayer next = layer.lower();
			next.clear();
			
			
			if(totalImages <= 1){
				break;
			}
			
			// read samples
			readSamples();
			
			// pair it							
			pairingPass();
			
			if(quads.size() < 1){
				break;
			}
			
			// write remaining quads			
			writeToDatabase(next);	
			
			// clean up
			next.getIndex().optimize();
			next.getIndex().close();
										
			// debug
			//new QuadDebug(quads, layer);
			
			// switch layer
			layer = next;
								
								
		}

		System.exit(0);
	}
	
	private void writeToDatabase(ZoomLayer destiny){
		ZoomLayer previous = destiny.higher();
		
		
		
		log.info("Writing data ...");
		
		for(Pairer.Quad q : quads){
			
			// get combined words
			StringBuilder words = new StringBuilder();
			NetworkNode r = null;
			
			int c = 0;
			
			String update = "0";
			
			ArrayList<NetworkNode> nodes = new ArrayList<NetworkNode>();
			for(Integer i : q.getItems() ){
				NetworkNode n = previous.getNodeById(i);
				nodes.add(n);
				
				assert n != null;
				
				// representative
				if(r == null){
					r = n;
				}
				
				// get words				
				words.append(n.getWords()); 
								
				if(++c < 4)
					words.append(",");
				
				update += " OR fotoliaId=" + n.getFotoliaId();
			}
			
			// store image
			destiny.addNode(r.getFotoliaId(), words.toString(), r.getThumbPath());
			
			// set parent for lower ones			
			String updateQuery = "update " + previous.getTablename() + " set parent=" + r.getFotoliaId() + " where " + update;
			//System.out.println(updateQuery);
			try {
				previous.execute(updateQuery);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			quadCount  ++;
		}
		
		log.info("Data written.");
		
		destiny.getIndex().commit();
		
		
	}

	private void pair(ArrayList<Integer> set) {

		Pairer p = new Pairer(set);
		p.pair();

		// get pairs
		Set<Pairer.Quad> quadList = p.getQuads();

		quads.addAll(quadList);
				
		
		allImages.addAll(p.getRemaining());
		
		if(quads.size() > 10000){
			writeToDatabase(layer.lower());
			quads.clear();
		}
		

		//System.out.println("Remaining " + p.getRemaining().size());
		//System.out.println("Processed " + p.getQuads().size() * 4);

		for (Pairer.Quad q : quadList) {
			processedImages.addAll(q.getItems());
			allImages.removeAll(q.getItems());
		}
	}

	@Override
	protected void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onQueryResult(ResultSet rs) {
		try {
	
			// loop over samples
			while (rs.next()) {
				final int id = rs.getInt("fotoliaId");

				// add to set
				allImages.add(id);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public Connection getConnection() {
		return layerDb.getConnection();
	}

	/**
	 * entry point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		FinalPairing p = new FinalPairing();
		p.process();		
	}
}
