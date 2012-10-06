package org.htw.vis.db.concurrency;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.htw.vis.db.ConnectionFactory;
import org.htw.vis.db.QueryCallback;

/**
 * query task
 * @see ConcurrentQueryExecutor
 * @author timo
 */
public class QueryTask implements Runnable {	
	private final String query;	
	private final ConcurrentQueryExecutor executor;
	
	/**
	 * create a query task
	 * @param query
	 * @param conFactory
	 * @param callback
	 */
	public QueryTask(ConcurrentQueryExecutor executor, String query){
		this.query = query;		
		this.executor = executor;
	}

	@Override
	public void run() {
		// get a connection
		Connection c = executor.getConnection();
		
		try{
			// get statement and result set
			Statement stmt = c.createStatement();			
			ResultSet rs = stmt.executeQuery(query);
			
			// query callback
			executor.onQueryResult(rs);
			
			// clean up
			rs.close();
			stmt.close();
			c.close();
		}catch(SQLException e){
			e.printStackTrace();
		}			
	}
	
	/**
	 * string representation
	 */
	public String toString(){
		return query;
	}

}
