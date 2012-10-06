package org.htw.vis.setup.importer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.htw.vis.db.ConnectionFactory;
import org.htw.vis.db.Database;
import org.htw.vis.db.QueryCallback;

public class TagCollectHandler implements ConnectionFactory, QueryCallback {
	private static final double MIN_DISTRIBUTION = 0.001;
	private TreeMap<String, Integer> wordMap = new TreeMap<String, Integer>();
	private Database destinationDb;
	LinkedHashMap<String, Integer> finalMap;

	public TagCollectHandler(Database destinationDb) {
		this.destinationDb = destinationDb;
	}

	@Override
	public void onQueryResult(ResultSet rs) {
		try {
			while (rs.next()) {
				// int fotoliaId = rs.getInt("fotoliaId");
				String words = rs.getString("words");
				if (words != null) {
					StringTokenizer tk = new StringTokenizer(words, ",");
					while (tk.hasMoreTokens()) {
						String token = tk.nextToken().trim();
						add(token);
					}
				}

			}
			//System.out.println(wordMap.size());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private synchronized void add(String token) {
		if (wordMap.containsKey(token)) {
			wordMap.put(token, wordMap.get(token) + 1);
		} else {
			wordMap.put(token, 1);
		}
	}
	
	public int getFullCount(){
		return wordMap.size();
	}
	
	public float getReductionFactor(){
		return (float) finalMap.size() / wordMap.size();
	}

	public LinkedHashMap<String,Integer> getFinalKeywords() {
		Map<String, Integer> sortedMap = sortByValue(wordMap);
		finalMap = new LinkedHashMap<String, Integer>();
		
		int m = sortedMap.size();
		for (Entry<String, Integer> e : sortedMap.entrySet()) {
			String w = e.getKey();
			int a = e.getValue();
			double p = (double) a / m;
			
			if(p > MIN_DISTRIBUTION){				
				finalMap.put(w, a);				
			}
		}
		
		return finalMap;
	}

	@Override
	public Connection getConnection() {
		return destinationDb.getConnection();
	}

	Map<String, Integer> sortByValue(Map<String, Integer> map) {
		List list = new LinkedList(map.entrySet());
		Collections.sort(list, new Comparator() {
			public int compare(Object o2, Object o1) {
				return ((Comparable) ((Map.Entry) (o1)).getValue())
						.compareTo(((Map.Entry) (o2)).getValue());
			}
		});

		Map result = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

}