package org.htw.vis.server;

import java.io.Serializable;

/**
 * VIS packet
 * @author timo
 *
 */
public class VISPacket implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8485216566881760437L;
	
	String sessionId = null;
	String command;
	Serializable data;
	
	
	public VISPacket(String sessionId, String command, Serializable data){
		setSessionId(sessionId);
		setData(data);
		setCommand(command);
	}
	
	public String getSessionId(){
		return sessionId;
	}
	
	public void setBytes(byte[] data){
		this.data = data;
	}
	
	public void setData(Serializable data){
		this.data = data;
	}
	
	public void setSessionId(String sessionId){
		this.sessionId = sessionId;
	}
		
	public Serializable getData() {
		return data;
	}
	
	public void setCommand(String command){
		this.command = command;
	}
	
	public String getCommand(){
		return command;
	}
	
	public String toString(){
		return "sid=" + sessionId + "] " + data;
	}
}
