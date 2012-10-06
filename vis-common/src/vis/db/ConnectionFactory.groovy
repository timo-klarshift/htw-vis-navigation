package vis.db

import java.sql.Connection
import java.sql.DriverManager

import vis.config.VisConfig

class ConnectionFactory {
	
	static Connection get(String url, String user, String password){
		Connection c = DriverManager.getConnection( url , user , password );
		return c;
	}
	
	static Connection getByName(String name){
		def config = VisConfig.get().database.source
		def dbData = config[name]
		return get(dbData.url, dbData.user, dbData.password)		
		
	}

	static main(args) {
		VisConfig.load()
		def c =  ConnectionFactory.getByName("features")
		println c
		c.close()
	}

}
