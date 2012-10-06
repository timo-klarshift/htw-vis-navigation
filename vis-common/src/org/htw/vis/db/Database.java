package org.htw.vis.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.htw.vis.config.DbConfig;

/**
 * database abstraction
 * using connection pooling
 * 
 * @author timo
 * 
 */
public class Database {	
	private PoolingDataSource ds;
	private final DbConfig config;
	
	/* database names */
	public static final String LAYERS = "layers";
	public static final String FEATURES = "feature";

	private static HashMap<String, Database> databases = new HashMap<String, Database>();
	private static final Logger log = Logger.getLogger("Database");
	
	/**
	 * load drivers statically
	 */
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Class.forName("org.sqlite.JDBC");			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public DbConfig getConfig(){
		return config;
	}
	
	/**
	 * register a database
	 * 
	 * @param name
	 * @param config
	 * @return
	 */
	public static Database register(String name, DbConfig config){
		Database db = databases.get(name); 
		if(db != null){
			log.info("Database already registered.");
			return db;
		}
		
		log.info("Database registered: " + name);		
		db = new Database(config);
		databases.put(name, db);
		
		return db;
	}
	
	/**
	 * database accesor
	 * get a database after it is registered
	 * @param name
	 * @return
	 * @throws Exception 
	 */
	public static Database get(String name) throws Exception{
		Database db = databases.get(name);
		if(db == null){
			log.error("Database " + name  + " not registered.");
			throw new Exception("Database " + name  + " not registered. Register first.");
		}
		
		return db;
	}

	/**
	 * create database
	 * 
	 * @param config
	 */
	private Database(DbConfig config) {
		// store config
		this.config = config;
	
		String connectionURI = config.getConnectionURI();

		log.info("Connect to DB: " + connectionURI);

		GenericObjectPool<Connection> connectionPool = new GenericObjectPool<Connection>();
		connectionPool.setMaxActive(10);
		connectionPool.setMaxIdle(5);

		ConnectionFactory cf = new DriverManagerConnectionFactory(
				connectionURI, config.getUsername(), config.getPassword());
		
		PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, connectionPool, null, null, false, true);
		
		ds = new PoolingDataSource(connectionPool);			
	}

	/**
	 * get the database name
	 * @return
	 */
	public String getName() {
		return config.getName();
	}

	/**
	 * get a database connection
	 * from pool
	 * @return
	 */
	public Connection getConnection() {
		try {
			return ds.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * execute a query
	 * @param query
	 */
	public void execute(String query){
		Connection c = getConnection();
		try {
			Statement stmt = c.createStatement();
			stmt.execute(query);
			stmt.close();
			c.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}	
	
	/**
	 * perform count on table
	 * @param tableName
	 * @param where
	 * @return
	 */
	public Integer count(String tableName, String where){
		if(where == null){
			where = "1";
		}
		
		Connection c = getConnection();
		Integer num = -1;
		try {
			ResultSet rs = c.createStatement().executeQuery("select count(*) from " + tableName + " WHERE " + where);			
			if(rs.next()){
				num = rs.getInt(1);
			}
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}		
		return num;
	}
	
}
