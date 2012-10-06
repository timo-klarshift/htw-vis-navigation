package org.htw.vis.lucene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.htw.vis.clustering.feature.ByteFeature;
import org.htw.vis.helper.FeatureAccess;
import org.htw.vis.layer.INode;
import org.htw.vis.layer.NetworkNode;

/**
 * image retriever
 * 
 * @author timo
 * 
 */
public class ImageRetriever {
	private final LuceneSearch search;
	private final FeatureAccess features = new FeatureAccess();
	private Comparator<SearchResult> comparator;
	private final Logger log = Logger.getLogger("IR");

	private final float minCombinedScore = 0.0f;
	public static final double VISUAL_D_NORM =  1.0/2200.0;

	/**
	 * create image retriever
	 * 
	 * @param search
	 */
	public ImageRetriever(LuceneSearch search) {
		this.search = search;

		// create similarity comparator
		this.comparator = new Comparator<SearchResult>() {

			@Override
			public int compare(SearchResult o1, SearchResult o2) {
				Float s1 = o1.getSimilarity();
				Float s2 = o2.getSimilarity();
				return s2.compareTo(s1);
			}

		};
	}

	public ArrayList<SearchResult> getSimilarImages(List<INode> sampleSet,
			final int maxResults, final float minSemanticScore,
			final float minVisualScore) {
		final ArrayList<SearchResult> results = new ArrayList<SearchResult>();

		final float visualAlfa = 0.99f;
		
		ArrayList<SearchResult> sorted = new ArrayList<SearchResult>();


		// create combined featureVector
		final ByteFeature combinedFeature = new ByteFeature(60, (byte) 0);
		byte[] cba = combinedFeature.getVector();

		// build lucene query
		StringBuffer sb = new StringBuffer();
		double norm = 1.0/sampleSet.size();
		int[] ia = new int[60]; 
		for(int c=0; c<60; c++)ia[c] = 0;
		for (INode n : sampleSet) {
			sb.append(n.getWords());
			sb.append(",");
			ByteFeature f = features.getFeature(n.getFotoliaId());
			byte[] ba = f.getVector();
			for (int b = 0; b < 60; b++) {
				ia[b] += ba[b];
				
			}
		}
		
		
		for(int c=0; c<60; c++){
			cba[c] = (byte) (norm*ia[c]);
		}
		Query q = search.createMoreLikeThisQuery(sb.toString());

		// perform search
		//System.out.println(sb.toString());
		search.query(q, maxResults, new LuceneQueryCallback() {

			@Override
			public boolean onQueryCallback(Integer docId, float semanticScore,
					LuceneSearch searcher) {
				
				if(results.size() >= maxResults){
					return false;
				}
				
			
				// get document
				Document currentDoc = searcher.getDoc(docId);
				Integer fotoliaId = Integer.parseInt(currentDoc
						.get("fotoliaId"));
				

				// score threshold
				if (semanticScore >= minSemanticScore) {
					
					// get visual measure
					double visualDistance = combinedFeature
							.getDistance(features.getFeature(fotoliaId));
					//System.out.println("D="+visualDistance);
					float visualScore = 1.0f - (float) (visualDistance * VISUAL_D_NORM);
					
					//System.out.println("F + " + docId + " s=" + semanticScore + " / v =" + visualScore);
					
					if (visualScore >= minVisualScore) {
						// combine scores
						float combinedScore = (visualAlfa * visualScore)
								+ ((1.0f - visualAlfa) * semanticScore);

						if (combinedScore >= minCombinedScore) {
							// emit search result
							results.add(new SearchResult(currentDoc, docId,
									combinedScore));
						}
					}
				}
				
				return true;
			}

		});
		
		// sort images
		Collections.sort(results, comparator);
		for(int r=0; r<Math.min(maxResults, results.size()); r++){
			SearchResult rs = results.get(r);
			sorted.add(rs);
		}



		// return results
		return sorted;
	}

	public ArrayList<SearchResult> getSimilarImages(final NetworkNode node,
			int maxResults, final float minSemanticScore,
			final float minVisualScore, final float visualAlfa) {
		// create moreLikeThis query
		Query q = search.createMoreLikeThisQuery(node.getWords());

		final ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		ArrayList<SearchResult> sorted = new ArrayList<SearchResult>();

		// perform search
		search.query(q, maxResults , new LuceneQueryCallback() {

			@Override
			public boolean onQueryCallback(Integer docId, float semanticScore,
					LuceneSearch searcher) {

				// get document
				Document currentDoc = searcher.getDoc(docId);
				Integer fotoliaId = Integer.parseInt(currentDoc
						.get("fotoliaId"));

				//if (!fotoliaId.equals(node.getFotoliaId())) {

					// score threshold
					if (semanticScore >= minSemanticScore) {

						// get visual measure
						double visualDistance = features.getDistance(
								node.getFotoliaId(), fotoliaId);
						float visualScore = 1.0f - (float) (visualDistance / VISUAL_D_NORM);

						if (visualScore >= minVisualScore) {
							// combine scores
							float combinedScore = (visualAlfa * visualScore)
									+ ((1.0f - visualAlfa) * semanticScore);

							if (combinedScore >= minCombinedScore) {
								// emit search result
								results.add(new SearchResult(currentDoc, docId,
										combinedScore));
							}
						}
					}
				//}
					
					return true;
			}
		});

		// sort images
		Collections.sort(results, comparator);
		for(int r=0; r<Math.min(maxResults, results.size()); r++){
			SearchResult rs = results.get(r);
			sorted.add(rs);
		}

		log.debug("Found " + sorted.size() + " similar images for "
				+ node.getFotoliaId());

		// return results
		return sorted;
	}

	public void shutdown() {
		log.debug("Shut down image retriever...");
		features.shutdown();
	}
}
