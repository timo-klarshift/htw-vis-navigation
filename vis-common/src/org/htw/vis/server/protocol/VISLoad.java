package org.htw.vis.server.protocol;

import java.io.Serializable;

/**
 * load packet
 * 
 * @author timo
 * 
 */
public class VISLoad implements Serializable {
	public int focusX = -1;
	public int focusY = -1;
	
	public int shiftX = 0;
	public int shiftY = 0;
	public int shiftZ = 0;

	public String searchQuery = null;
	public int maxResults = 100;
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("LOAD");
		sb.append("\r\n\tshiftX  :" + shiftX);
		sb.append("\r\n\tshiftY  :" + shiftY);
		sb.append("\r\n\tshiftZ  :" + shiftZ);
		sb.append("\r\n\tfocusX  :" + focusX);
		sb.append("\r\n\tfocusY  :" + focusY);
		sb.append("\r\n\tmax     :" + maxResults);
		if(searchQuery != null){
			sb.append("\r\n\tQuery    : " + searchQuery);
		}
		return sb.toString();
	}
}
