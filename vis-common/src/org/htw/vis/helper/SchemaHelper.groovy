package org.htw.vis.helper

import java.sql.Connection


import org.htw.vis.db.Database

/**
 * layer helper class
 * executes sql from org.htw.vis.layer.schema
 * @author timo
 *
 */
class SchemaHelper {
	static void initTable(Integer layer){
		Database db = Database.get("layers")
		Connection con = db.getConnection()

		// create table for this layer
		['node'].each{
			// read sql and replace
			String sql = readSchema("layers-${it}").replace('[LOD]', layer.toString())
					.replace('[DB]', db.getConfig().getName())

			// execute query
			con.createStatement().execute(sql)
		}

		// clean up
		con.close();
	}
	
	public static String readSchema(String name){
		return name.getClass().getResourceAsStream("/org/htw/vis/layer/schema/${name}.sql").text;
	}
}
