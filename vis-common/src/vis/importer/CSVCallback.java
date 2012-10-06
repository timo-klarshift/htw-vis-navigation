package vis.importer;

import java.io.File;

/**
 * CSV callback
 * @author timo
 *
 */
public interface CSVCallback {
	/**
	 * gets called after an image is parsed
	 * @param id
	 * @param url
	 * @param tags
	 */
	public void onImage(String id, String url, String tags);
	
	/**
	 * gets called when file processing is done 
	 * @param file
	 */
	public void onFileDone(File file);
	
	/**
	 * gets called when all files have been processed
	 */
	public void onDone();
	
	/**
	 * get called before the process stars
	 */
	public void onStart();
}
