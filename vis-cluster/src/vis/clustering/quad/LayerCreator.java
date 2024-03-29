package vis.clustering.quad;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;


import org.apache.log4j.Logger;

import vis.config.VisConfig;
import vis.db.ConnectionFactory;
import vis.db.SqlHelper;
import vis.importer.DbIndexer;
import vis.logging.VisLog;
import vis.lucene.IndexFactory;
import vis.lucene.SearchCallback;
import vis.lucene.SearchResult;
import vis.lucene.Searcher;
import vis.lucene.TemporaryIndex;

public class LayerCreator {
	private Logger log = Logger.getLogger(this.getClass());
	private Searcher source;	
	
	private int sourceId, destinationId;
	
	public LayerCreator(int sId, int dId){
		this.sourceId = sId;
		this.destinationId = dId;
				
		// create searcher
		source = new Searcher(new File(IndexFactory.getImageIndexPath(sourceId)));
	}
	
	public boolean create(){
		// create layer
		Connection con = ConnectionFactory.getByName("images");
		SqlHelper h = new SqlHelper(con);
		h.createImageTable(destinationId);
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		boolean r = run(source, -1);
		if(r){
		
			// assume all quads are written 
			// to the layer above
			DbIndexer indexer = new DbIndexer(destinationId, IndexFactory.getImageIndexPath(destinationId), 5000);
			indexer.index();
			indexer.shutdown();			
		}
		
		return r;
	}
	
	private boolean run(Searcher source, int oldSize){
		int sourceCount = source.numDocs();
		
		if(sourceCount <= 0){
			log.info("FIN");
			return false;
		}
				
		log.info("Having " + sourceCount + " .. " + oldSize);
					
		// create loop
		QuadLoop loop = new QuadLoop(source, destinationId, oldSize == sourceCount);
		loop.create();
		loop.close();
		
		// rerun
		Searcher nextSearcher = new Searcher(loop.getTempIndex().getIndexPath());
		if(nextSearcher.numDocs() % 4 != 0){
			log.error("*********** " + nextSearcher.numDocs());
		}
		run(nextSearcher, sourceCount);
		
		// clean up (delete file)
		loop.getTempIndex().delete();
		
		
		
		return true;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// init
		VisConfig.load();
		VisLog.initLogging();
		
		int s=0;
		int ml = 8;
		int last = -1;
		for(int l=s; l<ml; l++){
			last = l;
			LayerCreator lc = new LayerCreator(l, l+1);
			if(!lc.create()){
				break;
			}
		}
		
		// write positions
		new QuadPosition().write(last);
		
		System.exit(1);
	}

}
