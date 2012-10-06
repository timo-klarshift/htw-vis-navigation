package org.htw.vis.server;

import org.htw.vis.server.context.ContainerImage;
import org.htw.vis.server.protocol.VISUpdate;

public interface ClientListener {
	public void onSessionStarted();
	public void onImagesUpdate(VISUpdate update);
	public void onImagePreload(ContainerImage image);
}
