package vis.config

/**
 * @author timo
 * this is the basic configuration file
 * remember that this are the local (relative) paths
 * you have to adjust these to run in different environment
 */

/**
 * data paths
 */
data {
	baseDir = '../../data/'
	csv {		
		source {
			small = [dir: 'fotolia-csv-small']
		}		
	}
}

/**
 * importer settings
 */
importer {
	
	maxImages = Math.pow(4, 7) // second filter, above has to cover this
	maxFiles = Math.round(maxImages / 1000.0)+3	// first filter
	
	tagCollector {
		minDocFrequency = 0.01
	}
	
	csv2database {
		minWordCount = 2
	}
}

/**
 * database configuration
 */
database {
	// basic settings
	// 
	
	// sources
	source {
		// image database
		images {
			url = "jdbc:mysql://localhost:3306/3dvis-layers"			
			user = "3dvis"
			password = "3dvis"
		}
		
		features {
			url = "jdbc:mysql://localhost:3306/3dvis-layers"
			user = "3dvis"
			password = '3dvis'
		}
			
	}
	
}

/**
 * index configuration
 */
index{ 
	baseDir = '../../index'
	bulkSize = 10000
}


/**
 * environment specific configuration
 * this overwrites the upper configuration 
 */
environments{
	live{
		database{
			// ...
		}
	}
}