package org.htw.vis.setup.importer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

public class Dictionary {
	
	private LinkedHashMap<String, Integer> words = new LinkedHashMap<String, Integer>();

	public Dictionary(LinkedHashMap<String, Integer> words){
		this.words = words;				
	}
	
	public Set<String> getWords(){
		return words.keySet();
	}
	
	public Dictionary(File file){
		try {
			FileInputStream fis = new FileInputStream(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
			
			String line = null;
			StringTokenizer tk;
			while((line = reader.readLine()) != null){
				tk = new StringTokenizer(line, ":");
				String word = tk.nextToken();
				Integer count = Integer.parseInt(tk.nextToken());
				words.put(word, count);
			}
			
			reader.close();
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void save(File file){
		FileOutputStream os;
		try {
			os = new FileOutputStream(file);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "utf-8"));
			for(Entry<String, Integer> e : words.entrySet()){
				writer.write(e.getKey() + ":" + e.getValue() + "\r\n");
			}
			writer.flush();
			writer.close();
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
