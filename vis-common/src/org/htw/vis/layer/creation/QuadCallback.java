package org.htw.vis.layer.creation;

import org.htw.vis.layer.NetworkNode;

public interface QuadCallback {
	public void onQuadEmit(NetworkNode r, NetworkNode n1, NetworkNode n2, NetworkNode n3, NetworkNode n4);
}
