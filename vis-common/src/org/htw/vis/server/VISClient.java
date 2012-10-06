package org.htw.vis.server;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.htw.vis.server.context.ContainerImage;
import org.htw.vis.server.protocol.VISPacket;
import org.htw.vis.server.protocol.VISProto;
import org.htw.vis.server.protocol.VISServer;
import org.htw.vis.server.protocol.VISUpdate;


/**
 * VISClient 
 * @author timo
 *
 */
public class VISClient implements SocketHandler.HandlerCallback{
	/* listeners */
	private LinkedList<ClientListener> listeners = new LinkedList<ClientListener>();
	
	/* session data */
	private String sessionId;
	private SocketHandler handler;
	private String serverHost;
	
	/* logging */
	private final Logger log = Logger.getLogger("VISClient");

	/* states */
	
	
	private boolean sessionStarted = false;
	
	/**
	 * create new vis client on local host
	 */
	public VISClient(){
		serverHost = "localhost";
	}
	
	/**
	 * add listener to client
	 * @param listener
	 */
	public void addListener(ClientListener listener){
		listeners.add(listener);
	}
	
	/**
	 * set the remote host
	 * @param serverHost
	 */
	public void setRemoteHost(String serverHost){
		this.serverHost = serverHost;
	}
	
	/**
	 * create vis client with given host
	 * @param host
	 */
	public VISClient(String host){
		setRemoteHost(host);
	}
	
	/**
	 * get the clients session id
	 * @return
	 */
	public String getSessionId(){
		return sessionId;
	}
	
	/**
	 * shutdown client
	 */
	public void shutdown(){
		if(handler != null){
			handler.shutdown();
		}
	}	
	
	/**
	 * start session
	 * @param width
	 * @param height
	 */
	private void startSession(int width, int height){
		int[] data = new int[2];
		data[0] = width; data[1] = height;
		VISPacket packet = new VISPacket(null, VISProto.START_SESSION, data);
		handler.writePacket(packet);
	}
	
	/**
	 * start the vis client with a given size
	 * @param width
	 * @param height
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public void start(int width, int height) throws UnknownHostException, IOException{		
        Socket socket = new Socket(serverHost, VISServer.PORT);
        handler = new SocketHandler(socket, this);              
        handler.start();
        
        // start a session
        startSession(width, height);         
	}
	
	
	
	public boolean isSessionStarted(){
		return sessionStarted;
	}
	
	
	
	
	

	public void write(String command, Serializable data){
		VISPacket packet = new VISPacket(sessionId, command, data);
		handler.writePacket(packet);
	}
	
	public void write(String command){
		write(command, null);
	}
	
	
	

	@Override
	public void onPacket(final SocketHandler socketHandler, final VISPacket packet) {
		String cmd = packet.getCommand();
		
		
		if(cmd.equals(VISProto.START_SESSION)){
			// store session id
			this.sessionId = packet.getSessionId();					
			onSessionStarted();
		}else if(cmd.equals(VISProto.UPDATE)){
			VISUpdate update = (VISUpdate) packet.getData();			
			onUpdateImages(update);
		}else if(cmd.equals(VISProto.PRELOAD)){
			ContainerImage image = (ContainerImage) packet.getData();			
			onPreloadImage(image);
		}else{		
			log.debug("CLIENT RECV: " + cmd + " :: " + packet);
		}
	}
	
	private void onSessionStarted(){
		log.info("Session started: " + getSessionId());
		sessionStarted = true;
		for(ClientListener l : listeners){
			l.onSessionStarted();
		}
	}
	
	private void onUpdateImages(VISUpdate image){
		for(ClientListener l : listeners){
			l.onImagesUpdate(image);
			
		}
	}
	
	private void onPreloadImage(ContainerImage image){
		for(ClientListener l : listeners){
			l.onImagePreload(image);
		}
	}	
}
