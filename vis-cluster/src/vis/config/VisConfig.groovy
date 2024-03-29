package vis.config

import org.apache.log4j.Logger

import vis.logging.VisLog


public class VisConfig extends ConfigObject {
	private static VisConfig instance;
	
	private String _environment = null
	private Properties _properties
	
	private static Logger log = Logger.getLogger("VisConfig")
		
	private VisConfig(String environment = null){
		this._environment = environment
		
		// read defaults	
		readDefaultConfig()
	}
	
	public String getDataPath(){
		return data.baseDir
	}	
	
	public String getEnvironment(){		
		return _environment;
	}
	
	public void readDefaultConfig(){
		readFromClass(DefaultConfig, _environment)
	}
	
	public void readFromClass(Class configClass, String environment = null){
		environment = environment ?: this._environment		
		merge(new ConfigSlurper(environment).parse(configClass))
		update()
	}
	
	public void readProperties(File pFile){
		log.info("Reading $pFile")
		Properties p = new Properties()
		p.load(new FileInputStream(pFile))
		
		if(p != null){			
			merge(new ConfigSlurper().parse(p))
			update()
		}else
			log.error ("Could not read $pFile")
	}
	
	private void update(){
		_properties = toProperties()
	}

		
	public static VisConfig load(String environment = null){
		if(instance == null){
			instance = new VisConfig(environment) 			
		}
		
		return instance	
	}
	
	public static VisConfig get(){
		return instance
	}
	
	
	static main(args) {
		VisLog.initLogging()
		def c = VisConfig.load()
		c.readProperties(new File("./conf/importer.properties"))
		println c
	}
	
}
