package org.htw.vis.layer;

/**
 * related node
 * 
 * @author timo
 *
 */
public class RelatedNode {
	private final NetworkNode node, source;
	private final float similarity;
	
	/**
	 * create related node
	 * 
	 * @param node
	 * @param source
	 * @param similarity
	 */
	public RelatedNode(NetworkNode node, NetworkNode source, float similarity){
		this.source = source;
		this.node = node;
		this.similarity = similarity;
	}
	
	/**
	 * get the node related with
	 * @return
	 */
	public final NetworkNode getNode(){
		return node;
	}
	
	/**
	 * get the source the relation concerns to
	 * @return
	 */
	public final NetworkNode getSource(){
		return source;
	}
	
	/**
	 * get the weight of the relation
	 * @return
	 */
	public final float getSimilarity(){
		return similarity;
	}
	
	/**
	 * get the string representation
	 */
	public String toString(){
		return ""+similarity + " / " + node.getFotoliaId() + " :: source=" + source.getFotoliaId();
	}
}
