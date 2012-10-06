package org.htw.vis.server.protocol;

import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.htw.vis.helper.FeatureAccess;
import org.htw.vis.layer.INode;
import org.htw.vis.layer.ZoomLayer;
import org.htw.vis.layer.ZoomWorld;
import org.htw.vis.lucene.ImageRetriever;
import org.htw.vis.lucene.LuceneQueryCallback;
import org.htw.vis.lucene.SearchResult;
import org.htw.vis.server.SocketHandler;
import org.htw.vis.server.context.ContainerImage;
import org.htw.vis.server.context.ImageContainer;
import org.htw.vis.server.context.ServerContext;

/**
 * VISServer
 * 
 * @author timo
 * 
 */
public class VISServer implements SocketHandler.HandlerCallback, Runnable {
	public static final int PORT = 9900;

	private final Logger log = Logger.getLogger("VISServer");

	private VISServer server;
	
	private HashMap<String, ServerContext> sessions = new HashMap<String, ServerContext>();
	private HashMap<String, SocketHandler> connections = new HashMap<String, SocketHandler>();

	private boolean listening = true;
	
	
	

	public VISServer() {
		this.server = this;
	}

	@Override
	public void run() {
		// init world
		ZoomWorld.create(9);

		try {
			// create listen server
			ServerSocket serverSocket = new ServerSocket(PORT);
			log.info("Server running");
			while (listening) {
				Socket clientSocket = serverSocket.accept();
				log.info("Client connected: "
						+ clientSocket.getInetAddress().toString() + " // "
						+ clientSocket.getPort());

				SocketHandler handler = new SocketHandler(clientSocket, this);
				handler.setDelay(0);
				handler.start();

				Thread.sleep(1000);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private SocketHandler getConnection(String sessionId){
		if (sessionId == null)
			return null;
		return connections.get(sessionId);
	}

	public ServerContext getContext(String sessionId) {
		if (sessionId == null)
			return null;
		return sessions.get(sessionId);
	}

	public void start() {
		new Thread(this).start();
	}
	
	private void loadImages(ServerContext ctx, VISLoad load){				
		// shift container
		ImageContainer container = ctx.getContainer();

		// add new images
		ZoomLayer layer = ctx.getLayer();

		// TODO: calculate context
					
		final ArrayList<ContainerImage> results = new ArrayList<ContainerImage>();
		
		LinkedList<INode> contextImages = new LinkedList<INode>();
		for (INode n : container.getContextImages()) {
			contextImages.add(n);						
		}
		
		log.info("(" + contextImages.size() + ") Loading some images ... " + layer);
		
		
		
		
		
		int maxResults = Math.min(load.maxResults, (container.getCapacity() - container.count()));
		
		
		if (contextImages.size() > 0) {

			// retrieve similar images
			ImageRetriever ir = new ImageRetriever(layer.searcher());			
			if (maxResults > 0) {
				for (SearchResult sr : ir.getSimilarImages(contextImages,
						maxResults, 0.1f, 0.4f)) {
					
					if (!container.hasImage(sr.getFotoliaId())) {
						ContainerImage ci = new ContainerImage(sr);
						results.add(ci);
						
						
						// inform client
						//write(ctx, VISProto.PRELOAD, ci);
					}
				}
			}
			
			ir.shutdown();
		}
		
		FeatureAccess.printStats();
		
		

		if (results.size() > 0) {
			log.info("Found " + results.size());
			container.addImages(results);						
		}
		
		
	}
	
	private void setPosition(ServerContext ctx, int x, int y){
		log.info("Set focus " + x + "/" + y);
		ctx.setFocus(x, ctx.getContainer().getHeight() - y);
		ctx.getContainer().updateContext();
	}

	@Override
	public void onPacket(SocketHandler socketHandler, VISPacket packet) {
		String cmd = packet.getCommand();
		ServerContext ctx = getContext(packet.getSessionId());
		
		

		// handle packets here
		log.debug("RECV: " + cmd + " :: " + packet);
					
		try{
			if(ctx == null && cmd.equals(VISProto.START_SESSION)){
				createNewContext(socketHandler, packet);

			}else{
				LinkedList<ContainerImage> ctxImages = ctx.getContainer().getContextImages();
				if(cmd.equals(VISProto.LOAD)){
					VISLoad load = (VISLoad) packet.getData();
					
					System.out.println(load);
					System.out.println(ctx.getContainer().getContextImages().size());
					
					// set position
					setPosition(ctx, load.focusX, load.focusY);
					
					// shift when neccessary
					if(load.shiftX != 0 || load.shiftY != 0){
						// shift container
						ctx.getContainer().shift(load.shiftX, load.shiftY);
					}
					
					if(load.searchQuery != null){
						// perform search
						performSearch(ctx, load);
					}else {
						if(load.shiftZ != 0){
							log.info("Perform Zoom");
							//ctx.getContainer().shiftZ(load.shiftZ);
							
							// only clear those which are not in context
							LinkedList<ContainerImage> rem = new LinkedList<ContainerImage>();
							for(ContainerImage ci : ctx.getContainer().getImages()){
								if(ctxImages.contains(ci)){
									
								}else{
									rem.add(ci);
								}
							}
							for(ContainerImage ci : rem){
								ctx.getContainer().removeImage(ci);
							}
							//ctx.getContainer().clear();
							
							if(load.shiftZ < 0)
								ctx.zoomIn();
							
							if(load.shiftZ > 0)
								ctx.zoomOut();
						}
						
						loadImages(ctx, load);
					}
						
												
				
					
					// loading context images ...
					
					
					VISUpdate update = getUpdate(ctx);
					update.shiftX = load.shiftX;
					update.shiftY = load.shiftY;
					update.shiftZ = load.shiftZ;
					update.layer = ctx.getLayer().getLOD();
					
					write(ctx, VISProto.UPDATE, update);	
					
					
				}else if (cmd.equals(VISProto.CLEAR)) {
					ctx.getContainer().clear();
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
			log.error(e.getMessage());
		}

	}

	private void performSearch(final ServerContext ctx, VISLoad load) {
		if (ctx == null) {
			log.error("Cant search without current context.");
			return;
		}

		String query = load.searchQuery;
		int maxResults = load.maxResults;
		
		log.info("Performing search on `" + query + "`");

		ZoomLayer layer = ctx.getLayer();
		ImageContainer container = ctx.getContainer();

		final ArrayList<ContainerImage> results = new ArrayList<ContainerImage>();

		// clear container
		// container.clear();

		layer.searcher().search(query, maxResults,
				new LuceneQueryCallback.SimpleQueryCallback() {
					@Override
					public void onSearchResult(SearchResult sr) {
						if (sr.getSimilarity() >= 0) {
							ContainerImage ci = new ContainerImage(sr);
							results.add(ci);

							// inform client
							//write(ctx, VISProto.PRELOAD, ci);
						}
					}
				});

		// add images to container
		// and gain positions
		log.info("Found " + results.size() + " results for `" + query
				+ "`");
		
		container.addImages(results);
	}
	
	private void write(ServerContext ctx, String command, Serializable data){
		VISPacket packet = new VISPacket(ctx.getSessionId(), command, data);
		SocketHandler handler = getConnection(ctx.getSessionId());
		handler.writePacket(packet);
	}
	
	private void write(ServerContext ctx, String command){
		write(ctx, command, null);
	}

	private VISUpdate getUpdate(ServerContext ctx) {						
		ImageContainer c = ctx.getContainer();
		
		// create new update
		VISUpdate update = new VISUpdate();			
		
		// images in the container
		for(Integer ri : c.getRemovedImages()){
			update.removeImage(ri);		
		}
		
		// images in the container
		for(ContainerImage ci : c.getImages()){
			update.addImage(ci);
		}
		
		c.getRemovedImages().clear();
					
		return update;
	}

	/**
	 * start a new user session
	 * 
	 * @param handler
	 * @param packet
	 */
	private void createNewContext(SocketHandler handler, VISPacket packet) {
		// create session id
		String sessionId = UUID.randomUUID().toString();

		// create a new container
		ServerContext ctx = new ServerContext(sessionId);
		int[] dim = (int[]) packet.getData();
		ctx.createContainer(dim[0], dim[1]);

		// store context
		sessions.put(sessionId, ctx);
		connections.put(sessionId, handler);

		// send session id to client
		write(ctx, VISProto.START_SESSION);
	}

	/**
	 * server entry point
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Logger.getRootLogger().setLevel(Level.INFO);

		// start server
		new VISServer().start();
	}

}
