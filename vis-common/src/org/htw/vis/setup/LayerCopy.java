package org.htw.vis.setup;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

/**
 * image indexer
 * intends to index all images in source layer
 * 
 * @author timo
 * 
 */
public class LayerCopy implements ConnectionFactory, QueryCallback {
	private final Logger log = Logger.getLogger(this.getClass());	
	private final ConcurrentQueryExecutor e;

	private final AtomicInteger copyCount = new AtomicInteger();
	private int numImages;
	

	/**
	 * image importer
	 * 
	 */
	public LayerCopy(int threadCount) {
		log.info("Copy Base Layer ... ");
		
		Database.register(Database.LAYERS, EnvDbConfig.get().layerConfig());
		
		// get executor
		e = new ConcurrentQueryExecutor("node_0","id,fotoliaId,thumbPath,words", "id", threadCount, this);
	}
	
	private void printStats(){
		log.info("********* Import Copy Monitor *********");
		log.info("Progress: " + Math.round(100*(double)copyCount.get() / (numImages)) + " %");
		log.info("Copied images: " + copyCount.get());
		log.info("Tasks remaining: " + e.getRemainingTaskCount());
		log.info("***********");
	}

	/**
	 * index all images
	 */
	public void copy(int numImages) {
		this.numImages = numImages;
					
		try {
			Database.get(Database.LAYERS).getConnection().createStatement().execute("DELETE FROM node_1");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// execute all queries concurrent
		e.execute(this);
		
		
			
		// indexing loop
		while (copyCount.get() < numImages) {
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
	}

	/**
	 * importer entry point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// setup logger
		Logger.getRootLogger().setLevel(Level.INFO);
				
		LayerCopy copy = new LayerCopy(1);
		copy.copy(1000000/10);
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
			Connection con =  Database.get(Database.LAYERS).getConnection();
			PreparedStatement stmt = con.prepareStatement("insert into node_1(id,fotoliaId,words,thumbPath) values(?,?,?,?)");  
			while (rs.next() && copyCount.get() < numImages) {
				stmt.setInt(1, rs.getInt("id"));
				stmt.setInt(2, rs.getInt("fotoliaId"));
				stmt.setString(3, rs.getString("words"));
				stmt.setString(4, rs.getString("thumbPath"));
				stmt.execute();
				copyCount.incrementAndGet();						
			}
			stmt.close();					
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ThreadInterruptedException e) {
			// e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
