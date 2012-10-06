package org.htw.vis.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.htw.vis.helper.ImageAccess;
import org.htw.vis.layer.NetworkNode;
import org.htw.vis.layer.QuadView;
import org.htw.vis.layer.ZoomLayer;
import org.htw.vis.layer.ZoomWorld;

public class QuadPanel extends JPanel implements KeyListener{
	private static int size = 64;
	private ZoomWorld world = ZoomWorld.create(7);
	private QuadView view;
	private ImageAccess access = new ImageAccess();
	private int offsetX, offsetY;
	private int cellsX, cellsY;
	private ZoomLayer currentLayer;
	
	private int maxX, maxY;
	
	private LinkedHashMap<Integer, NetworkNode> images = new LinkedHashMap<Integer, NetworkNode>();
		
	public QuadPanel(){
		setPreferredSize(new Dimension(1000, 600));
		setSize(1000, 600);
		
		view = new QuadView(world.getFirstLayer());
		
		// get layer
		setLayer(world.getFirstLayer());

		
		setFocusable(true);
		addKeyListener(this);
		updateSize();
	}
	
	public void setLayer(ZoomLayer layer){		
		if(layer != null){
			currentLayer = layer;
		}
		
		
		
		view.setLayer(currentLayer);
		maxX = view.getMax("x");
		maxY = view.getMax("y");
		
		
		System.out.println("Set layer " + layer);
	}
	
	public void zoomOut(){
		setLayer(currentLayer.lower());
		offsetX *= 2;
		offsetY *= 2;
			
		updateSize();
		updateView();
	}
	
	public void zoomIn(){
		setLayer(currentLayer.higher());
		offsetX /= 2;
		offsetY /= 2;
		updateView();
	}
	
	
	private void updateSize(){
		int a = size;
		cellsX = Math.round(getWidth() / a);
		cellsY = Math.round(getHeight() / a);
	}
	
	private void addImage(NetworkNode n){
		if(images.containsKey(n.getFotoliaId())){
			return;
		}
		
		images.put(n.getFotoliaId(), n);		
		access.preload(n);
	}
	
	public void setOffset(int x, int y){
		offsetX = x;
		offsetY = y;
		updateView();
	}
	
	void updateView(){
		int ox = offsetX + cellsX;
		int oy = offsetY + cellsY;
		
		// get images
		List<NetworkNode> list = view.view(offsetX, offsetY, ox, oy);
		
		// add images
		synchronized (images) {
			images.clear();
			for(NetworkNode n : list){
				addImage(n);
			}
		}
										
		// repaint
		repaint();
	}	
	
	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		int r = 0;
		synchronized (images) {
			BufferedImage bi;
			Image bis;
			int x, y;
			NetworkNode n;
			
			int fx = getWidth()/2 - (cellsX * size)/2;
			int fy = getHeight()/2 - (cellsY * size)/2;
			
			for(Entry<Integer, NetworkNode> k : images.entrySet()){
				n = k.getValue();
				bi = access.getImage(n);
				bis = bi.getScaledInstance(size, size, BufferedImage.SCALE_FAST);
				x = n.getX();
				y = n.getY();
				
				
				
				
				
				g.drawImage(bis, (x-offsetX)*size, (y-offsetY)*size, null);
				r++;
			}
			
			System.out.println("RENDERED " + r +  "/" + fx);
		}		
		
		
	}
	
	public static void main(String[] args){
		QuadPanel panel = new QuadPanel();
		FrameWrapper w = new FrameWrapper("Quad View", panel);
		
		
		w.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel.updateView();
		System.out.println("**");
		panel.setOffset(5,3);
		System.out.println("**");
		panel.setOffset(0,0);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		
		if(e.getKeyCode() == KeyEvent.VK_LEFT){
			if(offsetX > 0)
				offsetX --;
		}else if(e.getKeyCode() == KeyEvent.VK_RIGHT){
			if(offsetX < 1000)
				offsetX ++;
		}else if(e.getKeyCode() == KeyEvent.VK_UP){
			if(offsetY > 0)
				offsetY --;
		}else if(e.getKeyCode() == KeyEvent.VK_DOWN){
			if(offsetY < 1000)
				offsetY ++;
		}
		
		if(e.getKeyCode() == KeyEvent.VK_PLUS){
			zoomIn();
		}else if(e.getKeyCode() == KeyEvent.VK_MINUS){
			zoomOut();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		//System.out.println(e);
		updateView();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		//System.out.println(e);
		
	}
	
	
}
