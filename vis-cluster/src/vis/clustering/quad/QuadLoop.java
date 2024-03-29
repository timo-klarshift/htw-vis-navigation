package vis.clustering.quad;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import vis.db.ConnectionFactory;
import vis.lucene.SearchCallback;
import vis.lucene.SearchResult;
import vis.lucene.Searcher;
import vis.lucene.TemporaryIndex;

/**
 * input: an index output: writes images to upper layer
 * 
 * @author timo
 * 
 */
public class QuadLoop {
	private static final int BULK_SIZE = 4*4*4*4;
	private static final double MIN_SEMANTIC_SCORE = 0.05;
	private static final double MIN_QUAD_SCORE = 0.51;

	private int maxDocs;
	private int quadCount = 0;

	private Searcher searcher;
	private IndexReader reader;

	private Quadrator quadrator;
	private QuadInspector quadInspector;

	private Set<String> processed = new HashSet<String>();
	private Set<Integer> leftOver = new HashSet<Integer>();

	private TemporaryIndex tempIndex;

	private Logger log = Logger.getLogger(this.getClass());

	private Connection con;
	private PreparedStatement insertStmt, updateStmt;

	private int destLayerId;
	private boolean lazy = false;

	public QuadLoop(Searcher searcher, int destLayerId, boolean lazy) {
		this.searcher = searcher;
		this.reader = searcher.getSearcher().getIndexReader();
		this.quadrator = new Quadrator();
		this.destLayerId = destLayerId;
		this.lazy = lazy;

		init();
	}

	private void init() {
		tempIndex = TemporaryIndex.create();
		quadInspector = new QuadInspector(quadrator);

		// get connection
		con = ConnectionFactory.getByName("images");
		try {
			insertStmt = con.prepareStatement("insert into images_"
					+ destLayerId
					+ " (fotoliaId,thumbPath,words,quadScore) values (?,?,?,?)");
			
			updateStmt = con.prepareStatement("update images_"
					+ (destLayerId-1)
					+ " set parent=? where (fotoliaId = ?) OR (fotoliaId = ?) OR (fotoliaId = ?) OR (fotoliaId = ?)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * create quads
	 */
	public void create() {
		// loop all docs
		maxDocs = reader.numDocs();
		log.info("# Having " + maxDocs + " docs // LAZY=" + lazy);

		if (!lazy) {
			for (int i = 0; i < maxDocs; i++) {
				try {
					Document d = reader.document(i);
					String id = d.get("fotoliaId");
					if (!processed.contains(id)) {
						processDocument(d);
					}									
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(i%100 == 0){
					printStats();
				}
			}
		} else {
			// winning round
			final ArrayList<SearchResult> results = new ArrayList<SearchResult>();
			try {
				for (int i = 0; i < maxDocs; i++) {
					SearchResult sr = new SearchResult(searcher.getSearcher().doc(i), 0);
					if(sr != null){
						results.add(sr);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// match
			createQuads(results);
		}
		
		// index left overs
		for(Integer lo : leftOver){
			Document d = searcher.getByTerm("fotoliaId", ""+lo);
			tempIndex.index(d);
		}

		// stats
		printStats();

		// clean up
		quadInspector.setVisible(false);
		quadInspector = null;
	}

	public void close() {
		searcher.close();
		tempIndex.close();
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public TemporaryIndex getTempIndex() {
		return tempIndex;
	}

	/**
	 * process single document
	 * 
	 * @param d
	 */
	private void processDocument(Document d) {
		// query images
		final ArrayList<SearchResult> results = new ArrayList<SearchResult>();

		// find more like that
		searcher.moreLike(d.get("words"), BULK_SIZE, new SearchCallback() {
			@Override
			public boolean onResult(SearchResult searchResult) {
				String fId = searchResult.getFotoliaId();
				if (searchResult.getScore() > MIN_SEMANTIC_SCORE
						&& !processed.contains(fId)) {
					results.add(searchResult);
				}
				return true;
			}
		});

		// match
		if(results.size() > 0){			
			createQuads(results);			
		}else{			
			markAsLeftOver(d);
		}
	}

	private void printStats() {
		double p = Math.round(100 * processed.size() / maxDocs);
		double q = Math.round(100 * (quadCount * 4.0 / processed.size()));
		log.info("Processed=" + processed.size() + " | " + p + " % ("
				+ leftOver.size() + ") // q=" + quadCount + " | quota=" + q
				+ " %");
	}
	
	private void markAsLeftOver(Document d){
		//tempIndex.index(d);
		leftOver.add(Integer.parseInt(d.get("fotoliaId")));
	}

	/**
	 * @param results
	 */
	private void createQuads(List<SearchResult> results) {
		
		// cut-off and collect ids
		// for quadrator algorithm
		int finalMatchCount = results.size()
				- (results.size() % Quadrator.CLUSTER);
		int finalQuadCount = finalMatchCount / 4;
		
		//log.info("CreateQuads(" + results.size() + ") -> " + finalMatchCount + " // " + finalQuadCount);

		// split images (those which get clustered and those getting keeped)
		int[] ids = new int[finalMatchCount];
		for (int n = 0; n < results.size(); n++) {
			SearchResult sr = results.get(n);
			
			if (n < finalMatchCount) {
				ids[n] = Integer.parseInt(sr.getFotoliaId());
			} else {
				markAsLeftOver(sr.getDocument());
			}
		}

		// not enough images for forming a quad
		if (finalQuadCount < 1) {			
			return;
		}

		// check
		if(ids.length % Quadrator.CLUSTER != 0){
			try {
				throw new Exception("Sorry: ids.length=" + ids.length);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(9);
			}
		}
		

		// perform matching
		quadrator.match(ids);

		// get ids
		int[] nodeIds = quadrator.getIds();
		double[] meanDistances = quadrator.getNodeMeanDistances();
		// int nc = nodeIds.length;

		// get quad scores
		double[] qs = quadrator.getQuadScore();
		int qc = qs.length; // quad count

		for (int q = 0; q < qc; q++) {
			double cqs = qs[q];
			if((cqs >= MIN_QUAD_SCORE) || lazy){			
				quadCount++;
				emitQuad(nodeIds, meanDistances, q, cqs);
			}else{
				/////////////				
				for(int n=0; n<Quadrator.CLUSTER; n++){
					int fid = nodeIds[q*Quadrator.CLUSTER + n];
					Document d = searcher.getByTerm("fotoliaId", ""+fid);
					markAsLeftOver(d);					
				}
			}
		}
	}

	private void emitQuad(int[] ids, double[] meanDistances, int qi, double quadScore) {
		Document r = null;
		double rMinD = Double.MAX_VALUE;
		

		StringBuilder tags = new StringBuilder();

		// loop all docs		
		ArrayList<Document> docs = new ArrayList<Document>();
		for (int n = 0; n < Quadrator.CLUSTER; n++) {
			// get according cluster index
			int ni = qi * Quadrator.CLUSTER + n;
			
			// get document by fotoliaId TODO: could be better
			Document d = searcher.getByTerm("fotoliaId", "" + ids[ni]);
			tags.append(d.get("words") + " ");
			docs.add(d);
			
			// mark
			processed.add(""+ids[ni]);
			leftOver.remove(ids[ni]);
			
			if(meanDistances[ni] < rMinD){
				rMinD = meanDistances[ni];
				r = d;				
			}					
		}
		
		if(r != null){
			// TODO: select representative with lowest distance		
			Integer fotoliaId = Integer.parseInt(r.get("fotoliaId"));
			String thumbPath = r.get("thumbPath");
			String words = tags.toString().trim();		
	
			try {
				// insert
				insertStmt.setInt(1, fotoliaId);
				insertStmt.setString(2, thumbPath);
				insertStmt.setString(3, words);	
				insertStmt.setFloat(4, (float) quadScore);
				insertStmt.execute();
				
				// set down reference
				
				updateStmt.setInt(1, fotoliaId);
				int npId = 2;
				for(Document d : docs){
					try {
						updateStmt.setInt(npId++, Integer.parseInt(d.get("fotoliaId")));
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				updateStmt.execute();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			log.error("No representative node could be found!");
		}
	}

}
