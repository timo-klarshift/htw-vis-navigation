package org.htw.vis.db;

import java.sql.Connection;

/**
 * simple connection factory interface
 * 
 * @author timo
 *
 */
public interface ConnectionFactory {
	/**
	 * get connection
	 * @return
	 */
	public Connection getConnection();
}
