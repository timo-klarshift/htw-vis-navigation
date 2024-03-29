package vis.lucene;

import vis.config.VisConfig
import vis.logging.VisLog


public class SearchTool {
	private static void search(Searcher s, String q, int max, SearchCallback sb){
		s.search(q, max, sb);
		
	}
	
	private static void mlt(Searcher s, String q, int max, SearchCallback sb){
		s.moreLike(q, max, sb);		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		def conf = VisConfig.load()
		VisLog.initLogging();
		
		// get index file
		File indexPath = new File(IndexFactory.getImageIndexPath(0));		
		
		// create searcher
		Searcher s = new Searcher(indexPath);
		SearchCallback sb = new SearchCallback() {

			@Override
			public boolean onResult(SearchResult r) {
				System.out.println("\t" + r);
				return true;
			}
		};
				
		def reader = new BufferedReader(new InputStreamReader(System.in))		
		while(true){
			print "Your query: "
			String query = reader.readLine()
			if(query == "exit"){
				break;
			}
			
			// do search (TODO: or mlt)
			search(s, query, 10000, sb);					
		}								
			
		// cleanup
		s.close();
		
		System.out.println("bye!");
	}

}
