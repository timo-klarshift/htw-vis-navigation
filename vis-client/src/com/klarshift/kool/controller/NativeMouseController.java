package com.klarshift.kool.controller;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * native mouse controller
 * @author timo
 *
 */
public class NativeMouseController {
	private int mouseId;
	private File mouseFile;
	private InputStream mouseStream;
	private byte deltaX, deltaY;
	private boolean enabled = false;
	Rectangle bounds;
	int width, height;
	double mouseX, mouseY;
	private long packetsRead = 0;
	
	/* listeners */
	private LinkedList<NativeMouseListener> listeners = new LinkedList<NativeMouseListener>();
	
	public NativeMouseController(int id, int width, int height){
		this.mouseId = id;
		this.width = width;
		this.height = height;
		
		setCursor(width/2, height/2);
	}
	
	public void setCursor(double x, double y){
		if(x > 0 && x<= width)
			mouseX = x;
		if(y > 0 && y<= height)
			mouseY = y;
	}
	
	public double getX(){return mouseX;}
	public double getY(){return mouseY;}
	
	public void addListener(NativeMouseListener l){
		listeners.add(l);
	}
	
	private void update(){
		// movement (delta > 0)
		if(deltaX != 0 || deltaY != 0){
			// update position
			double r = 0.1;
			setCursor(mouseX + deltaX * r, mouseY - deltaY * r);			
						
			// inform listeners
			for(NativeMouseListener l : listeners){
				l.onMouseMoved(this);
			}
		}
	}
	
	public int getDeltaX(){return (int)deltaX;}
	public int getDeltaY(){return (int)deltaY;}
	
	public void enable(){
		new Thread(){
			public void run() {
				mouseFile = new File("/dev/input/mouse" + mouseId);
				try{
					mouseStream = new FileInputStream(mouseFile);
					
					int d;
					byte[] packet = new byte[3];
					enabled = true;
					while(enabled && (d = mouseStream.read(packet, 0, 3)) != -1){
						// get delta
						deltaX = packet[1];
						deltaY = packet[2];
						update();
						packetsRead += Math.abs(deltaX) + Math.abs(deltaY);
					}					
				}catch(Exception e){
					e.printStackTrace();
				}finally{
					if(mouseStream != null){
						try {
							mouseStream.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				
			};
		}.start();	
	}	
	
	public void disable(){
		enabled = false;
	}
	
	public String toString(){
		return "Native Mouse " + mouseId + "] + [" + mouseX + "/" + mouseY + "] : " + packetsRead;
	}
	
	public interface NativeMouseListener {
		public void onMouseMoved(NativeMouseController mouse);
	}
}
