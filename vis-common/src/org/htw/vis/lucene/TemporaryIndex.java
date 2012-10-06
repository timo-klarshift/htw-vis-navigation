package org.htw.vis.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class TemporaryIndex {
	
	public static LuceneIndex create(ArrayList<SearchResult> images){
		String name = UUID.randomUUID().toString();
		String path = "/tmp/tmp-index-" + name;
		LuceneIndex index = LuceneIndex.register(name, path);
		
		for(SearchResult sr : images){
			try {
				index.addDoc(sr.getFotoliaId(), sr.getThumbPath(), sr.getWords());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return index;
	}
	
	
}
