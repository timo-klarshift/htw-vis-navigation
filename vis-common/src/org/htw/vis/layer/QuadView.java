package org.htw.vis.layer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class QuadView {
	private ZoomLayer layer;
	private PreparedStatement rangeStmt; 
	
	public QuadView(ZoomLayer layer){
		this.layer = layer;			
	}
	
	private PreparedStatement getRangeStatement(){
		if(rangeStmt == null){
			try {
				rangeStmt = layer.getDbConnection().prepareStatement("select * from " + layer.getTablename() + " where (x>=? AND x<=? && y>=? && y<=?)");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return rangeStmt;
	}
	
	public void setLayer(ZoomLayer layer){
		if(layer != null){
			this.layer = layer;
			rangeStmt = null;
		}
	}
	
	public int getMax(String c){
		Statement stmt;
		try {
			stmt = layer.getDbConnection().createStatement();
			ResultSet rs = stmt.executeQuery("select max(" + c + ") from " + layer.getTablename());
			if(rs.next()){
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
	public List<NetworkNode> view(int x, int y, int ox, int oy){
		ArrayList<NetworkNode> nodes = new ArrayList<NetworkNode>();	
		
		PreparedStatement s = getRangeStatement();
		
		try {
			s.setInt(1, x);
			s.setInt(2, ox);
			s.setInt(3, y);
			s.setInt(4, oy);
			ResultSet rs = s.executeQuery();
			while(rs.next()){
				nodes.add(NetworkNode.withPosition(rs));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return nodes;		
	}	
}
