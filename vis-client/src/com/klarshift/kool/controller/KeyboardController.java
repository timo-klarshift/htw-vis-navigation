package com.klarshift.kool.controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

/**
 * keyboard controller
 * https://wiki.ubuntu.com/X/MPX
 * @author timo
 *
 */
public class KeyboardController implements KeyListener{
	private HashMap<Integer,Boolean> keyMap = new HashMap<Integer,Boolean>();	

	@Override
	public void keyPressed(KeyEvent e) {
		keyMap.put(e.getKeyCode(), true);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keyMap.put(e.getKeyCode(), false);
	}

	@Override
	public void keyTyped(KeyEvent e) {		
	}
	
	public boolean isKeyPressed(int keyCode) {
		if (keyMap.containsKey(keyCode) == false) {
			return false;
		}
		return keyMap.get(keyCode);
	}
}
