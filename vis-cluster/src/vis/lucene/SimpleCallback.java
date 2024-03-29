package vis.lucene;

import java.util.ArrayList;

public class SimpleCallback implements SearchCallback {
	private ArrayList<SearchResult> results = new ArrayList<SearchResult>();
	private double minScore;
	
	public SimpleCallback(double minScore){
		this.minScore = minScore;
	}

	@Override
	public boolean onResult(SearchResult searchResult) {
		if(searchResult.getScore() >= minScore){
			results.add(searchResult);
			return true;	
		}
		return false;		
	}
	
	public ArrayList<SearchResult> getResults(){
		return results;
	}

	
}
