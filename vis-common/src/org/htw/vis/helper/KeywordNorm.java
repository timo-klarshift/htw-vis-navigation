package org.htw.vis.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * keyword normalization 
 * TODO keep track of count of resulting keywords, should be not that much
 * @author timo
 *
 */
public class KeywordNorm {
	private final ArrayList<String> keywords = new ArrayList<String>();
	private final HashMap<String, Integer> histogram = new HashMap<String, Integer>();

	int max = 1;

	public KeywordNorm() {

	}
	
	public int count(){
		return keywords.size();
	}

	public void add(String[] keywords) {
		for (int i = 0; i < keywords.length; i++) {
			addWord(keywords[i]);
		}
	}

	public void addWord(String keyword) {

		Integer m = histogram.get(keyword);
		if (m == null) {
			keywords.add(keyword);
			histogram.put(keyword, 1);
		} else {
			histogram.put(keyword, ++m);
			if (m > max) {
				max = m;
			}
		}

	}

	public ArrayList<String> normalize() {
		ArrayList<String> list = new ArrayList<String>();
		int t = (int) Math.max(0.1 * max, 1);
		t=0;
		int l = 0;
		for (String k : keywords) {
			l = histogram.get(k);
			if (l >= t) {
				for (int o = 0; o < l; o++)
					list.add(k);
			}
		}

		return list;
	}

	public String getNormalizedString() {
		StringBuilder s = new StringBuilder();
		Iterator<String> i = normalize().iterator();
		while (i.hasNext()) {
			s.append(i.next());
			if (i.hasNext())
				s.append(",");
		}
		return s.toString();
	}

}
