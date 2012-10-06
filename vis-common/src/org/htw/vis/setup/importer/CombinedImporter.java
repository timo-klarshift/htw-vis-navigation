package org.htw.vis.setup.importer;

import org.htw.vis.layer.ZoomWorld;
import org.htw.vis.setup.NodeIndexer;
import org.htw.vis.setup.WorldCreator;


/**
 * image importer
 * 
 * @author timo
 * 
 */
public class CombinedImporter  {	

	/**
	 * importer entry point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ZoomWorld.create(1);
		ImageImporter importer = new ImageImporter(1000*1000);
		importer.process();
		
		TagImporter tImporter = new TagImporter();
		tImporter.process();
				
		/*TagReducer reducer = new TagReducer();
		reducer.process();*/
		
		new NodeIndexer(0, 10).indexAll();

		// exit
		System.exit(0);
	}
}
