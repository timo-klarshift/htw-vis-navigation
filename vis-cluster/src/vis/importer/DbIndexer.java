package vis.importer;


import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import org.apache.log4j.Logger;

import vis.config.VisConfig;
import vis.db.ConnectionFactory;
import vis.db.SqlHelper;
import vis.image.Image;
import vis.logging.VisLog;
import vis.lucene.Indexer;

/**
 * reads from database and writes
 * an index with lucene to the given index path
 * 
 * @author timo
 * 
 */
public class DbIndexer {
	private Connection con;
	private int layerId;
	private String indexPath;
	private Logger log = Logger.getLogger(this.getClass());
	private SqlHelper sql;
	private PreparedStatement stmt = null;
	private Indexer indexer;
	private int bulkSize = 1000;

	/**
	 * 
	 * @param layerId the corresponding layer id
	 * @param indexPath the destination index path
	 * @param bulkSize the database fetch bulk size
	 */
	public DbIndexer(int layerId, String indexPath, int bulkSize) {
		this.layerId = layerId;
		this.indexPath = indexPath;
		this.bulkSize = bulkSize;

		// init
		init();
	}

	private void init() {
		// create connection
		con = ConnectionFactory.getByName("images");
		sql = new SqlHelper(con);

		try {
			stmt = con
					.prepareStatement("select id,fotoliaId,thumbPath,words from images_"
							+ layerId + " where id >= ? && id < ?");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// create index
		indexer = new Indexer(new File(indexPath), true);

	}

	public void shutdown() {
		indexer.close();
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void index() {
		int max = getMaxId();
		int min = getMinId();
		try {
			for (int o = min; o < max; o += bulkSize) {
				stmt.setInt(1, o);
				stmt.setInt(2, o + bulkSize);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {																		
					// index image
					String thumbPath = rs.getString(3);
					String words = rs.getString(4);
					Image image = new Image(rs.getInt(1), rs.getInt(2), thumbPath, words);	
					indexer.index(image.toDocument());
				}
				
				log.info("Index Chunk " + o/bulkSize + " of " + (max/bulkSize));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private int _numberSelect(String type) {
		Statement s;
		try {
			s = con.createStatement();
			ResultSet rs = s.executeQuery("select " + type + " from images_"
					+ layerId);
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return -1;
	}

	private int getMaxId() {
		return _numberSelect("MAX(id)");
	}

	private int getMinId() {
		return _numberSelect("MIN(id)");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		VisLog.initLogging();
		VisConfig.load();

		DbIndexer indexer = new DbIndexer(0, "../index/test-db-indexer", 5000);
		indexer.index();
		indexer.shutdown();
	}

}
