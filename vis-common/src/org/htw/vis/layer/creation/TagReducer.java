package org.htw.vis.layer.creation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.htw.vis.helper.KeywordHelper;
import org.htw.vis.layer.ZoomWorld;
import org.htw.vis.lucene.LuceneIndex;

public class TagReducer {
	private LinkedHashMap<String, Integer> keywordMap = new LinkedHashMap<String, Integer>(
			100000);
	KeywordHelper keywordHelper;

	LinkedHashMap<String, Integer> finalTags = new LinkedHashMap<String, Integer>(
			100000);
	int maxO = Integer.MIN_VALUE;
	Map<String, Integer> sortedMap;
	
	public TagReducer() {
		ZoomWorld.create(0);
		keywordHelper = new KeywordHelper();
		
		

	}
	
	public boolean has(String w){
		return finalTags.containsKey(w);
	}

	public void reduce() {
		int bulk = 100000;
		for (int i = 0; i < 10; i++) {
			int read = readTags(i * bulk, bulk);

			System.out.println(keywordMap.size());
			System.out.println(read + " NEW WORDS");

		}

		// create final map
		sortedMap = sortByValue(keywordMap);

		int total = sortedMap.size();
		for (String w : sortedMap.keySet()) {

			// get word freq
			int v = sortedMap.get(w);
			if (v > -1) {

				float p = (float) v / total;

				if (p > 0.001 && p <= 1) {
					System.out.println(w + "/" + p + "/" + sortedMap.get(w));

					finalTags.put(w, v);
				}
			}

		}

		System.out.println("**** FINAL " + finalTags.size());
	}
 
	private int readTags(int offset, int maxSamples) {		
		int newCounter = 0;

		ResultSet iterator = keywordHelper.iterateKeywords(offset, maxSamples);
		try {
			while (iterator.next()) {
				String line = iterator.getString("word");
				Set<String> keywords = keywordHelper.getKeywords(line);
				for (String w : keywords) {

					if (!keywordMap.containsKey(w)) {
						newCounter++;
						keywordMap.put(w, 1);
					} else {

						int v = keywordMap.get(w);
						if (v > maxO) {
							maxO = v;
						}
						keywordMap.put(w, v + 1);
						// System.out.println(keywordMap.get(w));
					}

				}

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return newCounter;

	}

	static Map<String, Integer> sortByValue(Map<String, Integer> map) {
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

	public static void main(String[] args) {
		Logger.getRootLogger().setLevel(Level.INFO);
		new TagReducer().reduce();
	}
}
