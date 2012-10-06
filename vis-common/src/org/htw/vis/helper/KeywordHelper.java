package org.htw.vis.helper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.htw.vis.db.Database;
import org.htw.vis.layer.creation.TagReducer;
import org.tartarus.snowball.ext.PorterStemmer;

/**
 * keyword helper
 * 
 * @author timo
 *
 */
public class KeywordHelper {
	private Connection con;
	private PreparedStatement selectStmt, iterateStmt;
	private static final Logger log = Logger.getLogger("KeywordHelper");
	
	private final PorterStemmer stemmer = new PorterStemmer();
	private final StopWords stopWords = new StopWords();
	
	private final int minLength = 1;
	private final int maxLength = 32;
	
	private boolean quantize = false;
	private TagReducer tagReducer;
	
	/**
	 * create keyword helper
	 */
	public KeywordHelper(){
		log.debug("Created KeywordHelper");
		
		try {
			con = Database.get(Database.FEATURES).getConnection();
			
			selectStmt = con.prepareStatement("select ik.fk_image, ik.fk_keyword, k.word from image_keyword ik left join keyword k on k.id = ik.fk_keyword left join image i on i.fotolia_id = ik.fk_image where lang_id = 2 and fk_image = ?");
			
			iterateStmt = con.prepareStatement("select ik.*, k.word, k.lang_id from image i join image_keyword ik on i.fotolia_id = ik.fk_image join keyword k on (k.id = ik.fk_keyword) having k.lang_id = 2 order by ik.fk_image limit ?,?");			
			iterateStmt.setFetchSize(100);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setQuantize(boolean q){
		this.quantize = q;
		if(q && tagReducer == null){
			tagReducer = new TagReducer();
			tagReducer.reduce();
		}
	}
	
	public final ResultSet iterateKeywords(int offset, int max){
		try {
			iterateStmt.setInt(1, offset);
			iterateStmt.setInt(2, max);
			return iterateStmt.executeQuery();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public final Set<String> getKeywords(String words){
		Set<String> keywords = new HashSet<String>();		
		StringTokenizer tok = new StringTokenizer(words, " |-_");
		while(tok.hasMoreTokens()){
			String w = tok.nextToken();
			w = w.trim().toLowerCase();
			
			if(w.matches("[a-z0-9\\.]+")){
			
				// check for stop word
				if(stopWords.isStopWord(w) == false && w.length() >= minLength && w.length() <= maxLength){
					// stem
					//w = stem(w);
					
					if(!quantize || tagReducer.has(w)){
					
						// add
						keywords.add(w);
					}
				}
			}
		}
		return keywords;
	}
		
	public String stem(String word){
		stemmer.setCurrent(word);
		stemmer.stem();
		return stemmer.getCurrent();
	}
	
	/**
	 * get keywords
	 * @param id
	 * @return
	 */
	public final Set<String> getKeywords(Integer id){
		final Set<String> keywords = new HashSet<String>();
		ResultSet rs;
		try {
			selectStmt.setInt(1, id);
			rs = selectStmt.executeQuery();
			while(rs.next()){
				// read word
				String kw = rs.getString("word").trim().toLowerCase();
				keywords.addAll(getKeywords(kw));							
			}
			rs.close();
			return keywords;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}	
	
	public void shutdown(){
		try {
			con.close();
			log.debug("Shut down. KeywordHelper");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
