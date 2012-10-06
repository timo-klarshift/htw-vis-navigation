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
public class ImageImporter extends SetupProcess {
	/* import settings */	
	public static final int READ_FETCH_SIZE = 10000;
	public static final int TASK_BULK_SIZE = 50000;
	public static final int FLUSH_BULK_SIZE = 20000;
	
	/* how many images to import */
	public final int NUM_IMAGES;	

	/* stats */
	private final AtomicInteger imageImportCount = new AtomicInteger();

	/* database */
	private Database destinationDb, sourceDb;
	private Connection cSource, cDestination;
	private PreparedStatement insertImageStmt, readStmt;
	private long startTime;

	/**
	 * create image importer
	 */
	public ImageImporter(int numImages) {
		super("Image Importer");		
		this.NUM_IMAGES = numImages;
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

			// create insert statement for destination database
			insertImageStmt = cDestination
					.prepareStatement("INSERT INTO node_0 (fotoliaId,thumbPath) VALUES (?,?)");

			// read statement
			readStmt = cSource
					.prepareStatement("SELECT fotolia_id,thumb_path FROM image LIMIT ?,?");
			readStmt.setFetchSize(READ_FETCH_SIZE);			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.info("Initial image importer created.");
	}

	private void startRuntime() {
		startTime = new Date().getTime();
	}

	public long getRuntime() {
		return new Date().getTime() - startTime;
	}

	public int getThroughput() {
		return (int) ((1000 * imageImportCount.get()) / getRuntime());
	}

	/**
	 * import all images
	 */
	public void importImages() {
		log.info("Importing (" + NUM_IMAGES + ") Images ...");

		// drop and re-create
		destinationDb.execute("DROP TABLE node_0");
		SchemaHelper.initTable(0);

		startRuntime();
		int taskBulk = Math.min(TASK_BULK_SIZE, NUM_IMAGES);
		for (int c = 0; c < NUM_IMAGES; c += taskBulk) {
			importImages(c, taskBulk);								
		}

		// final commit
		flushImages();
		log.info("All (" + NUM_IMAGES + ") Images imported.");
	}

	private void flushImages() {
		try {
			log.info("Flushing ...");
			insertImageStmt.executeBatch();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	

	/**
	 * import image range
	 * 
	 * @param offset
	 * @param numImages
	 */
	private void importImages(int offset, int numImages) {
		log.info("Importing Image Range " + offset + " to "
				+ (offset + numImages));

		try {
			// read all from source database
			readStmt.setInt(1, offset);
			readStmt.setInt(2, numImages);
			ResultSet rs = readStmt.executeQuery();

			// add all nodes
			while (rs.next()) {
				addNode(rs.getInt("fotolia_id"), rs.getString("thumb_path"));
			}

			// close result set
			rs.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// print final stats
		printStats();
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
		System.out.println("****** Import Stats *************");
		System.out.println("Imported " + imageImportCount.get() + " nodes");
		System.out.println("Troughput " + getThroughput() + " nodes / s");
	}

	/**
	 * add node
	 * 
	 * @param fotoliaId
	 * @param thumbPath
	 * @param words
	 * @throws SQLException
	 */
	private void addNode(int fotoliaId, String thumbPath) throws SQLException {
		insertImageStmt.setInt(1, fotoliaId);
		insertImageStmt.setString(2, thumbPath);
		insertImageStmt.addBatch();
		imageImportCount.incrementAndGet();
		
		if (imageImportCount.get() % FLUSH_BULK_SIZE == 0) {
			printStats();
			setProgress("Importing images ...", (float)((double)imageImportCount.get() / NUM_IMAGES));
			flushImages();
		}
		
	}
	
	
	@Override
	protected void doProcess() {	
		// import images
		importImages();
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
		ImageImporter importer = new ImageImporter(2000000);
		importer.process();

		// exit
		System.exit(0);
	}
}
