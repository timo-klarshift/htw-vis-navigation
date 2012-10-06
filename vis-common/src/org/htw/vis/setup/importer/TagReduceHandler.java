package org.htw.vis.setup.importer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.htw.vis.db.ConnectionFactory;
import org.htw.vis.db.Database;
import org.htw.vis.db.QueryCallback;

public class TagReduceHandler implements ConnectionFactory, QueryCallback {
	private Database destinationDb;
	LinkedHashMap<String, Integer> finalMap;
	private TagReducer reducer;

	public TagReduceHandler(TagReducer reducer, LinkedHashMap<String,Integer> finalMap, Database destinationDb) {
		this.destinationDb = destinationDb;
		this.finalMap = finalMap;
		this.reducer = reducer;
	}

	@Override
	public void onQueryResult(ResultSet rs) {
		try {
			while (rs.next()) {
				int fotoliaId = rs.getInt("fotoliaId");
				String words = rs.getString("words");
				Set<String> wordSet = new HashSet<String>();
				if (words != null) {					
					StringTokenizer tk = new StringTokenizer(words, ",");
					while (tk.hasMoreTokens()) {
						String token = tk.nextToken().trim();
						if(finalMap.containsKey(token)){
							wordSet.add(token);	
						}
					}
					
					if(wordSet.size() >= TagImporter.MIN_KEYWORDS){
					
						StringBuilder sb = new StringBuilder();
						int c = 0;
						for(String t : wordSet){
							sb.append(t);
							if(++c < wordSet.size()){
								sb.append(",");	
							}
						}
						
						// set tags
						reducer.updateTags(fotoliaId, sb.toString());	
					}
				}

			}
			//System.out.println(wordMap.size());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public Connection getConnection() {
		return destinationDb.getConnection();
	}

}