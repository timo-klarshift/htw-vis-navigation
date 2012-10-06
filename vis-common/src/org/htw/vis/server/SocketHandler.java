package org.htw.vis.server;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.PriorityBlockingQueue;

import org.htw.vis.server.protocol.VISPacket;

/**
 * client server socket handler
 * @author timo
 *
 */
public class SocketHandler implements Runnable {
	private Socket socket;
	private ObjectInputStream objectInputStream;
	private ObjectOutputStream objectOutputStream;
	private InputStream inputStream;
	private OutputStream outputStream;
	private HandlerCallback callback;
	private SocketHandler handler;
	private boolean handling = false;
		
	private final PriorityBlockingQueue<VISPacket> sendQueue = new PriorityBlockingQueue<VISPacket>();
	
	PacketReader packetReader = new PacketReader();
	PacketWriter packetWriter = new PacketWriter();
	private int delay = 0;
	
	private class PacketReader implements Runnable {

		@Override
		public void run() {
			while(handling){
				readNextPacket();
			}
		}
		
	}
	
	private class PacketWriter implements Runnable{
		@Override
		public void run() {
			while(handling){
				writeAll();
			}
		}		
	}
	
	private void readNextPacket(){
		try{
			// read packet (maybe we can read more than one in one cycle?)			
			VISPacket packet = null;
			packet = readPacket();
													
			if (packet != null) {
				//System.out.println("READ " + (new Date().getTime()-packet.getTimestamp()));
				callback.onPacket(handler, packet);
				Thread.sleep(delay);
			}else{
				Thread.sleep(10);						
			}	
		}catch(EOFException e){
			shutdown();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void writeAll(){
		try {			
			if(sendQueue.size() > 0){
				while(sendQueue.size() > 0){							
					VISPacket packet = sendQueue.poll();
					//System.out.println("WRITE " + packet.getTimestamp());
					objectOutputStream.writeObject(packet);
					objectOutputStream.reset();
					Thread.sleep(delay);
				}
			}else{
				Thread.sleep(10);
			}
			
			//objectOutputStream.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	/**
	 * create callback handler
	 * @param socket
	 * @param callback
	 */
	public SocketHandler(Socket socket, HandlerCallback callback) {
		this.socket = socket;
		this.callback = callback;		
		this.handler = this;

		try {
			// get streams
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();	
			
			// create output stream and initially flush
			// because it would block 
			objectOutputStream = new ObjectOutputStream(outputStream);
			objectOutputStream.flush();
									
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	/**
	 * shutdown the socket handler
	 */
	void shutdown() {
		handling = false;
		try {
			if (objectOutputStream != null) {
				objectInputStream.close();
			}
			if (objectInputStream != null) {
				objectInputStream.close();
			}
			if (inputStream != null) {
				inputStream.close();
			}
			if (outputStream != null) {
				outputStream.close();
			}
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		System.out.println("SHUT DOWN");
	}

	/**
	 * write a packet
	 * @param packet
	 */
	public void writePacket(final VISPacket packet) {	
		sendQueue.offer(packet);
	}
	
	

	/**
	 * read a packet
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private VISPacket readPacket() throws IOException, ClassNotFoundException {
		if(objectInputStream == null){
			objectInputStream = new ObjectInputStream(inputStream);
		}
		
		Object obj = objectInputStream.readObject();
		if (obj != null) {
			return (VISPacket) obj;
		}
			
		
		return null;
	}

	/**
	 * handler call bacl
	 * @author timo
	 *
	 */
	public interface HandlerCallback {
		public void onPacket(SocketHandler socketHandler, VISPacket packet);
	}
	
	/**
	 * start the handler
	 */
	public void start(){
		// start thread
		
	
		if(true){
			handling = true;
			new Thread(packetReader).start();
			new Thread(packetWriter).start();
		}else{
			new Thread(this).start();
		}
	
	}

	public void setDelay(int i) {
		this.delay  = i;
	}

	@Override
	public void run() {
		handling = true;
		while(handling){			
			readNextPacket();	
			writeAll();
		}
	}

}
