package vis.importer;

import vis.config.VisConfig
import vis.logging.VisLog
import vis.lucene.IndexFactory

public class InitialImporter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// init logging
		VisLog.initLogging()
		
		// get config
		VisConfig config = VisConfig.load()
		
		// 
		String csvDir = config.getDataPath() + "/" + config.data.csv.source.small.dir
		int maxFiles = config.importer.maxFiles
		int maxImages = config.importer.maxImages
		float minDF = config.importer.tagCollector.minDocFrequency
		int minWordCount = config.importer.csv2database.minWordCount
		
		
		// 
		create(new File(csvDir), maxFiles, maxImages, minDF, minWordCount);
	}
	
	/**
	 * create initial data set
	 * @param csvFolder source CVS folder
	 * @param maxFiles maximum files to process (not images)
	 * @param minDF minimum frequency a word has to occure to be part of image tags
	 * @param minWordCount minimum words an image has to have left over before storing
	 */
	public static void create(File csvFolder, int maxFiles, int maxImages, float minDF, int minWordCount){
		CSVReader r = new CSVReader(maxFiles);
		
		// 1st: collect tags / analyze
		CSVTagCollector col = new CSVTagCollector(minDF);
		r.setCallback(col);
		r.importDirectory(csvFolder);

		// 2nd: write to database
		CSV2Database iw = new CSV2Database(col, minWordCount, maxImages);
		r.setCallback(iw);
		r.importDirectory(csvFolder);
		
		// 3rd: index first layer
		String indexPath = IndexFactory.getImageIndexPath(0)
		DbIndexer indexer = new DbIndexer(0, indexPath, VisConfig.get().index.bulkSize);
		indexer.index();
		indexer.shutdown();
	}

}