package vis.importer;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import vis.db.ConnectionFactory;
import vis.db.SqlHelper;

/**
 * writes images to database
 * 
 * @author timo
 *
 */
public class CSV2Database implements CSVCallback {
	
	// collector reference
	private CSVTagCollector tc;
	
	private Logger log = Logger.getLogger(this.getClass());
	
	// stats
	private int addCount = 0;
	private int skipCount = 0;	
	
	private int minWordCount = 5;
	private int maxImages = -1;
	
	private long start = -1;
	
	private PreparedStatement stmt = null;
	
	// db
	Connection con;
	
	/**
	 * create csv2database converter
	 * @param tags
	 */
	public CSV2Database(CSVTagCollector tags, int minWordCount, int maxImages) {
		this.tc = tags;
		this.minWordCount = minWordCount;	
		this.maxImages = maxImages;
		init();
	}
	
	private void init(){
		log.info("CSV2Database // minWordCount=" + minWordCount);
		
		// get connection
		con = ConnectionFactory.getByName("images");
		
		// init database
		SqlHelper h = new SqlHelper(con);
		h.createImageTable(0);
		
		// create statement
		try {
			stmt = con.prepareStatement("insert into images_0 (fotoliaId,thumbPath,words) values (?,?,?)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	

	@Override
	public void onImage(String id, String url, String tags) {		
		// get final tags for that image
		// final == tags contained in tagCollectors filtered tag set
		StringTokenizer tk = new StringTokenizer(tags, ",");
		Set<String> finalTags = new HashSet<String>();
		while(tk.hasMoreTokens()){
			String t = tk.nextToken().trim().toLowerCase();			
			if(tc.hasTag(t)){
				finalTags.add(t);
			}			
		}
		
		// add images
		if(finalTags.size() >= minWordCount){
			if(addCount < maxImages){
				addImage(id, url, finalTags);
				addCount ++;
			}else{
				log.warn("Max images=" + maxImages + " reached. Skipping: " + id);
				skipCount++;
			}
		}else{
			skipCount++;
		}
	}
	
	/**
	 * add image
	 * @param fotoliaId
	 * @param url
	 * @param tags
	 */
	private void addImage(String fotoliaId, String url, Set<String> tags){
		
		StringBuffer b = new StringBuffer();
		for(String s : tags){
			b.append(s);
			b.append(" ");
		}
		
		// write to database
		try {
			stmt.setInt(1,  Integer.parseInt(fotoliaId));
			
			String thumbPath = url.substring(24);
			stmt.setString(2, thumbPath);
			stmt.setString(3, b.toString().trim());			
			stmt.execute();		
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	@Override
	public void onFileDone(File file) {

	}

	@Override
	public void onDone() {
		log.info("Imported " + addCount + " images. Skipped " + skipCount + " images.");	
		
		double ips = 1000*((double)addCount / (new Date().getTime()-start));
		log.info("Wrote " + Math.round(ips) + " images / sec");
		shutdown();
	}
	
	public void shutdown(){
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.info("Shut down");
	}

	@Override
	public void onStart() {
		addCount = 0;
		skipCount = 0;
		start = new Date().getTime();
		log.info("Writing CVS data to database ...");
	}

}
