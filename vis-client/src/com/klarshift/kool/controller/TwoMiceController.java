package com.klarshift.kool.controller;

import java.util.LinkedList;

import com.klarshift.kool.controller.NativeMouseController.NativeMouseListener;

/**
 * two mice controller
 * @author timo
 *
 */
public class TwoMiceController implements NativeMouseListener {
	private NativeMouseController mouse1, mouse2;
	
	/* listeners */
	private LinkedList<NativeMouseListener> listeners = new LinkedList<NativeMouseListener>();
	
	public TwoMiceController(int width, int height){
		mouse1 = new NativeMouseController(0, width, height);
		mouse1.addListener(this);
		mouse2 = new NativeMouseController(2, width, height);
		mouse2.addListener(this);
	}
	
	public void addListener(NativeMouseListener l){
		listeners.add(l);
	}
	
	public void enable(){
		mouse1.enable();
		mouse2.enable();
	}
	
	public NativeMouseController getMouse1(){
		return mouse1;
	}
	
	public NativeMouseController getMouse2(){
		return mouse2;
	}
	
	public void disable(){
		mouse1.disable();
		mouse2.disable();
	}

	@Override
	public void onMouseMoved(NativeMouseController mouse) {
		// inform listeners
		for(NativeMouseListener l : listeners){
			l.onMouseMoved(mouse);
		}
	}
	
	public static void main(String[] args){
		TwoMiceController c = new TwoMiceController(800, 600);
		c.enable();
	}		
}
