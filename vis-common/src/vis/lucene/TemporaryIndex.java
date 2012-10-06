package vis.lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TemporaryIndex extends Indexer {

	public TemporaryIndex(File indexPath) {
		super(indexPath, true);
	}
	
	public static TemporaryIndex create(){
		Path tempPath;
		try {
			tempPath = Files.createTempDirectory("temp-index");			
			return new TemporaryIndex(tempPath.toFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;		
	}




}
