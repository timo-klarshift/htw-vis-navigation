package org.htw.vis.server.protocol;

import java.io.Serializable;
import java.util.Date;

/**
 * VIS packet
 * @author timo
 *
 */
public class VISPacket implements Serializable, Comparable<VISPacket> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8485216566881760437L;
	
	private String sessionId = null;
	private String command;
	private Serializable data;
	private Double priority = new Double(0);
	private Long timestamp = new Date().getTime();
	
	/**
	 * create a packet
	 * @param sessionId
	 * @param command
	 * @param data
	 */
	public VISPacket(String sessionId, String command, Serializable data){
		setSessionId(sessionId);
		setData(data);
		setCommand(command);
	}
	
	public void setPriority(double p){
		priority = p;
	}
	
	/**
	 * get the session id
	 * @return
	 */
	public String getSessionId(){
		return sessionId;
	}	
	
	/**
	 * set the data
	 * @param data
	 */
	public void setData(Serializable data){
		this.data = data;
	}
	
	/**
	 * set the session id
	 * @param sessionId
	 */
	public void setSessionId(String sessionId){
		this.sessionId = sessionId;
	}
		
	/**
	 * get the actual data
	 * @return
	 */
	public Serializable getData() {
		return data;
	}	
	
	/**
	 * set the command
	 * @param command
	 */
	public void setCommand(String command){
		this.command = command;
	}
	
	/**
	 * get the packets command
	 * @return
	 */
	public String getCommand(){
		return command;
	}
	
	public String toString(){
		return "PCK + " + timestamp + " / sid=" + sessionId + "] data=" + data;
	}
	
	public Long getTimestamp(){
		return timestamp;
	}

	@Override
	public int compareTo(VISPacket other) {
		if(priority.equals(other.getPriority())){
			// by timestamp
			return timestamp.compareTo(other.getTimestamp());
		}
		
		return priority.compareTo(other.getPriority());
	}
	
	public Double getPriority(){
		return priority;
	}
}
