package rsr;

import terrain.HexRef;

/**
 * 
 * @author morvael
 */
public interface HexControlInfo {

	String getSide();
	boolean canEnter(HexRef hex);
	
}
