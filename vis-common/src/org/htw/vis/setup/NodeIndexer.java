package org.htw.vis.setup;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.util.ThreadInterruptedException;
import org.htw.vis.config.EnvDbConfig;
import org.htw.vis.db.ConnectionFactory;
import org.htw.vis.db.Database;
import org.htw.vis.db.QueryCallback;
import org.htw.vis.db.concurrency.ConcurrentQueryExecutor;
import org.htw.vis.layer.ZoomLayer;
import org.htw.vis.layer.ZoomWorld;
import org.htw.vis.lucene.LuceneIndex;

/**
 * image indexer
 * index all images in source layer
 * @author timo
 */
public class NodeIndexer implements ConnectionFactory, QueryCallback {
	private final Logger log = Logger.getLogger(this.getClass());
	private final LuceneIndex index;
	private final ConcurrentQueryExecutor e;

	private final AtomicInteger indexCount = new AtomicInteger();
	
	private final int layerId;

	/**
	 * image importer
	 * 
	 */
	public NodeIndexer(int layerId, int threadCount) {
		//ZoomWorld world = ZoomWorld.create(1);
		ZoomWorld world = ZoomWorld.get();
		
		log.info("Indexer on Layer " + layerId + " with " + threadCount
				+ " threads");
		
		this.layerId = layerId;

		// get executor
		e = new ConcurrentQueryExecutor("node_" + layerId,
				"id,fotoliaId,thumbPath,words", "id", threadCount, this);

		// get index
		
		index = world.getIndex(layerId);
	}
	
	private void printStats(){
		System.out.println("********* Indexer Monitor *********");
		System.out.println("Progress: " + Math.round(100 * e.getProgress()) + " %");
		System.out.println("Indexed images: " + indexCount.get());
		System.out.println("Tasks remaining: " + e.getRemainingTaskCount());
		System.out.println("***********");
	}

	/**
	 * index all images
	 */
	public void indexAll() {
		// clear index
		index.clear();

		// execute all queries concurrent
		e.execute(this);
		
		// get node count
		int nodeCount = new ZoomLayer(layerId).nodeCount();

		// indexing loop
		while (indexCount.get() < nodeCount) {
			try {
				printStats();
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		// final stats
		printStats();
		
		// stop executor
		e.stop();			

		// optimize & close
		index.optimize();
		index.close();
	}

	/**
	 * importer entry point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// setup logger
		Logger.getRootLogger().setLevel(Level.INFO);
		ZoomWorld.create(0);

		int layerId = 0;
		int threadCount = 12;

		if (args.length > 0) {
			layerId = Integer.parseInt(args[0]);
		}

		if (args.length > 1) {
			threadCount = Integer.parseInt(args[1]);
		}
	
		// create indexer and index all images of selected layer
		NodeIndexer indexer = new NodeIndexer(layerId, threadCount);
		indexer.indexAll();
	}

	@Override
	public Connection getConnection() {
		try {
			return Database.get(Database.LAYERS).getConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onQueryResult(ResultSet rs) {
		try {
			while (rs.next()) {
				String words = rs.getString("words");
				if(words != null){
					index.addDoc(rs.getInt("fotoliaId"), rs.getString("thumbPath"),
						words);
				}
				indexCount.incrementAndGet();				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ThreadInterruptedException e) {
			// e.printStackTrace();
		}
	}
}
