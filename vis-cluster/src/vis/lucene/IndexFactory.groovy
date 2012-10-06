package vis.lucene

import vis.config.VisConfig

class IndexFactory {

	static String getImageIndexPath(int layer){
		return VisConfig.load().index.baseDir + "/images_$layer"
	}

}
