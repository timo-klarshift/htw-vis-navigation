package vis.importer;

import org.apache.log4j.Logger

import vis.config.VisConfig
import vis.logging.VisLog

/**
 * this is an example of reading the csv files
 * from fotolia
 * 
 * @author timo
 *
 */
public class CSVReaderExample implements CSVCallback {
	private Logger log = Logger.getLogger(this.getClass());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// init logging
		VisLog.initLogging();
		
		// get config
		VisConfig config = VisConfig.load()
		
		// get CVS directory
		String csvDir = config.getDataPath() + "/" + config.data.csv.source.small.dir
		if(args.size() > 0){
			csvDir = args[0]
		}
		
		// get max files to process
		int max = 100
		if(args.size() > 1) {
			max = Integer.parseInt(args[1])
		}
		
		// process			
		CSVReader r = new CSVReader(max);
		r.setCallback(new CSVReaderExample());
		r.importDirectory(new File(csvDir));
	}
	
	

	@Override
	public void onImage(String id, String url, String tags) {
		// simply display the image information
		// TODO: insert your code here ...
		// log.debug("Image Callback: ID=" + id + " / " + url + " :: " + tags);
	}

	@Override
	public void onFileDone(File file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDone() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		
	}

}
