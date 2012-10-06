package org.htw.vis.lucene;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.apache.lucene.util.Version;

/**
 * lucene search
 * 
 * @author timo
 * 
 */
public class LuceneSearch {
	private final LuceneIndex index;
	private IndexSearcher searcher;
	private IndexReader reader;

	private final Logger log = Logger.getLogger(this.getClass());
	private MoreLikeThis mlt;
	
	public IndexSearcher getSearcher(){
		return searcher;
	}

	/**
	 * create lucene search on given index
	 * 
	 * @param index
	 */
	public LuceneSearch(LuceneIndex index) {
		// store index
		this.index = index;

		// get reader and searcher
		try {
			reader = IndexReader.open(index.getDirectory(), true);
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// get reader
		if (reader != null) {
			searcher = new IndexSearcher(reader);
			log.info("Created LuceneSearch");
		} else {
			log.error("Could not create index reader.");
		}
		
		// create more like this
		mlt = new MoreLikeThis(reader);
		mlt.setAnalyzer(index.getAnalyzer());
		//mlt.setBoost(true);
		//mlt.setMaxDocFreqPct(50);
		mlt.setMinDocFreq(3);
		//mlt.setMaxNumTokensParsed(20);
		mlt.setMaxQueryTerms(10);
		
		
		mlt.setMaxWordLen(12);		
		mlt.setMinWordLen(3);
		
		mlt.setMinTermFreq(1);
		
		mlt.setFieldNames(new String[]{"words"});
		
	}

	/**
	 * find more like given sample
	 * 
	 * @param sample
	 * @param maxHits
	 * @param minScore
	 * @return
	 */
	public ArrayList<SearchResult> moreLikeThis(String sample, int maxHits,
			LuceneQueryCallback cb) {
	
		return query(createMoreLikeThisQuery(sample), maxHits, cb);
	}
	
	public Query createMoreLikeThisQuery(String sample){			
		Query mltq;
		try {
			mltq = mlt.like(new StringReader(sample),"words");
			
			
			return mltq;  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}
	
	public ArrayList<SearchResult> moreLikeThis(Integer docId, int maxHits, LuceneQueryCallback cb){
		try {
			return moreLikeThis(searcher.doc(docId).get("words"), maxHits, cb);
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * search with lucene
	 * 
	 * @param query
	 * @param maxHits
	 * @param minScore
	 * @return
	 */
	public ArrayList<SearchResult> search(String query, int maxHits,
			LuceneQueryCallback cb) {
		try {
			// build query
			Query q = new QueryParser(Version.LUCENE_35, "words",
					index.getAnalyzer()).parse(query);

			return query(q, maxHits, cb);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	public Query createSearchQuery(String query, String field){
		try {
			return  new QueryParser(Version.LUCENE_35, field,
					index.getAnalyzer()).parse(query);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public int maxDoc(){
		return searcher.maxDoc();
	}
	
	public IndexReader getReader(){
		return reader;
	}
	
	/**
	 * get document by its id
	 * @param id
	 * @return
	 */
	public Document getDoc(Integer id){
		try {
			return searcher.doc(id);
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * query with lucene
	 * 
	 * @param q
	 * @param maxHits
	 * @param minScore
	 * @return
	 */
	public ArrayList<SearchResult> query(Query q, int maxHits, LuceneQueryCallback callback) {
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();

		try {
			// perform search
			TopScoreDocCollector collector = TopScoreDocCollector.create(
					maxHits, true); // we want to skip the sample itself
												
			searcher.search(q, collector);			

			TopDocs topDocs = collector.topDocs();
			ScoreDoc[] hits = topDocs.scoreDocs;

			float score;
			Document d;
			int docId;

			for (int i = 0; i < hits.length; i++) {
				// get document
				docId = hits[i].doc;
				d = searcher.doc(docId);

				// get score
				score = hits[i].score / topDocs.getMaxScore();
											
				// callback
				if(!callback.onQueryCallback(docId, score, this)){
					break;
				}
			}
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return results;
	}

	
}
