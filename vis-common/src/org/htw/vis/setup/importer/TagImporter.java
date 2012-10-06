package org.htw.vis.setup.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.htw.vis.config.EnvDbConfig;
import org.htw.vis.db.ConnectionFactory;
import org.htw.vis.db.Database;
import org.htw.vis.db.QueryCallback;
import org.htw.vis.db.concurrency.ConcurrentQueryExecutor;
import org.htw.vis.helper.KeywordHelper;
import org.htw.vis.helper.SchemaHelper;
import org.htw.vis.setup.SetupProcess;

/**
 * image importer
 * 
 * @author timo
 * 
 */
public class TagImporter extends SetupProcess {
	/* import settings */
	public static final int MIN_KEYWORDS = 1;
	public static final int COMMIT_BULK_SIZE = 50000;
	public static final int TAG_THREADS = 10;

	/* stats */
	private final AtomicInteger tagImportCount = new AtomicInteger();

	/* database */
	private Database destinationDb, sourceDb;
	private Connection cSource, cDestination;
	private PreparedStatement updateTagsStmt;
	private long startTime;
	
	private boolean flushing = false;

	/**
	 * create image importer
	 */
	public TagImporter() {
		super("Tag Importer");
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
	
	/**
	 * clean tags
	 */
	private void cleanTags(){
		log.info("Cleaning tags ...");
		destinationDb.execute("DELETE FROM node_0 WHERE words IS NULL");
		log.info("Cleaning tags ... DONE.");		
	}

	
	private void flushTags() {
		try {
			flushing = true;
			log.info("Flushing ...");
			updateTagsStmt.executeBatch();
			flushing = false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * import tags
	 */
	public void importTags() {
		log.info("Importing tags ... ");
		TagImportHandler th = new TagImportHandler(this);
		ConcurrentQueryExecutor e = new ConcurrentQueryExecutor("node_0",
				"fotoliaId", "id", TAG_THREADS, th);
		e.execute(th);

		// wait for executor finished
		while (e.isRunning()) {
			try {
				Thread.sleep(1000);
				if(!flushing){
					setProgress("Importing tags ...", e.getProgress());
					printStats();
				}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		flushTags();

		log.info("Importing tags ... DONE.");
		
		cleanTags();
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
		log.info("****** Import Stats *************");
		System.out.println("Imported Tags for " + tagImportCount.get() + " nodes");
		//log.info("Troughput " + getThroughput() + " nodes / s");
	}
	
	protected synchronized void updateTags(int fotoliaId, String words){
		try {
			updateTagsStmt.setString(1, words);
			updateTagsStmt.setInt(2, fotoliaId);
			updateTagsStmt.addBatch();
			tagImportCount.incrementAndGet();
			
			if(tagImportCount.get() % COMMIT_BULK_SIZE == 0){
				flushTags();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	@Override
	protected void doProcess() {		
		importTags();
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
		TagImporter importer = new TagImporter();
		importer.process();

		// exit
		System.exit(0);
	}
}
