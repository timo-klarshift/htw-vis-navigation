package org.htw.vis.config;

import java.io.File;

/**
 * database configuration
 * 
 * @author timo
 * 
 */
public class DbConfig {
	private final String hostname, username, password, name;
	private String type = "mysql";

	public static final String TYPE_MYSQL = "mysql";
	public static final String TYPE_SQLITE = "sqlite";

	/**
	 * create db config
	 * 
	 * @param name
	 * @param hostname
	 * @param username
	 * @param password
	 */
	public DbConfig(String name, String hostname, String username,
			String password, String type) {
		this.hostname = hostname;
		this.username = username;
		this.password = password;
		this.name = name;
		this.type = type;
	}

	/**
	 * create db config
	 * 
	 * @param name
	 * @param hostname
	 * @param username
	 * @param password
	 */
	public DbConfig(String name, String hostname, String username,
			String password) {
		this.hostname = hostname;
		this.username = username;
		this.password = password;
		this.name = name;
	}

	public String getHostname() {
		return hostname;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getName() {
		return name;
	}

	public String getConnectionURI() {
		if(type == TYPE_SQLITE){
			File dbFile = new File("db/" + getName() + ".db");
			if(!dbFile.getParentFile().exists()){
				dbFile.getParentFile().mkdirs();
			}
			String dbPath = dbFile.getAbsolutePath();			
			
			return "jdbc:sqlite:" + dbPath;
		}else if(type == TYPE_MYSQL){		
			return "jdbc:mysql://" + getHostname() + "/" + getName();
		}
		return null;		
	}

	/**
	 * config for remote feature database
	 * 
	 * @return
	 */
	public static DbConfig createRemoteFeaturesConfig() {
		return new DbConfig("visnav_final", "141.45.146.52", "timo_f",
				"8hfWVM9");
	}

	/**
	 * config for remote layer database
	 * 
	 * @return
	 */
	public static DbConfig createRemoteLayerConfig() {
		return new DbConfig("tf_db", "141.45.146.52", "timo_f", "8hfWVM9");
	}

	/**
	 * config for local feature database
	 * 
	 * @return
	 */
	public static DbConfig createLocalFeaturesConfig() {
		return new DbConfig("3dvis", "localhost", "3dvis", "3dvis");
	}

	/**
	 * config for local layer database
	 * 
	 * @return
	 */
	public static DbConfig createLocalLayerConfig() {
		return new DbConfig("3dvis-layers", "localhost", "3dvis", "3dvis");
	}

}
