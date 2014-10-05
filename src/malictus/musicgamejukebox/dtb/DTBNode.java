package malictus.musicgamejukebox.dtb;

/**
 * DTBNode
 *
 * An object that represents a single DTB node.
 *
 * by Jim Halliday
 * malictus@malictus.net
 *
 */
public class DTBNode {

	/*
	 * The actual value represented by this node. This will be a DTBValue object, or a (possibly empty)
	 * Vector of DTBValue objects. This value will be NULL if the node contains child nodes rather than
	 * values, or is empty.
	 */
	Object theValue;
	int childNodeCount;
	//start position of this node in the file
	int filepos;
	//node path to this node
	String path;
	//node ID; will be -1 for ID-less nodes
	int nodeID;

	public DTBNode(Object theValue, int childNodeCount, int filepos, String path, int nodeID) {
		this.theValue = theValue;
		this.childNodeCount = childNodeCount;
		this.filepos = filepos;
		this.path = path;
		this.nodeID = nodeID;
	}

}

