package org.htw.vis.layer.creation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.htw.vis.layer.NetworkNode;
import org.htw.vis.layer.ZoomLayer;
import org.htw.vis.layer.ZoomWorld;

/**
 * simple position writer
 * @author timo
 *
 */
public class SimplePositionWriter  {
	private static Logger log = Logger.getLogger("SimplePositionWriter");
	/**
	 * create a simple position writer
	 * @param layer
	 */
	public SimplePositionWriter(){
			
	}
	
	/**
	 * write positions
	 */
	public void writePositions(){
		ZoomWorld world = ZoomWorld.create(4);
		ZoomLayer layer = world.getLastLayer();
		
		ArrayList<NetworkNode> nodes = new ArrayList<NetworkNode>();
		ResultSet rs;
		try {
			rs = layer.queryNodes();
			while(rs.next()){
				NetworkNode n = new NetworkNode(rs);
				if(n.getParentId() != -1)
					nodes.add(n);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		
		
		// set first quad positions
		for(int y=0; y<2; y++){
			for(int x=0; x<2; x++){
				NetworkNode n = nodes.get(2*x+y);				
				set(layer, n.getFotoliaId(), x, y);										
			}
		}
				
		System.out.println("Initial positions written.");
	}
	
	/**
	 * delegating position setter
	 * @param layer
	 * @param id
	 * @param x
	 * @param y
	 */
	public static void set(ZoomLayer layer, int id, int x, int y){		
		layer.setPosition(id, x, y);
					
		// set position in previous layers
		ZoomLayer higher = layer.higher();
		
		if(higher != null && higher.getLOD() > 0){
			// get children (which are in higher) from current layer
			ArrayList<NetworkNode> children = layer.getChildren(id);			
			
			
			for(int ny=0; ny<2; ny++){
				for(int nx=0; nx<2; nx++){
					try{
						NetworkNode n = children.get(2*nx+ny);
						if(n != null)
							set(higher, n.getFotoliaId(), 2*x+nx, 2*y+ny);
					}catch(Exception e){
						log.error(e.getMessage());
					}
				}
			}			
		}
	}
	
	public static void main(String[] args) {
		// setup logger
		Logger.getRootLogger().setLevel(Level.INFO);
				
		try {
			new SimplePositionWriter().writePositions();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
	}
}
