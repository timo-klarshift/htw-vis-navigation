package org.htw.vis.server;

import java.net.Socket;


/**
 * VISClient 
 * @author timo
 *
 */
public class VISClient implements SocketHandler.HandlerCallback{
	private String sessionId;
	SocketHandler handler;
	
	public VISClient(){
		
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
	 */
	public void start(int width, int height){
		try {

            Socket socket = new Socket("localhost", VISServer.PORT);
            handler = new SocketHandler(socket, this);              
            new Thread(handler).start();
            
            // start a session
            startSession(width, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	

	@Override
	public void onPacket(SocketHandler socketHandler, VISPacket packet) {
		String cmd = packet.getCommand();
		System.out.println("CLIENT RECV: " + cmd + " :: " + packet);
		
		if(cmd.equals(VISProto.START_SESSION)){
			this.sessionId = packet.getSessionId();
			System.out.println("Session started " + packet);
		}		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new VISClient().start(30, 20);		
	}

}
