package org.htw.vis.config

import org.apache.log4j.Logger

/**
 * environment specific configuration
 * for database
 * 
 * it uses hostname matching to switch parameters
 * 
 * @author timo
 *
 */
public class EnvDbConfig {
	private String env
	private final Logger log = Logger.getLogger(this.getClass())
	
	/* environments */
	private final String ENV_DEVELOP		= "develop"
	private final String ENV_PRODUCTION		= "production"
	
	/**
	 * create environment specific config
	 */
	public EnvDbConfig(){
		String hostname = getHostname();
		
		// set production
		if(hostname == 'viscomp1'){
			setEnv(ENV_PRODUCTION)
		}else{
			setEnv(ENV_DEVELOP)
		}
	}
	
	static String getHostname(){
		// define environment
		InetAddress addr = InetAddress.getLocalHost()
		return addr.getHostName()
	}
	
	/**
	 * manually set the environment
	 * @param env
	 */
	public void setEnv(String env){
		this.env = env;
		log.info("Database Environment set to: $env")
	}
	
	/**
	 * get environment
	 * @return
	 */
	public String getEnv(){
		return env;
	}
	
	/**
	 * static getter
	 * @return
	 */
	public static EnvDbConfig get(){
		return new EnvDbConfig()
	}
	
	/**
	 * get the feature database configuration
	 * @return
	 */
	public DbConfig featureConfig(){
		if(env == ENV_DEVELOP){
			return DbConfig.createLocalFeaturesConfig()
		}else{
			return DbConfig.createRemoteFeaturesConfig()
		}
	}
	
	/**
	 * get the layer database configuration
	 * @return
	 */
	public DbConfig layerConfig(){
		if(env == ENV_DEVELOP){
			return DbConfig.createLocalLayerConfig()
		}else{
			return DbConfig.createRemoteLayerConfig()
		}
	}
}
