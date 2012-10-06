package vis.clustering.quad;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 * visualize the quad finding process
 * 
 * @author timo
 *
 */
public class QuadInspector extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5078896223438741963L;
	private Quadrator quad;
	public static QuadInspector instance;
	private BufferedImage bi = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
	private Graphics big = bi.getGraphics();
	private int s = 0;
	
	public QuadInspector(Quadrator quad){
		this.quad = quad;
		
		setPreferredSize(new Dimension(400, 400));
		pack();
		setResizable(false);
		setVisible(true);
		
		
		instance = this;
	}
	
	@Override
	public void paint(Graphics g) {
		//super.paint(g);
	    Graphics2D g2 = (Graphics2D) g;

		
		big.setColor(Color.WHITE);
		big.fillRect(0,  0,  getWidth(),  getHeight());
		
		double[] qs = quad.getQuadScore();
		if(qs == null)return;
		int ql = qs.length;
		int q2 = (int) Math.max(1,  Math.round(Math.sqrt(ql)));
		
		
		int ga = 40;
		int w = (getWidth()-ga) / q2;
		int b = (int) (w * 0.9);
		for(int cy=0; cy<q2; cy++){
			for(int cx=0; cx<q2; cx++){
				// get quad
				int i = cy*q2 + cx;
				if(i < qs.length){
				
					// get quad score
					double s = qs[i];
					
					float col = (float) Math.min(1, 1*s);
					big.setColor(new Color(1-col, col, 0));
					
					int x = ga/2+cx*w;
					int y = ga/2+20 + (cy*w);
					big.fillRect(x,  y,  b,  b);
				}
			}	
		}
		
		// draw image
		g2.drawImage(bi,  0,  0,  this);
		
		// clean up
		g.dispose();
		g2.dispose();
		
		// draw
		/*try {
			ImageIO.write(bi, "png", new File("out-" + (s++) + ".png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/		
	}
}
