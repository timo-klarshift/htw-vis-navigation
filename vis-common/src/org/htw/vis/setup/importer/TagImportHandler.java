package org.htw.vis.setup.importer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import org.htw.vis.db.ConnectionFactory;
import org.htw.vis.db.QueryCallback;
import org.htw.vis.helper.KeywordHelper;

/**
 * tag handler for importing tags
 * @author timo
 *
 */
public class TagImportHandler implements ConnectionFactory, QueryCallback {
	

	private TagImporter importer;

	public TagImportHandler(TagImporter importer) {
		this.importer = importer;
	}
			
	@Override
	public void onQueryResult(ResultSet rs) {
		KeywordHelper kw = new KeywordHelper();
		Set<String> words;
		
		
		
		int fotoliaId, size, c;
		
		try {
			while(rs.next()){
				fotoliaId = rs.getInt("fotoliaId");
				words = kw.getKeywords(fotoliaId);
				size = words.size();
				c=0;
				StringBuilder sb = new StringBuilder();
				
				if (words.size() >= TagImporter.MIN_KEYWORDS) { 						 
					for(String t : words) {
						sb.append(t);
						if(++c < size)sb.append(",");							
					}
					
					// set tags
					importer.updateTags(fotoliaId, sb.toString());						
				}								
			}
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		kw.shutdown();
	}

	@Override
	public Connection getConnection() {
		return importer.getDestinationDb().getConnection();
	}

}