package org.htw.vis.layer.creation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.htw.vis.helper.FeatureAccess;
import org.htw.vis.layer.ZoomLayer;

/**
 * abstract position writer
 * @author timo
 *
 */
public abstract class PositionWriter {
	protected final FeatureAccess features = new FeatureAccess();
	private PreparedStatement updateStmt;
	protected final Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * set a nodes raw position
	 * @param layer
	 * @param id
	 * @param x
	 * @param y
	 */
	public void setPosition(ZoomLayer layer, int id, int x, int y){
		try {
			updateStmt = layer.getDbConnection().prepareStatement("update " + layer.getTablename() + " set x = ?, y = ? where fotoliaId=?");
			updateStmt.setInt(1, x);
			updateStmt.setInt(2, y);
			updateStmt.setInt(3, id);
			updateStmt.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param layer
	 * @param parent
	 * @return
	 */
	protected ArrayList<Integer> getChildren(ZoomLayer layer, int parent){
		ResultSet rs;
		try {
			String q = "select fotoliaId from " + layer.getTablename() + " where parent = " + parent + " limit 4";			
			rs = layer.executeQuery(q);
			ArrayList<Integer> nodes = new ArrayList<Integer>();
			while(rs.next()){								
				nodes.add(rs.getInt(1));
			}
			return nodes;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	
}
