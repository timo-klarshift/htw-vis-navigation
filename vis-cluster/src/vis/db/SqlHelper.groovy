package vis.db

import java.sql.Connection
import java.sql.ResultSet
import java.sql.Statement

import org.apache.log4j.Logger

class SqlHelper {
	private Connection con;
	private Logger log = Logger.getLogger(this.getClass());
	
	SqlHelper(Connection con){
		this.con = con;
	}
	
	public static String readSchema(ClassLoader cl , String resourcePath){
		return cl.getResource(resourcePath).getText()
	}
	
	public boolean execute(String sql){
		Statement stmt = con.createStatement()
		return stmt.execute(sql)
	}
	
	public void createImageTable(Integer layerId, boolean drop = true){
		if(drop)
			execute("drop table if exists images_${layerId}");
			
		String schema = SqlHelper.readSchema(getClass().getClassLoader(), "images.sql");
		schema = schema.replaceAll("\\[LOD\\]", layerId.toString());
		execute(schema);
		log.info("Created image table for layer " + layerId)
	}

	public int max(String tableName) {
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("select count(*) from " + tableName);
		if(rs.next()){
			return rs.getInt(1);
		}
		return -1;
	}

}
