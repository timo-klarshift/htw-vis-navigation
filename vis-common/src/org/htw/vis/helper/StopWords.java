package org.htw.vis.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * stop word helper
 * @author timo
 *
 */
public class StopWords {
	private Set<String> words = new HashSet<String>();
	
	public StopWords(){
		
		try {
			// TODO load with thread resource loader
			File f = new File("data/stopwords.txt");
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String line ;
			while((line = r.readLine()) != null){
				words.add(line.toLowerCase().trim());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean isStopWord(String string){
		return words.contains(string);
	}
}
