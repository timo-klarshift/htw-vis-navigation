package org.htw.vis.matching;

import java.io.Serializable;
import java.util.HashMap;

import org.htw.vis.lucene.ImageRetriever;

public class MatchParty implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1213298531887048923L;
	private final int id;
	private Integer pairId = -1;	
	private final int _hash;
	
	private HashMap<Integer,Byte> preferences = new HashMap<Integer,Byte>();	
		
	public MatchParty(int id){
		this.id = id;
		this._hash = new Integer(id).hashCode();
	}
	
	public int hashCode(){
		return _hash;
	}
	
	public HashMap<Integer,Byte> getPreferences(){
		return preferences;
	}
	
	public boolean equals(Object obj) {
		if(this == obj){
			return true;
		}
		
		if(obj instanceof MatchParty == false){
			return false;
		}
		
		if(((MatchParty)obj).getId() == id){
			return true;
		}
		
		return false;
	}
	
	public int getId(){
		return id;
	}
	
	public int getPairId(){
		return pairId;
	}
	
	private void preferByte(Integer partyId, Byte rank){		
		preferences.put(partyId,rank);
	}
	
	public void prefer(Integer partyId, float rank){
		preferByte(partyId, ImageMatching.encodeRank(rank));
	}	
	
	public String toString(){
		return "" + id + "(" + pairId + ")";
	}
	
	public boolean propose(Integer propId){			
		// is proposed party preferred?
		Byte pRank = getRank(propId);
		Byte cRank = getRank(pairId);		
				
		if(pRank > cRank || (pairId == -1)){
			return true;
		}
					
		return false;
	}
	
	public Byte getRank(Integer id){
		
		Byte b = preferences.get(id);
				
		if(b == null)return (byte)ImageMatching.RANK_LOWEST;
		return b;
	}
	
	public boolean isPaired(){
		return !pairId.equals(-1);
	}
	
	public void pair(int id){
		this.pairId = id;
	}
	
	public void unpair(){
		pairId = -1;
	}

	public void unprefer(Integer r) {
		preferences.remove(r);
	}
}
