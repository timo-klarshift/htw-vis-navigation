package org.htw.vis.setup;

import java.sql.SQLException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.htw.vis.helper.FeatureAccess;
import org.htw.vis.helper.SchemaHelper;
import org.htw.vis.layer.NetworkNode;
import org.htw.vis.layer.ZoomLayer;
import org.htw.vis.layer.ZoomWorld;
import org.htw.vis.layer.creation.QuadCallback;
import org.htw.vis.layer.creation.QuadFinder;
import org.htw.vis.layer.creation.SOMPositionWriter;
import org.htw.vis.layer.creation.SimplePositionWriter;
import org.htw.vis.lucene.LuceneIndex;
import org.htw.vis.lucene.LuceneQueryCallback;
import org.htw.vis.lucene.LuceneSearch;
import org.htw.vis.lucene.SearchResult;


/**
 * bootstrap all layers
 * 
 * TODO: http://stackoverflow.com/questions/1757363/java-hashmap-performance-optimization-alternative
 * http://www.technofundo.com/tech/java/equalhash.html
 * 
 * @author timo
 *
 */
public class WorldCreator {	
	private final ZoomWorld world;
	private final Logger log = Logger.getLogger(this.getClass());
	
	private int maxImages;
	private int MAX_SIZE = 16;
	public static final int E = 7;
	public static final double MIN_E = 1;
	
	/**
	 * create world creator
	 * 
	 * @param maxImages
	 * @throws Exception
	 */
	public WorldCreator() throws Exception{			
		// store max images
		this.maxImages =  (int) Math.pow(4, E);
		
		// create world
		world = ZoomWorld.create(E);
		log.info("Created Zoom world for " + maxImages + " images / = 4^" + E);
	}
	
	/**
	 * setup all tables
	 */
	public void setupTables(){
		for(int i=0; i<MAX_SIZE; i++){
			try {
				if(i>0)
					world.getBaseLayer().execute("drop table node_" + i);										
			} catch (SQLException e) {
				log.warn(e.getMessage());
			}
			
			/*try {
				world.getBaseLayer().execute("drop table relation_" + i);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				log.warn(e.getMessage());
			}*/
		}
		
		for(int i=1; i<world.getLayerCount(); i++){
			SchemaHelper.initTable(i);
		}
	}
	
	/**
	 * create world from a query
	 * @param query
	 * @throws Exception
	 */
	public void createFromQuery(String query) throws Exception{
		// get main index
		LuceneIndex mainIndex = world.getBaseIndex();		
		LuceneSearch search = new LuceneSearch(mainIndex);
		
		
		
		// write first layer
		final ZoomLayer firstLayer = world.getFirstLayer();
		firstLayer.clear();
		
		log.info("Search for `" + query + "`");
		
		// add search results
		search.search(query, maxImages, new LuceneQueryCallback.SimpleQueryCallback() {
			@Override
			public void onSearchResult(SearchResult sr) {
				firstLayer.addNode(sr.getFotoliaId(), sr.getWords(), sr.getThumbPath());
			}
		});
					
		// optimize and close index
		firstLayer.getIndex().optimize();
		firstLayer.getIndex().close();
						
		// process layer
		new FeatureAccess().preload(firstLayer);
		createFromLayer(firstLayer);
	}
	
	public void createFromSequence() throws Exception{
		// get main index
		LuceneIndex mainIndex = world.getBaseIndex();
		LuceneSearch search = new LuceneSearch(mainIndex);
		
		// write first layer
		final ZoomLayer firstLayer = world.getFirstLayer();
		firstLayer.clear();
		
		maxImages = Math.min(maxImages, search.maxDoc());
		
		
		for(int i=0; i<maxImages; i++){
			SearchResult s = new SearchResult(search.getDoc(i), i, 0f);
			firstLayer.addNode(s.getFotoliaId(), s.getWords(), s.getThumbPath());
		}
		
		// optimize and close index
		firstLayer.getIndex().optimize();
		firstLayer.getIndex().close();
		
		// process layer
		createFromLayer(firstLayer);
	}
	


	/**
	 * create quads from layer
	 * @param layer
	 * @throws Exception
	 */
	public void createFromLayer(final ZoomLayer layer) throws Exception{
		// validate node count
		if(layer.nodeCount() <= 4){
			return;
		}
		
						
		// create next layer
		final ZoomLayer nextLayer = layer.lower();
		if(nextLayer == null)return;
		nextLayer.clear();
		
		// find quads for that layer
		QuadFinder qFinder = new QuadFinder(layer);
		qFinder.findQuads(new QuadCallback() {
			
			@Override
			public void onQuadEmit(NetworkNode r, NetworkNode n1, NetworkNode n2,
					NetworkNode n3, NetworkNode n4) {
				
				
				
				// update bottom layer
				try {
					String c = "0";
					if(n1 != null){c += " OR fotoliaId=" + n1.getFotoliaId(); }
					if(n2 != null){c += " OR fotoliaId=" + n2.getFotoliaId(); }
					if(n3 != null){c += " OR fotoliaId=" + n3.getFotoliaId(); }
					if(n4 != null){c += " OR fotoliaId=" + n4.getFotoliaId(); }
					String q = "update " + layer.getTablename() + " set parent=" + r.getFotoliaId() + " where " + c;
					System.out.println(q);
					layer.execute(q);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// add single node to nextLayer
				nextLayer.addNode(r.getFotoliaId(), r.getWords(), r.getThumbPath());
			}
		});
		
		// optimize and close index
		nextLayer.getIndex().optimize();
		nextLayer.getIndex().close();
		
		// perform on 
		createFromLayer(nextLayer);		
	}

	public static void main(String[] args) {
		// setup logger
		Logger.getRootLogger().setLevel(Level.INFO);
				
		try {
			WorldCreator wc;
			wc = new WorldCreator();
			
			// setup tables
			wc.setupTables();	
			
			// create from query
			//wc.createFromQuery("banana");
			//wc.createFromSequence();
			
			
			
			//wc.createFromLayer(ZoomWorld.get().getFirstLayer());
			wc.createFromLayer(ZoomWorld.get().getBaseLayer());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}

}
