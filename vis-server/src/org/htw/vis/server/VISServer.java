package org.htw.vis.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;

public class VISServer implements SocketHandler.HandlerCallback {
	static final int PORT = 9900;

	VISServer server;
	HashMap<String,SocketHandler> sessions = new HashMap<String,SocketHandler>();

	public VISServer() {
		this.server = this;
	}


	public void start() {
		try {
			// create server
			ServerSocket serverSocket = new ServerSocket(PORT);

			// accept only one connection
			// TODO: should be in thread
			Socket clientSocket = serverSocket.accept();
			System.out.println("Client connected.");

			// handle client
			SocketHandler handler = new SocketHandler(clientSocket, this);
			new Thread(handler).start();						
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new VISServer().start();
	}

	@Override
	public void onPacket(SocketHandler socketHandler, VISPacket packet) {
		String cmd = packet.getCommand();
		
		// handle packets here
		System.out.println("RECV: " + cmd + " :: " + packet);
		
		// start a session
		if(cmd.equals(VISProto.START_SESSION)){
			startSession(socketHandler, packet);				
		}
		
		
	}
	
	/**
	 * start a new user session
	 * @param handler
	 * @param packet
	 */
	private void startSession(SocketHandler handler, VISPacket packet){
		// create session id
		String sessionId = UUID.randomUUID().toString();
		
		// keep handler
		sessions.put(sessionId, handler);
		
		// stats
		System.out.println("Having " + sessions.size() + " active sessions ...");
		
		// send session id to client
		VISPacket response = new VISPacket(sessionId, VISProto.START_SESSION, null);
		handler.writePacket(response);
	}
	
	

}
