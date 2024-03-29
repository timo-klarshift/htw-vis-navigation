package vis.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.apache.log4j.Logger;


/**
 * implement caching
 * 
 * @author timo
 * 
 */
public class VisualFeatures {
	private Connection con;
	private PreparedStatement stmt = null;
	private HashMap<Integer,Cached> cache = new HashMap<Integer, Cached>();
	private Logger log = Logger.getLogger(this.getClass());
	

	public VisualFeatures() {
		init();
	}

	private void init() {
		// do database connection
		con = ConnectionFactory.getByName("features");

		// select statement
		try {
			stmt = con
					.prepareStatement("select features from features where fotoliaId=? limit 1");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public byte[] getFeature(Integer fotoliaId) {
		Cached cached = cache.get(fotoliaId);
		if(cached != null)return cached.features;
		
		byte[] features = null;
		try {
			// set params
			stmt.setInt(1, fotoliaId);

			// query and read
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				features = rs.getBytes(1);
				cache.put(fotoliaId, new Cached(features));				
			}else{
				log.warn("Feature not found " + fotoliaId);
			}
			
			

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return features;
	}

	public void shutdown() {
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * embedded cache class
	 * @author timo
	 *
	 */
	private class Cached {		
		public byte[] features = null;
		public Cached(byte[] features){			
			this.features = features;
		}
	}

}
