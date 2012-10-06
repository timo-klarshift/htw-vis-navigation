package org.htw.vis.db.concurrency;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.htw.vis.db.ConnectionFactory;
import org.htw.vis.db.QueryCallback;

/**
 * concurrent executor
 * @author timo
 *
 */
public class ConcurrentQueryExecutor {
	/* task holder */
	private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
	
	private final ConnectionFactory conFactory;
	private final String query;
	private final String sequenceName;
	private final String tableName;
	
	private QueryCallback callback;
		
	private AtomicInteger remainingTaskCount = new AtomicInteger();
	private int totalTaskCount = 0;
	
	private ThreadPoolExecutor executor; 
	
	/**
	 * create a concurrent query executor
	 * @param tableName
	 * @param fields
	 * @param sequenceName
	 * @param threadCount
	 * @param conFactory
	 */
	public ConcurrentQueryExecutor(String tableName, String fields, String sequenceName, int threadCount, ConnectionFactory conFactory){
		// store params
		this.sequenceName = sequenceName;
		this.conFactory = conFactory;		
		this.query = "select " + fields + " from " + tableName;
		this.tableName = tableName;
		
		// create the executor
		executor = new ThreadPoolExecutor(threadCount, threadCount, 30, TimeUnit.MINUTES,queue);
	}
	
	private Integer countSelect(String s){
		String q = "select " + s + " from " + tableName;
		int c = -1;
		Connection con = conFactory.getConnection();
		try{
			ResultSet rs = con.createStatement().executeQuery(q);
			if(rs.next()){
				c = rs.getInt(1);
			}
			
			con.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return c;
	}
		
	/**
	 * create a task
	 * @param o
	 * @param c
	 */
	public void createTask(int min, int max){	
		String q = query + " WHERE " + sequenceName + " BETWEEN " + min + " AND " + max + " ORDER by " + sequenceName + " asc";
		System.out.println(q);
		QueryTask t = new QueryTask(this, q);		
		remainingTaskCount.incrementAndGet();
		totalTaskCount++;
		executor.execute(t);		
	}
	
	public int getRemainingTaskCount(){
		return executor.getQueue().size();
	}
	
	public float getProgress(){
		return (float) ((double)executor.getCompletedTaskCount() / (double)totalTaskCount);
	}
	
	/**
	 * stop executor
	 */
	public void stop(){		
		executor.shutdownNow();
	}
	
	public void execute(QueryCallback callback){
		// store callback
		this.callback = callback;
		
		int num = countSelect("count(*)");
		int min = countSelect("min(" + sequenceName + ")");
		int max = countSelect("max(" + sequenceName + ")");
		int range = max-min;
		
		if(num < 1){
			return;
		}
		
		System.out.println("min=" + min + ",max=" + max + ",range=" + range + ",count=" + num);			
		
		// create tasks
		int bulkSize = Math.min(1000, max);		// keep small
		int offset = min;
		while(offset <= max){
			createTask(offset, offset+bulkSize-1);
			offset += bulkSize;
		}							
	}
	
	public boolean isRunning(){
		return remainingTaskCount.get() > 0;
	}
	
	/**
	 * get a new connection
	 * @return
	 */
	public Connection getConnection(){
		return conFactory.getConnection();
	}

	public int getTotalTaskCount() {
		return totalTaskCount; 
	}

	public void onQueryResult(ResultSet rs) {
		// delegate callback
		callback.onQueryResult(rs);
		remainingTaskCount.decrementAndGet();
	}
}
