package vis.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * indexer class
 * 
 * @author timo
 *
 */
public class Indexer {
	private File indexPath;
	private boolean create;
	private IndexWriter writer;
	private long indexStart = new Date().getTime();
	private Logger log = Logger.getLogger(this.getClass());	

	public Indexer(File indexPath, boolean create) {
		this.indexPath = new File(indexPath.getAbsolutePath());
		this.create = create;
		init();
	}
	
	public void delete(){		
		close();
		log.debug("Deleting index " + indexPath);
		indexPath.delete();
	}
	
	public IndexWriter getWriter(){
		return writer;
	}
	
	public File getIndexPath(){
		return indexPath;
	}

	/**
	 * init index
	 */
	private void init() {		
		try {
			log.debug("Created Index " + indexPath);

			// open directory
			Directory dir = FSDirectory.open(indexPath);
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40,
					analyzer);

			if (create) {
				log.info("Mode=CREATE");
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				// Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
				log.info("Mode=APPEND");
			}

			// set ram buffer
			iwc.setRAMBufferSizeMB(256.0);
			
			// create writer
			writer = new IndexWriter(dir, iwc);
		} catch (IOException e) {
			log.error(" caught a " + e.getClass()
					+ "\n with message: " + e.getMessage());
		}
	}


	/**
	 * index document
	 * @param doc
	 */
	public void index(Document doc) {		
		
		try {

			if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
				writer.addDocument(doc);
			} else {
				writer.updateDocument(new Term("id", doc.get("id")), doc);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}
	}

	public void close() {
		// close writer
		try {			
			writer.forceMerge(3);
			writer.close();
		} catch(AlreadyClosedException e){
			// 
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// track runtim
		Date end = new Date();
		log.debug("Took " + (end.getTime() - indexStart) + " total milliseconds");
	}

}
