package org.htw.vis.db;

import java.sql.ResultSet;

import org.htw.vis.db.concurrency.ConcurrentQueryExecutor;

/**
 * simple query callback
 * @see ConcurrentQueryExecutor
 * @author timo
 *
 */
public interface QueryCallback {
	public void onQueryResult(ResultSet rs);
}
