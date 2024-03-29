package vis.clustering.quad;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import vis.config.VisConfig;
import vis.db.ConnectionFactory;
import vis.image.Image;
import vis.image.ImageLoader;
import vis.image.ImageLoader.CacheObject;
import vis.image.ImageLoader.LoadCallback;
import vis.logging.VisLog;

public class QuadView extends JFrame implements LoadCallback {
	private static final double SIZE = 100;
	private static final int BUF = 2;
	private QuadView frame;
	private Connection con;
	private HashMap<String,Image> currentImages = new HashMap<String,Image>();
	private QuadPanel panel;
	private int currentLayer = 2;
	private Logger log = Logger.getLogger(this.getClass());

	int x1, x2, y1, y2;
	int offsetX = 0;
	int offsetY = 0;

	double zoom = 1;
	int cellWidth = -1;

	private ImageLoader loader = new ImageLoader();

	public QuadView() {
		frame = this;
		
		// init
		con = ConnectionFactory.getByName("images");
		
		loader.addCallback(this);

		setPreferredSize(new Dimension(410, 420));

		panel = new QuadPanel();
		setContentPane(panel);

		pack();
		setVisible(true);

		
		
		updateImages();

	}

	public ArrayList<Image> rangeImages(int layerId, int x1, int y1, int x2,
			int y2) {
		ArrayList<Image> list = new ArrayList<Image>();
		PreparedStatement stmt;
		try {
			stmt = con
					.prepareStatement("select id, fotoliaId, thumbPath, x, y, quadScore from images_"
							+ layerId
							+ " where x >= ? and x <= ? && y >= ? && y <= ?");
			stmt.setInt(1, x1);
			stmt.setInt(2, x2);
			stmt.setInt(3, y1);
			stmt.setInt(4, y2);

			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Image i = new Image(rs.getInt(1), rs.getInt(2),
						rs.getString(3), null);
				i.setPosition(rs.getInt(4), rs.getInt(5));
				i.setQuadScore(rs.getFloat(6));
				list.add(i);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return list;
	}
	
	public void zoomIn(){
		int nl = currentLayer - 1;
		if(nl < 0)return;
		
		zoom *= 0.5;
		offsetX *= 2;
		offsetY *= 2;
		currentLayer = nl;
		log.info("Zoomed in, layer="+currentLayer);
		updateImages();
	}
	
	public void zoomOut(){
		int nl = currentLayer + 1;
		if(nl > 7)return;
		
		zoom *= 2;
		offsetX /= 2;
		offsetY /= 2;
		currentLayer = nl;
		log.info("Zoomed out, layer="+currentLayer);
		updateImages();
	}

	public void updateImages() {
		int w = panel.getWidth();
		int h = panel.getHeight();
		int m = Math.max(w, h);

		cellWidth = (int) Math.round(zoom * SIZE);

		// calculate viewable range
		x1 = offsetX;
		x2 = (int) (x1 + Math.floor(w / cellWidth));
		y1 = offsetY;
		y2 = (int) (y1 + Math.floor(h / cellWidth));
		
		setTitle("[" + currentLayer + "] " + x1 + "/" + y1 + " // " + x2 + "/" + y2 + " +" + offsetX + "/" + offsetY + " ((" + loader.remaining());

		// get images
		ArrayList<Image> list = rangeImages(currentLayer, x1-BUF, y1-BUF, x2+BUF, y2+BUF);

		// preload
		for (Image i : list) {
			loader.preload(i.getUrl());
		}
		//loader.waitFor();

		// swap
		currentImages.clear();
		for(Image i : list){
			currentImages.put(i.getUrl(), i);
		}

		// update ui
		panel.updateUI();
	}

	private class QuadPanel extends JPanel implements ComponentListener,
			MouseWheelListener, MouseMotionListener, KeyListener {
		
		private HashMap<Integer,Integer> keyMap = new HashMap<Integer,Integer>();

		public QuadPanel() {
			addComponentListener(this);
			addMouseWheelListener(this);
			addMouseMotionListener(this);
			frame.addKeyListener(this);
		}

		@Override
		public void paint(Graphics g) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, getWidth(), getHeight());

			// draw all images
			for (Image i : currentImages.values()) {
				drawImage(i, g);

			}

			//

		}
		
		private void drawImage(Image i, Graphics g){
			Graphics2D g2 = (Graphics2D)g;
			int thickness = 3;
			Stroke oldStroke = g2.getStroke();
			g2.setStroke(new BasicStroke(thickness));			
			
			
			int x = i.getX() - offsetX;
			int y = i.getY() - offsetY;
			float s = i.getQuadScore();
			
			BufferedImage bi = loader.getImageForUrl(i.getUrl());
			if (bi != null) {
				
				g.drawImage(bi, x * cellWidth, y * cellWidth, cellWidth,
						cellWidth, this);
					
				if(s > -1){
					s *= s;
					System.out.println(s);
					g2.setColor(new Color(1.0f-s, s, 0));
					g2.drawRect(x * cellWidth, y * cellWidth, cellWidth,
							cellWidth);
				}
			}
			
			g2.setStroke(oldStroke);
		}
		
		private boolean isPressed(int kc){
			Integer v = keyMap.get(kc);
			if(v == null)return false;
			return v.equals(1);
		}

		@Override
		public void componentResized(ComponentEvent e) {
			updateImages();
		}

		@Override
		public void componentMoved(ComponentEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void componentShown(ComponentEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void componentHidden(ComponentEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int d = e.getWheelRotation();
			
			if(isPressed(KeyEvent.VK_CONTROL)){
				// zoom view
				zoom = zoom - d*(zoom*0.1);
				updateImages();
				System.out.println(zoom);
			}else{
				// zoom layer
				if(d == -1){
					zoomOut();
				}else{
					
					zoomIn();
				}			
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			int mouseX = (e.getX() / cellWidth)+offsetX;
			int mouseY = (e.getY() / cellWidth)+offsetY;
			System.out.println(mouseX + "/" + mouseY);
		}

		@Override
		public void keyTyped(KeyEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void keyPressed(KeyEvent e) {
			keyMap.put(e.getKeyCode(), 1);
			
			int f = isPressed(KeyEvent.VK_CONTROL) ? 10 : 1;
			
			if(e.getKeyCode() == KeyEvent.VK_LEFT){			
				offsetX -= 1*f;
				updateImages();
			}
			if(e.getKeyCode() == KeyEvent.VK_RIGHT){
				offsetX += 1*f;
				updateImages();
			}
			if(e.getKeyCode() == KeyEvent.VK_UP){			
				offsetY -= 1*f;
				updateImages();
			}
			if(e.getKeyCode() == KeyEvent.VK_DOWN){
				offsetY += 1*f;
				updateImages();
			}
			
			if(e.getKeyCode() == KeyEvent.VK_SPACE){
				offsetY = 0;
				offsetX = 0;
				currentLayer = 2;
				zoom = 1;
				updateImages();
			}
			
			if(e.getKeyCode() == KeyEvent.VK_C){
				loader.clearCache();
			}
			
			if(e.getKeyCode() == KeyEvent.VK_R){
				Random r = new Random();
				offsetX = r.nextInt(100);
				offsetY = r.nextInt(100);
				updateImages();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			keyMap.put(e.getKeyCode(), 0);			
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		VisConfig.load();
		VisLog.initLogging();

		new QuadView();
	}

	@Override
	public void onImage(CacheObject o) {
		
		Image i = currentImages.get(o.url);
		if(i != null){
			panel.drawImage(i, panel.getGraphics());
		}
		//panel.updateUI();
	}

}
