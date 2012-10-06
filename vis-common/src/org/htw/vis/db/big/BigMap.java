package org.htw.vis.db.big;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.htw.vis.config.DbConfig;
import org.htw.vis.db.Database;
import org.htw.vis.helper.SchemaHelper;

import com.spaceprogram.kittycache.KittyCache;

/**
 * big map implementation
 * 
 * @author timo
 * 
 * @param <T>
 */
public class BigMap<T> implements Map<Integer, T> {
	private final String name;

	/* types */
	private static final int CACHE_SIZE = 5000;

	private final Logger log = Logger.getLogger(this.getClass());

	/* database */
	private Database db;
	private Connection con;

	/* cache */
	private final KittyCache<Integer, T> cache = new KittyCache<Integer, T>(
			CACHE_SIZE);

	/* statements */
	PreparedStatement putStmt, getStmt, keyLookupStmt, deleteStmt, iterateStmt;
	private boolean caching = true;

	/**
	 * create big map
	 * 
	 * @param name
	 * @param type
	 */
	public BigMap(String name) {		
		this.name = name;

		// import schema
		init();

		try {
			// disable auto commit
			con.setAutoCommit(false);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void printInfo() throws SQLException{
		System.out.println("Key Cache:\t" + (caching ? ("on" + "(" + CACHE_SIZE + ")") : "off"));
		System.out.println("Auto-Commit:\t" + (con.getAutoCommit() ? "on" : "off"));
	}

	/**
	 * set caching
	 * 
	 * @param c
	 */
	public void setCaching(boolean c) {
		caching = c;
	}

	/**
	 * init
	 */
	private void init() {
		// get database
		DbConfig config = new DbConfig("BigMap-" + name, null, "", "",
				DbConfig.TYPE_SQLITE);
		db = Database.register("BigMap-" + name, config);

		// import schema
		con = db.getConnection();

		// read sql and replace
		String bigMapSchema = SchemaHelper.readSchema("big-map");
		bigMapSchema = bigMapSchema.replace("[NAME]", name);

		// set generic type
		// BLOB
		bigMapSchema = bigMapSchema.replace("[GENERIC]", "BLOB NOT NULL");		

		try {
			con.createStatement().execute(bigMapSchema);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// create statements
		try {
			putStmt = con.prepareStatement("replace into " + name
					+ " (id, val) VALUES (?, ?)");
			getStmt = con.prepareStatement("select val from " + name
					+ " where id = ? limit 1");
			keyLookupStmt = con.prepareStatement("select id from " + name
					+ " where id = ? limit 1");
			deleteStmt = con.prepareStatement("delete from " + name
					+ " where id = ? ");
			
			iterateStmt = con.prepareStatement("select id,val from " + name);
			iterateStmt.setFetchSize(1000);
			System.out.println(iterateStmt.getFetchSize());
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		log.debug("Init BigMap: " + name);
	}

	/**
	 * shutdown the map
	 */
	public void shutdown() {
		// clean up
		try {
			con.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * execute sql command
	 * 
	 * @param sql
	 */
	private void execute(String sql) {
		try {
			con.createStatement().execute(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void clear() {
		// delete all
		execute("delete from " + name);
		commit();

		// clear cache
		cache.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		try {
			keyLookupStmt.setInt(1, (Integer) key);
			ResultSet rs = keyLookupStmt.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		System.out.println("OOOOOOOOOOOOOOOOOOOOOOO");
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public T get(Object key) {
		if (key instanceof Integer == false) {
			return null;
		}

		// check cache
		if (caching) {
			T cached = cache.get((Integer) key);
			if (cached != null) {
				return cached;
			}
		}

		try {
			getStmt.setInt(1, (Integer) key);

			ResultSet rs = getStmt.executeQuery();
			if (rs.next()) {
				T object = (T) readValue(rs, 1); 				

				if (caching == true)
					cache.put((Integer) key, object, 1000000);
				return object;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public void iterate(BigMapIteratorCallback<T> callback){
		ResultSet rs;
		try {
			rs = iterateStmt.executeQuery();
			while(rs.next()) {
				callback.onKeyValue(rs.getInt(1), readValue(rs, 2));				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Set<Integer> keySet() {
		log.error("You should use iterate() instead of keySet()");
		final HashSet<Integer> set = new HashSet<Integer>();
		iterate(new BigMapIteratorCallback<T>() {

			@Override
			public void onKeyValue(Integer key, T value) {
				set.add(key);
			}
			
		});
		
		return set;
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends T> m) {
		for (Integer k : m.keySet()) {
			put(k, m.get(k));
		}
	}

	@Override
	public T remove(Object key) {
		T val = get(key);
		if (val == null) {
			return null;
		}

		try {
			deleteStmt.setInt(1, (Integer) key);
			deleteStmt.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (caching && cache.get((Integer) key) != null) {
			cache.remove((Integer) key);
		}

		return val;
	}

	@Override
	public Collection<T> values() {
		try {
			throw new Exception("Not implemented.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private byte[] getBytesForObject(T value) {

		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(value);
			oos.flush();
			oos.close();
			bos.close();
			return bos.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private T readValue(ResultSet rs, int pos) {

		ByteArrayInputStream bais;
		ObjectInputStream ins;

		try {

			
			bais = new ByteArrayInputStream(rs.getBytes(pos));

			ins = new ObjectInputStream(bais);

			T mc = (T) ins.readObject();
			

			ins.close();

			return mc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;

	}

	@Override
	public T put(Integer key, T value) {		
		try {
			putStmt.setInt(1, key);					

			putStmt.setBytes(2, getBytesForObject(value));			

			putStmt.execute();

			if (caching && cache.get(key) != null) {
				cache.remove(key);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	

	public void commit() {
		try {
			if (con.getAutoCommit() == true) {
				return;
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		log.debug("Commit BigMap ...");
		try {
			con.commit();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug("OK");
	}

	@Override
	public int size() {
		ResultSet rs;
		try {
			rs = con.createStatement().executeQuery(
					"select count(id) from " + name);
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return -1;
	}

	@Override
	public Set<java.util.Map.Entry<Integer, T>> entrySet() {
		try {
			throw new Exception("Not implemented.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
