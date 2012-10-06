package org.htw.vis.setup.importer;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.htw.vis.db.Database;
import org.htw.vis.db.concurrency.ConcurrentQueryExecutor;
import org.htw.vis.setup.SetupProcess;

/**
 * image importer
 * 
 * @author timo
 * 
 */
public class TagReducer extends SetupProcess {	

	/* database */
	private Database destinationDb, sourceDb;
	private Connection cSource, cDestination;
	private PreparedStatement updateTagsStmt;
	private long startTime;
	public static final int COMMIT_BULK_SIZE = 50000;
	
	/* stats */
	private final AtomicInteger tagImportCount = new AtomicInteger();
	
	private LinkedHashMap<String,Integer> finalMap;

	/**
	 * create image importer
	 */
	public TagReducer() {
		super("Tag Reducer");
	}
	
	@Override
	public void init() {		
		
		// register databases
		registerDatabases();
		
		// create statements
		try {
			// get database connections
			sourceDb = Database.get(Database.FEATURES);
			destinationDb = Database.get(Database.LAYERS);

			cSource = sourceDb.getConnection();
			cDestination = destinationDb.getConnection();
			cDestination.setAutoCommit(false);			
			
			// update tags statement
			updateTagsStmt = cDestination.prepareStatement("UPDATE node_0 SET words=? WHERE fotoliaId=? LIMIT 1");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.info("Initial image importer created.");
	}

	

	
	private void flushTags() {
		try {
			log.info("Flushing ...");
			updateTagsStmt.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * shutdown importer and all its database connections
	 */
	protected void shutdown() {
		try {			
			// close connections
			cSource.close();
			cDestination.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * print some stats
	 */
	public void printStats() {
		return;
		/*log.info("****** Import Stats *************");
		log.info("Imported " + tagImportCount.get() + " nodes");*/
		//log.info("Troughput " + getThroughput() + " nodes / s");
	}
		
	
	/**
	 * clean tags
	 */
	private void cleanTags(){
		log.info("Cleaning tags ...");
		destinationDb.execute("DELETE FROM node_0 WHERE words IS NULL");
		log.info("Cleaning tags ... DONE.");		
	}
	
	/**
	 * reduce tags
	 */
	private void collectTags() {
		log.info("Collecting tags ... ");
		TagCollectHandler th = new TagCollectHandler(destinationDb);
		ConcurrentQueryExecutor e = new ConcurrentQueryExecutor("node_0",
				"fotoliaId,words", "id", TagImporter.TAG_THREADS, th);
		e.execute(th);

		// wait for executor finished
		while (e.isRunning()) {
			try {
				Thread.sleep(1000);
				
				setProgress("Collecting Tags ...", e.getProgress());
				printStats();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}			

		log.info("Reducing tags ... DONE.");
		
		finalMap = th.getFinalKeywords();
		log.info("Reduction " + th.getReductionFactor() +  " / Final Tag Count: " + finalMap.size());
		
		new Dictionary(finalMap).save(new File("dic.txt"));
			
	}
	
	private void reduceTags(){
		tagImportCount.set(0);
		log.info("Reducing tags ... ");
		TagReduceHandler th = new TagReduceHandler(this, finalMap, destinationDb);
		ConcurrentQueryExecutor e = new ConcurrentQueryExecutor("node_0",
				"fotoliaId,words", "id", TagImporter.TAG_THREADS, th);
		e.execute(th);

		// wait for executor finished
		while (e.isRunning()) {
			try {
				Thread.sleep(1000);
				setProgress("Reducing Tags ...", e.getProgress());
				printStats();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}		
		
		flushTags();

		log.info("Reducing tags ... DONE.");			
	}
	
	protected synchronized void updateTags(int fotoliaId, String words){
		try {			
			updateTagsStmt.setString(1, words);
			updateTagsStmt.setInt(2, fotoliaId);
			updateTagsStmt.addBatch();
			
			int ic = tagImportCount.incrementAndGet();
			
			if(ic % COMMIT_BULK_SIZE == 0){
				flushTags();	
			}
						
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	@Override
	protected void doProcess() {		
		collectTags();
		reduceTags();		
		cleanTags();
	}
	
	protected Database getDestinationDb() {
		return destinationDb;
	}

	/**
	 * importer entry point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// import
		TagReducer importer = new TagReducer();
		importer.process();

		// exit
		System.exit(0);
	}
}
