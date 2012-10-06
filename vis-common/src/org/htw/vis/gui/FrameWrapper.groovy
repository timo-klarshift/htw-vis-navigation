package org.htw.vis.gui

import javax.swing.JFrame;
import javax.swing.JPanel;

class FrameWrapper extends JFrame {
	private JPanel panel
	
	public FrameWrapper(String title, JPanel panel){
		setTitle(title)
		this.panel = panel
		
		panel.setVisible(true)
		add(panel)
		pack()
		setVisible(true)
	}
}
