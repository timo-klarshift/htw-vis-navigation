package vis.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import vis.config.VisConfig;
import vis.logging.VisLog;

public class FeatureDump {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// init
		VisConfig.load();
		VisLog.initLogging();
		
		
		
		
		
		// fetch loop
		
		try {
			Connection con1 = ConnectionFactory.getByName("features");
			
			Connection con2 = ConnectionFactory.getByName("images");
			SqlHelper helper = new SqlHelper(con2);
			helper.execute("delete from features");
			con2.setAutoCommit(false);
			
			
			
			// insert statement
			PreparedStatement insert = con2.prepareStatement("insert into features (fotoliaId, features) VALUES (?,?)");
			
			// loop features
			int bulk = 50000;
			int max = 17000000;
			double p = 0;
			int r=0;
			for(int i=0; i<max; i+=bulk){
				Statement stmt = con1.createStatement();
				ResultSet rs = stmt.executeQuery("select fotolia_id, feature_vectors from `3dvis`.image limit " + i + "," + bulk);
				
				while(rs.next()){					
					insert.setInt(1, rs.getInt(1));
					insert.setBytes(2, rs.getBytes(2));
					insert.addBatch();
				}
				
				r++;
				if(r > 4){
					System.out.println("batching ...");
					insert.executeBatch();
					r=0;
					con2.commit();
				}
				
				//
				
				
				
				p = (double)i / max;				
				System.out.println((Math.round(100*p) ) + " %");
				
				stmt.close();
				rs.close();
				//;
			}
			
			
						
			con2.close();
			con1.close();		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}

}
