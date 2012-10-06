package org.htw.vis.lucene;

import org.apache.lucene.document.Document;


/**
 * lucene callback interface
 * @author timo
 *
 */
public interface LuceneQueryCallback {
	/**
	 * on query callback 
	 * @param docId
	 * @param score
	 * @param searcher
	 */
	public boolean onQueryCallback(Integer docId, float score, LuceneSearch searcher);
	
	
	
	/**
	 * simple implementation
	 * with min score
	 * @author timo
	 *
	 */
	public abstract class SimpleQueryCallback implements LuceneQueryCallback {
		
		private float minScore;

		public SimpleQueryCallback(float minScore){
			this.minScore = minScore;
		}
		
		public SimpleQueryCallback(){
			this.minScore = 0.0f;
		}
		
		public abstract void  onSearchResult(SearchResult sr);

		@Override
		public boolean onQueryCallback(Integer docId, float score,
				LuceneSearch searcher) {
			
			if(score >= minScore){
				Document d = searcher.getDoc(docId);
				onSearchResult(new SearchResult(d, docId, score));
				return true;
			}
			
			return false;
			
		}
		
	}	
}
