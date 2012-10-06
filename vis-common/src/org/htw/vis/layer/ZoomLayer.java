package org.htw.vis.layer;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.htw.vis.db.Database;
import org.htw.vis.helper.SchemaHelper;
import org.htw.vis.lucene.LuceneIndex;
import org.htw.vis.lucene.LuceneSearch;

import com.spaceprogram.kittycache.KittyCache;



/**
 * zoom layer
 * 
 * TODO make read only
 * TODO make java
 * 
 * @author timo
 *
 */
public class ZoomLayer {
	private Database db;
	private LuceneIndex index;
	private LuceneSearch search;	
	private Connection con = null;	
	private final int lod;
	
	private final Logger log = Logger.getLogger(this.getClass());
	
	private final KittyCache<Integer,NetworkNode> nodeCache = new KittyCache<Integer, NetworkNode>(10000); // 5000 is max number of objects
	
	/**
	 * create new zoom layer
	 * @param lod
	 */
	public ZoomLayer(int lod){
		this.lod = lod;
		
		// init layer
		init();
	}
	
	public LuceneSearch searcher(){
		// create searcher
		if(search == null){
			search = new LuceneSearch(index);
		}
		return search;
	}
	
	public QuadView getQuadView(){
		return new QuadView(this);
	}
	
	/**
	 * get the lucene index
	 * @return
	 */
	public LuceneIndex getIndex(){
		return index;
	}
	
	/**
	 * add network node
	 * @param node
	 */
	public void addNode(Integer fotoliaId, String words, String thumbPath){
		try {
			// add to database
			PreparedStatement stmt = getDbConnection().prepareStatement("insert into " + getTablename() + " (fotoliaId,words,thumbPath) values(?,?,?)");
			stmt.setInt(1, fotoliaId);
			stmt.setString(2, words);
			stmt.setString(3, thumbPath);
			stmt.execute();
			stmt.close();
			
			// add to index
			getIndex().addDoc(fotoliaId, thumbPath, words);
			
			log.debug("Added Node to layer " + lod + ": " + fotoliaId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("Failed to write node to database:" + fotoliaId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("Failed index node: " + fotoliaId);
		}		
	}
	
	
	/**
	 * clear layer
	 */
	public void clear(){
		// clear index
		getIndex().clear();
		
		// delete table
		try {
			execute("drop table if exists " + getTablename());
			SchemaHelper.initTable(getLOD());
		} catch (SQLException e) {
			e.printStackTrace();
			log.error("Could not clear layer.");
		}
		
	}	
	

	
	/**
	 * init the zoom layer
	 */
	private void init(){
		// get database connection
		try {
			db = Database.get("layers");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.error("Layer init failed.");			
		}
					
		// create index
		index = LuceneIndex.get("layer-" + lod);			
	}
	
	public String toString(){
		return "ZoomLayer " + lod + " / nodeCount=" + nodeCount();
	}
	
	/**
	 * get the database connection
	 * per layer
	 * @return
	 */
	public Connection getDbConnection(){
		if(con == null){
			con = db.getConnection();
		}
		return con;
	}	
	
	/**
	 * shutdown layer
	 * @throws SQLException 
	 */
	public void shutdown() throws SQLException{
		log.debug("Shutdown layer " + lod);
		con.close();
		index.close();
	}
		
	/**
	 * query nodes
	 * @param query
	 * @return
	 * @throws SQLException 
	 */
	public final ResultSet queryNodes(String query) throws SQLException{		
		return executeQuery("select * from " + getTablename() + " where " + query);
	}	
	
	/**
	 * query nodes
	 * @return
	 * @throws SQLException 
	 */
	public final ResultSet queryNodes() throws SQLException{		
		return queryNodes("1");		
	}	
	
	/**
	 * get the internal tablename
	 * @return
	 */
	public String getTablename(){
		return "node_" + lod;
	}
	
	/**
	 * get the current node count
	 * 
	 * @return
	 */
	public Integer nodeCount(){		
		return db.count(getTablename(), "1");		
	}	

	/**
	 * get node by its fotoliaId
	 * @param id
	 * @return
	 */
	public final NetworkNode getNodeById(Integer id) {
		if(id == null)return null;
				
		// try cache
		NetworkNode node = nodeCache.get(id);
		if(node != null){
			return node;
		}
		
		try {
			ResultSet rs = executeQuery("select * from " + getTablename() + " where fotoliaId = " + id);
			if(rs.next()){
				node = new NetworkNode(rs);
				nodeCache.put(id, node, 10000);
				return node;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void setPosition(int id, int x, int y){
		try {
			PreparedStatement updateStmt = getDbConnection().prepareStatement("update " + getTablename() + " set x = ?, y = ? where fotoliaId=?");
			updateStmt.setInt(1, x);
			updateStmt.setInt(2, y);
			updateStmt.setInt(3, id);
			updateStmt.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<NetworkNode> getChildren(int parent){	
		ArrayList<NetworkNode> nodes = new ArrayList<NetworkNode>();
		if(higher() == null)
			return nodes;
		
		ResultSet rs;
		try {
			String q = "select * from " + higher().getTablename() + " where parent = " + parent + " limit 4";			
			rs = executeQuery(q);
			
			while(rs.next()){								
				nodes.add(new NetworkNode(rs));
			}
	
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return nodes;
	}	
	
	public ArrayList<NetworkNode> getFriends(int sibling){
		ArrayList<NetworkNode> friends = new ArrayList<NetworkNode>();
		
		
			NetworkNode parent = lower().getNodeById(sibling);
			if(parent != null){				
				friends.addAll(lower().getChildren(parent.getFotoliaId()));
			}
				
					
		return friends;		
	}	
	
	
	
	public ArrayList<NetworkNode> getFriends(NetworkNode sibling){
		return getFriends(sibling.getFotoliaId());
		
	}
	
	public final ResultSet executeQuery(String query) throws SQLException{		
		return getDbConnection().createStatement().executeQuery(query);
	}
	
	public final boolean execute(String query) throws SQLException{
		return getDbConnection().createStatement().execute(query);
	}
	
	/**
	 * get the level of detail
	 * @return
	 */
	public int getLOD(){
		return lod;
	}
	
	public ZoomLayer lower(){
		return ZoomWorld.get().getLayer(lod+1);
	}
	
	public ZoomLayer higher(){
		return ZoomWorld.get().getLayer(lod-1);
	}
}
