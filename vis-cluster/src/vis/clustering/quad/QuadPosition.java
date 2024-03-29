package vis.clustering.quad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import org.apache.log4j.Logger;

import vis.config.VisConfig;
import vis.db.ConnectionFactory;
import vis.db.SqlHelper;
import vis.logging.VisLog;

public class QuadPosition {
	private Connection con;
	private PreparedStatement spStmt;
	private Logger log = Logger.getLogger(this.getClass());
	
	public QuadPosition(){
		this.con = ConnectionFactory.getByName("images");
		
		
	}
	
	public void write(int startLayer){
		log.info("Writing position for layer " + startLayer);
		// get laycount
		SqlHelper h = new SqlHelper(con);
		int imageCount = h.max("images_"+startLayer);
		
		int n = (int) Math.sqrt(imageCount);
		int x = 0;
		int y = 0;
		int c = 0;
		
		// get a set of all ids		
		Statement stmt;
		try {
			stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("select fotoliaId from images_" + startLayer);
			while(rs.next()){
				x = c % n;
				y = c / n;
							
				setPosition(startLayer, rs.getInt(1), x, y);
										
				c++;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.info("Writing position done.");
				
	}
	
	private void setPosition(int layer, int fotoliaId, int x, int y){
		System.out.println(layer + " / " + fotoliaId + " / " + x + " / " + y);
		
		PreparedStatement stmt;
		try {
			// update position
			stmt = con.prepareStatement("update images_" + layer + " set x=?, y=?  where fotoliaId=?");
			stmt.setInt(1, x);
			stmt.setInt(2, y);
			stmt.setInt(3, fotoliaId);
			stmt.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// get child nodes in lower layer
		int[] children = new int[4];
		int k=0;
		if(layer > 0){
			try {
				ResultSet rs = con.createStatement().executeQuery("select fotoliaId from images_" + (layer-1) + " where parent=" + fotoliaId);			
				while(rs.next()){
					children[k++] = rs.getInt(1);
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// recursive call
		if(k > 0){
			int cx, cy;
			k=0;
			for(int j=0; j<2; j++){
				for(int i=0; i<2; i++){
					cx = x*2+i;
					cy = y*2+j;
					setPosition(layer-1, children[k++], cx, cy);
				}
			}
		}
		
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		VisConfig.load();
		VisLog.initLogging();
		// TODO Auto-generated method stub
		new QuadPosition().write(6);
	}

}
