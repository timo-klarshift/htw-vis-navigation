package org.htw.vis.lucene;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.NativeFSLockFactory;
import org.apache.lucene.util.Version;

/**
 * lucene indexer
 * 
 * @author timo
 *
 */
public class LuceneIndex {
	/* holder */
	private final static HashMap<String,LuceneIndex> indexMap = new HashMap<String,LuceneIndex>();
	
	/* logging */
	private final static Logger log = Logger.getLogger("LuceneIndex");
	
	/* index writer */
	private IndexWriter writer;

	/* fields */
	File indexDir;

	/* analyzer */
	private StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);
	
	private Directory directory;
			
	/**
	 * lucene indexer
	 */
	private LuceneIndex(String path){									
		indexDir = new File(path);
		try {
			directory = new NIOFSDirectory(indexDir, new NativeFSLockFactory(indexDir));
			log.info("Linked index path: " + path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public Directory getDirectory(){
		return directory;
	}
	
	/**
	 * get the index analyzer
	 * @return
	 */
	public final Analyzer getAnalyzer(){
		return analyzer;
	}
	
	/**
	 * optimize the index
	 */
	public void optimize(){
		log.info("Optimizing index ...");
		
		// force single segment and wait for optimization
		try {
			if(writer != null){
				writer.forceMerge(1);
			}
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
		
	/**
	 * get an index by its name
	 * 
	 * @param name
	 * @return
	 */
	public static LuceneIndex get(String name){		
		log.info("Get LuceneIndex: " + name);
		LuceneIndex i = indexMap.get(name);
		if(i == null){
			log.error("Index " + name + " not registered.");
			return null;
		}
		return i;
	}
	
	public static LuceneIndex register(String name, String path){
		File f = new File(path);
		path = f.getAbsolutePath();
		
		// check for registration
		LuceneIndex i = indexMap.get(name);
		if(i != null){
			log.error("Index already registered: " + name);
			return i;
		}
					
		// store
		log.info("Register LuceneIndex: " + name + " :: " + path);
		i = new LuceneIndex(path);
		indexMap.put(name, i);
		
		
		return i;
	}
	
	/**
	 * get the index writer
	 * 
	 * @return
	 */
	public IndexWriter getWriter(){
		if(writer == null){
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);
			
			config.setRAMBufferSizeMB(2048);

			// get writer
			try {
				writer = new IndexWriter(directory, config);
			} catch (CorruptIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LockObtainFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		
		return writer;
	}

	/**
	 * close the writer
	 */
	public void close(){
		IndexWriter w = writer;
		if(w != null){
			try {
				log.info("Closing index ...");
				w.close();
				while(w.isLocked(directory)){
					try {
						log.info("WAit for indexer ...");
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				indexMap.remove(indexDir.getAbsolutePath());
			} catch (CorruptIndexException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
	}
	
	/**
	 * add an document
	 * 
	 * @param fotoliaId
	 * @param thumbPath
	 * @param keywords
	 * @throws IOException
	 */
	public void addDoc(Integer fotoliaId, String thumbPath, String words) throws IOException {
		// new document
		Document doc = new Document();
		
		Field fotoliaIdField = new Field("fotoliaId", "", Field.Store.YES, Field.Index.NO);
		Field thumbPathField = new Field("thumbPath", "", Field.Store.YES, Field.Index.NO);
		Field keywordField = new Field("words", "", Field.Store.YES, Field.Index.ANALYZED,Field.TermVector.YES);

		// set fields
		fotoliaIdField.setValue(fotoliaId.toString());
		thumbPathField.setValue(thumbPath);
		keywordField.setValue(words);

		// add fields
		doc.add(fotoliaIdField); doc.add(thumbPathField) ; doc.add(keywordField);
		getWriter().addDocument(doc);
		
		log.debug("Added document to index: " + fotoliaId);
	}
	
	public void commit(){
		try {
			getWriter().commit();	
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void clear() {
		try {			
			log.info("Clearing index ... " + indexDir.getAbsolutePath());
			getWriter().deleteAll();
			getWriter().commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
