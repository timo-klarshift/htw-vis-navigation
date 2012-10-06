package org.htw.vis.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.htw.vis.clustering.feature.ByteFeature;
import org.htw.vis.clustering.feature.Feature;
import org.htw.vis.db.Database;
import org.htw.vis.layer.ZoomLayer;

import com.spaceprogram.kittycache.KittyCache;

/**
 * feature access
 * @author timo
 *
 */
public class FeatureAccess {
	/* feature cache */
	private static final int CACHE_SIZE = 1024*8;
	private static final int CACHE_TIME = 300;	
	
	private static final KittyCache<Integer,ByteFeature> cache = new KittyCache<Integer,ByteFeature>(CACHE_SIZE);
	private static final File cachePath = new File("../cache/features/");
	
	private static final HashMap<Integer,ByteFeature> featureMap = new HashMap<Integer,ByteFeature>(); 
	
	/* database */
	private Connection con;
	private PreparedStatement selectFeatureStmt;
	
	/* vector */
	private final int vectorSize = 60;
	
	/* logging */
	private final Logger log = Logger.getLogger("FeatureHelper");
	
	/* stats */
	private static AtomicInteger totalHits = new AtomicInteger();
	private static AtomicInteger memHits = new AtomicInteger();
	private static AtomicInteger diskHits = new AtomicInteger();
	private static AtomicInteger dbHits = new AtomicInteger();

	/**
	 * create feature access
	 */
	public FeatureAccess() {		
		try {
			// create an connection instance
			con = Database.get(Database.FEATURES).getConnection();
			
			// create the statement
			selectFeatureStmt = con
					.prepareStatement("select feature_vectors from image where fotolia_id = ?");
			
			// init
			if(cachePath.exists() == false){
				log.info("Creating feature cache: " + cachePath);
				cachePath.mkdirs();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		} 				
	}	
	
	public void preload(ZoomLayer layer){
	
		try {			
			ResultSet nr = layer.queryNodes("1 order by fotoliaId");
			int id = -1;
			while(nr.next()){
				id = nr.getInt("fotoliaId");				
				featureMap.put(id, getFeature(id));
			}			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * read a ByteFeature from resultSet
	 * @param rs
	 * @return
	 */
	private ByteFeature readFeatureFromResultSet(ResultSet rs){		
		try {
			Blob blob;
			blob = rs.getBlob("feature_vectors");
			return new ByteFeature(blob.getBytes(1, vectorSize));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		return null;
	}
	
	public static void printStats(){
		int total = totalHits.incrementAndGet();
		int m = ((100 * memHits.get()) / total);
		int dsk = ((100 * diskHits.get()) / total);
		int db = ((100 * dbHits.get()) / total);
		System.out.println("* Feature HITS");			
		System.out.println("\tmem  \t" + m + " %");
		System.out.println("\tdisk \t" + dsk + " %");
		System.out.println("\tdb   \t" + db + " %");
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public ByteFeature getFeature(Integer id) {
		int total = totalHits.incrementAndGet();			
		
		if(total > 100000){					
			totalHits.set(1);
			memHits.set(0);
			diskHits.set(0);
			dbHits.set(0);
		}
		
		ByteFeature feature = featureMap.get(id);
		if(feature != null){
			return feature;
		}
		
		// I) Try in memory cache
		feature = cache.get(id);
		if(feature != null){					
			memHits.incrementAndGet();
			return feature;
		}
		
		// II) Try read feature from disk
		/*feature = readFeatureFromDisk(id);
		if(feature != null){
			// cache feature in memory
			cache.put(id, feature,CACHE_TIME);
			diskHits.incrementAndGet();
			return feature;
		}*/
		
		// III) Read feature from database
		feature = readFeatureFromDatabase(id);
		if(feature != null){
			// TODO: Cache on disk (asynch writer?)
			//cacheFeatureOnDisk(id, feature);

			// Cache in memory
			cache.put(id, feature,CACHE_TIME);
			
			dbHits.incrementAndGet();
			
			return feature;
		}else{		
			// when we reach this its an error		
			log.error("Could not load feature for " + id);
			return null;
		}		
	}
	
	/**
	 * cache (serialize) object in file
	 * @param id
	 * @param feature
	 */
	private void cacheFeatureOnDisk(Integer id, ByteFeature feature) {
		// get location
		File fp = new File(getFeaturePath(id));
		File fpp = fp.getParentFile();
		if(!fpp.exists()){fpp.mkdirs();}
		
		// write object to file		
		try {
			FileOutputStream fos = new FileOutputStream(fp);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(feature);
			os.close();
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}

	/**
	 * retrieve the feature path for given id
	 * @param id
	 * @return
	 */
	private String getFeaturePath(Integer id){	
		int n = 8;
		int m = (int) Math.pow(2, n);
		
		int a =  id >> n;
		int b = a >> n;		
		int j = a > m ? a % m : a;
		
		
		return (cachePath.getAbsolutePath() + "/" + b + "/" + j + "/" + id);
	}
	
	/**
	 * read feature from disk
	 * @param id
	 * @return
	 */
	private ByteFeature readFeatureFromDisk(Integer id) {
		return readFeatureFromFile(new File(getFeaturePath(id)));				
	}

	/**
	 * load (de-serialize) feature from given file
	 * @param fp
	 * @return
	 */
	private ByteFeature readFeatureFromFile(File fp) {
		if(fp.exists()){			
			try {
				ByteFeature feature = null;
				FileInputStream fis = new FileInputStream(fp);
				ObjectInputStream os = new ObjectInputStream(fis);
				feature = (ByteFeature) os.readObject();
				os.close();
				fis.close();
				return feature;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}			
		}else{
			return null;
		}		
	}

	/**
	 * read feature from database
	 * @param id
	 * @return
	 */
	private ByteFeature readFeatureFromDatabase(Integer id){
		try {
			selectFeatureStmt.setInt(1, id);
			ResultSet rs = selectFeatureStmt.executeQuery();
			if (rs.next()) {
				return readFeatureFromResultSet(rs);		
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * shutdown the feature access
	 * this means closing the database connection
	 */
	public void shutdown() {
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * calculate the distance between two images
	 * @param a
	 * @param b
	 * @return
	 */
	public double getDistance(Integer a, Integer b) {
		Feature fA = getFeature(a);
		Feature fB = getFeature(b);
		return fA.getDistance(fB);		
	}
}
