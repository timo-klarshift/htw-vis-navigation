package vis.lucene;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.mlt.MoreLikeThisQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Searcher {
	private IndexReader reader;
	private IndexSearcher searcher;
	private Analyzer analyzer;
	private File indexPath;
	private Logger log = Logger.getLogger(this.getClass());

	public Searcher(File indexPath) {
		this.indexPath = indexPath;

		init();
	}
	
	public int numDocs(){
		return searcher.getIndexReader().numDocs();
	}

	private void init() {
		try {
			reader = DirectoryReader.open(FSDirectory.open(indexPath));
		} catch(FileNotFoundException e){
			log.error("Invalid index path: " + indexPath);
			e.printStackTrace();			
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		searcher = new IndexSearcher(reader);
		analyzer = new StandardAnalyzer(Version.LUCENE_40);
	}
	
	public IndexSearcher getSearcher(){
		return searcher;
	}
	
	
	
	public Document get(String id){
		
		try {			
			TopDocs docs = searcher.search(new TermQuery(new Term("id", id)), null, 1);
			if(docs.totalHits > 0){
				return searcher.doc(docs.scoreDocs[0].doc);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
			
	}
	
	public Document getByTerm(String termName, String expr){
		
		try {
			
			TopDocs docs = searcher.search(new TermQuery(new Term(termName, expr)), null, 1);
			if(docs.totalHits > 0){
				return searcher.doc(docs.scoreDocs[0].doc);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
			
	}
	
	
	
	public void doQuery(Query query, int max, SearchCallback cb){
		try {			
			TopDocs topDocs = searcher.search(query, null, max);
			//System.out.println("Found: " + topDocs.totalHits);
			double maxScore = topDocs.getMaxScore();
			for(ScoreDoc d : topDocs.scoreDocs){
				int docId = d.doc;
				double score = d.score / maxScore;
				if(cb.onResult(new SearchResult(searcher.doc(docId), score)) == false){
					break;
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void search(String q, int max, SearchCallback cb) {
		QueryParser parser = new QueryParser(Version.LUCENE_40, "words",
				analyzer);
				
		try {
			doQuery(parser.parse(q), max, cb);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void moreLike(String q, int max, SearchCallback cb){
		String likeText = q;
		MoreLikeThisQuery mltq = new MoreLikeThisQuery(likeText, new String[]{"words"}, analyzer, "words");
		mltq.setMinTermFrequency(0);
		mltq.setMinDocFreq(0);
		doQuery(mltq, max,  cb);
	}
	
	public void close(){
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
