package vis.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * csv reader
 * 
 * @author timo
 *
 */
public class CSVReader {
	private Logger log = Logger.getLogger(this.getClass());
	private CSVCallback callback;
	
	private int fileCount = 0;
	private int lineCount = 0;
	private int unmatchedLineCount = 0;
	
	private int maxFiles;
	
	/**
	 * create csv reader
	 * @param maxFiles
	 */
	public CSVReader(int maxFiles) {
		this.maxFiles = maxFiles;
	}

	public void setCallback(CSVCallback callback) {
		this.callback = callback;
	}

	public void importDirectory(File directory) {
		log.info("Start import of " + directory);
		
		// init
		fileCount = 0;
		lineCount = 0;
		unmatchedLineCount = 0;
		
		callback.onStart();
		log.info("Processing " + directory);
		if (directory.exists() && directory.isDirectory()
				&& directory.length() > 0) {
			for (File f : directory.listFiles()) {
				if (f.getName().endsWith(".csv")) {
					try {
						importFile(f);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (fileCount >= maxFiles) {
					break;
				}
			}
		}
		
		System.out.println("");
		log.info("Processing " + directory + " done.");
		log.info("\t" + fileCount + " files");
		log.info("\t" + lineCount + " images");
		log.info("\t" + unmatchedLineCount + " skipped (no tags)");
		
		callback.onDone();
	}

	/**
	 * import a file
	 * @param file
	 * @throws IOException
	 */
	private void importFile(File file) throws IOException {		
		// regex pattern
		// 			     id  url	    categories		tags
		String rx = "^(\\d+),(.+\\.jpg),\"*(.+?)\"*,\"*([^\"]+)\"*,+.+?";
		Pattern p = Pattern.compile(rx);

		// read line by line
		BufferedReader r = new BufferedReader(new FileReader(file));
		String line = null;
		while ((line = r.readLine()) != null) {
			lineCount++;
			Matcher m = p.matcher(line);
			if (m.matches()) {
				String id = m.group(1);
				String url = m.group(2);
				// String categories = m.group(3);
				String tags = m.group(4);
				callback.onImage(id, url, tags);
			}else{
				unmatchedLineCount++;
			}
		}
		
		// callback
		callback.onFileDone(file);

		// progress
		System.out.print("#");
		fileCount++;
		if(fileCount % 80 == 0){
			System.out.println("");
		}
		r.close();
	}

	
}
