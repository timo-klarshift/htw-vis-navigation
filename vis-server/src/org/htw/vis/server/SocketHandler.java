package org.htw.vis.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.htw.vis.server.SocketHandler.HandlerCallback;

public class SocketHandler implements Runnable{
	private Socket socket;
	private ObjectInputStream objectInputStream;
	private ObjectOutputStream objectOutputStream;
	InputStream inputStream;
	OutputStream outputStream;
	private HandlerCallback callback;
	private SocketHandler handler;
	private boolean handling = false;

	public SocketHandler(Socket socket, HandlerCallback callback) {
		this.socket = socket;
		this.callback = callback;		
		this.handler = this;

		// get streams
		try {
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			shutdown();
		}
		
		System.out.println("INI");

	}

	void shutdown() {

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

		handling = false;
		System.out.println("SHUT DOWN");
	}

	public void writePacket(VISPacket packet) {
		try {
			objectOutputStream = new ObjectOutputStream(outputStream);
			objectOutputStream.writeObject(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public VISPacket readPacket() {
		try {
			if(inputStream != null)
				objectInputStream = new ObjectInputStream(inputStream);
			
			if (objectInputStream != null) {
				Object obj = objectInputStream.readObject();
				if (obj != null) {
					return (VISPacket) obj;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public interface HandlerCallback {
		public void onPacket(SocketHandler socketHandler, VISPacket packet);
	}

	@Override
	public void run() {
		handling = true;
		try {
			while (handling) {
				try {
					// validate
					if (socket.isConnected()
							&& socket.isClosed() == false) {

						VISPacket packet = readPacket();
						if (packet != null) {
							callback.onPacket(handler, packet);
						}
						Thread.sleep(1000);
					} else {
						shutdown();
						break;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			shutdown();
		}
	}
}
