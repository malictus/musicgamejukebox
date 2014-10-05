package malictus.musicgamejukebox.dtb;

import java.io.*;
import java.util.*;
import malictus.robusta.file.*;

public class DTBFile {

	private File DTBFile;
	private File DTAFile;
	private int key;
	//Vector of DTB entries (which store DTBValue objects) for this file
	private Vector<DTBNode> DTBNodes;

	public DTBFile(File theFile) throws Exception {
		DTBNodes = new Vector<DTBNode>();
		//create DTB file from ARK
		DTBFile = theFile;
		//create a fresh file for the DTA file
		String dtaName = DTBFile.getName().substring(0, DTBFile.getName().length() - 4);
		dtaName = dtaName + ".dta";
		DTAFile = new File(DTBFile.getParent() + File.separator + dtaName);
		if (DTAFile.exists()) {
			DTAFile.delete();
		}
		DTAFile.createNewFile();
		DTAFile.deleteOnExit();
		SmartRandomAccessFile rafin = new SmartRandomAccessFile(DTBFile, "r");
		key = (int)rafin.read32BitUnsignedLE();
		rafin.close();
		FileInputStream fis = new FileInputStream(DTBFile);
		FileOutputStream fos = new FileOutputStream(DTAFile);
		DecryptionTable dt = new DecryptionTable(key);
		try {
			byte[] buf = new byte[(int)DTBFile.length() - 4];
			fis.skip(4);
			int x = fis.read(buf);
			if (x != buf.length) {
				throw new Exception("Error decrypting DTB file");
			}
			dt.decrypt(buf);
			fos.write(buf);
		} catch (Exception err) {
			fis.close();
			fos.close();
			throw err;
		}
		fis.close();
		fos.close();
		createLookupTable();
	}

	/*************************** GENERAL PUBLIC METHODS **********************/

	public File getDTBFile() {
		return DTBFile;
	}

	public boolean nodeExists(String node) throws Exception {
		int counter = 0;
		while (counter < DTBNodes.size()) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			if (x.path.equals(node)) {
				return true;
			}
			counter = counter + 1;
		}
		return false;
	}

	/**
	 * Return a string vector with the node names of all direct child nodes of this node, stripping
	 * out the parent node portion
	 */
	public Vector<String> getChildNodesFor(String nodeLabel) throws Exception {
		int counter = 0;
		Vector<String> vec = new Vector<String>();
		while (counter < DTBNodes.size()) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.startsWith(nodeLabel) && (!node.equals(nodeLabel))) {
				if (node.indexOf("/") != -1) {
					node = node.substring(node.indexOf("/") + 1);
					if (node.indexOf("/") == -1) {
						vec.add(node);
					}
				}
			}
			counter = counter + 1;
		}
		return vec;
	}

	public int getNodePosition(String nodeLabel) throws Exception {
		int counter = 0;
		int realfilepos = -1;
		while (counter < DTBNodes.size()) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.equals(nodeLabel)) {
				realfilepos = x.filepos;
			}
			counter = counter + 1;
		}
		if (realfilepos == -1) {
			throw new Exception("Node not found");
		}

		//find parent node
		counter = 0;
		String keyFolder = "";
		String keyFile = "";
		if (nodeLabel.lastIndexOf("/") == -1) {
			keyFolder = "";
			keyFile = nodeLabel;
		} else {
			keyFolder = nodeLabel.substring(0, nodeLabel.lastIndexOf("/"));
			keyFile = nodeLabel.substring(nodeLabel.lastIndexOf("/") + 1);
		}
		int filepos = 0;
		if (keyFolder.equals("")) {
			filepos = -3;
		} else {
			boolean foundit = false;
			while (counter < DTBNodes.size() && (foundit == false) ) {
				DTBNode x = (DTBNode)DTBNodes.get(counter);
				String node = x.path;
				if (node.equals(keyFolder)) {
					foundit = true;
					filepos = x.filepos;
				}
				counter = counter + 1;
			}
			if (!foundit) {
				throw new Exception("DTB node not found");
			}
		}

		SmartRandomAccessFile raf = new SmartRandomAccessFile(DTAFile, "r");
		//skip node number
		raf.seek(filepos + 4);
		int originalchildren = raf.read16BitUnsignedLE();
		//skip this node's id
		raf.skipBytes(4);
		if (filepos != -3) {
			//skip first child (should be node name)
			int numb = (int)raf.read32BitUnsignedLE();
			if (numb != 5) {
				//wasn't keyword
				raf.close();
				throw new Exception("Not keyword");
			}
			int skipper = (int)raf.read32BitUnsignedLE();
			int innercounter = 0;
			while (innercounter < skipper) {
				raf.read();
				innercounter = innercounter + 1;
			}
		}
		int returnval = 1;
		//now should be at start of first non-name node, position 1
		while (raf.getFilePointer() != realfilepos) {
			skipNode(raf);
			returnval = returnval + 1;
			if (raf.getFilePointer() >= (raf.length() + 1)) {
				raf.close();
				throw new Exception("node note found");
			}
		}
		raf.close();
		return returnval;
	}

	/*************************** PUBLIC VALUE READING METHODS **********************/

	public Float readFloat(String floatLabel) throws Exception {
		int counter = 0;
		while (counter < DTBNodes.size()) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.equals(floatLabel)) {
				Object obj = x.theValue;
				if (! (obj instanceof DTBValue)) {
					throw new Exception("DTBValue object not found.");
				}
				DTBValue dtbval = (DTBValue)obj;
				if (! (dtbval.value instanceof Float)) {
					throw new Exception("DTBValue is not float.");
				}
				return (Float)dtbval.value;
			}
			counter = counter + 1;
		}
		throw new Exception("Float not found");
	}

	public Integer readInt(String intLabel) throws Exception {
		int counter = 0;
		while (counter < DTBNodes.size()) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.equals(intLabel)) {
				Object obj = x.theValue;
				if (! (obj instanceof DTBValue)) {
					throw new Exception("DTBValue object not found.");
				}
				DTBValue dtbval = (DTBValue)obj;
				if (! (dtbval.value instanceof Integer)) {
					throw new Exception("DTBValue is not integer.");
				}
				return (Integer)dtbval.value;
			}
			counter = counter + 1;
		}
		throw new Exception("Integer not found");
	}

	public String readString(String stringLabel) throws Exception {
		int counter = 0;
		while (counter < DTBNodes.size()) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.equals(stringLabel)) {
				Object obj = x.theValue;
				DTBValue dtbval;
				if (! (obj instanceof DTBValue)) {
					//exception ONLY for Trogdor loading tip!
					if (stringLabel.startsWith("loading_tip")) {
						Vector<DTBValue> nodes = (Vector<DTBValue>)obj;
						dtbval = nodes.get(0);
					} else {
						throw new Exception("DTBValue object not found.");
					}
				} else {
					dtbval = (DTBValue)obj;
				}
				if (! (dtbval.value instanceof String)) {
					throw new Exception("DTBValue is not string.");
				}
				return (String)dtbval.value;
			}
			counter = counter + 1;
		}
		throw new Exception("String not found");
	}

	public Vector readVector(String vectorLabel) throws Exception {
		int counter = 0;
		while (counter < DTBNodes.size()) {
			DTBNode x = (DTBNode)DTBNodes.get(counter);
			String node = x.path;
			if (node.equals(vectorLabel)) {
				Object obj = x.theValue;
				if (! (obj instanceof Vector)) {
					throw new Exception("Vector object not found.");
				}
				return (Vector)obj;
			}
			counter = counter + 1;
		}
		throw new Exception("Vector not found");
	}

	/*************************** PRIVATE METHODS **********************/

	/*
	 * Re-read the current DTA file, and repopulate all local vars
	 */
	private void createLookupTable() throws Exception {
		SmartRandomAccessFile raf = new SmartRandomAccessFile(DTAFile, "r");
		DTBNodes.clear();
		//read number of top-level nodes
		raf.skipBytes(1);
		int topNodes = raf.read16BitUnsignedLE();
		raf.skipBytes(4);
		try {
			int counter = 0;
			while (counter < topNodes) {
				String nodeString = "";
				processNode(raf, nodeString);
				counter = counter + 1;
			}
		} catch (Exception err) {
			err.printStackTrace();
			raf.close();
			throw err;
		}
		raf.close();
	}

	/**
	 * Read through the nodes, creating the DTBNodes vector as we go
	 */
	private void processNode(SmartRandomAccessFile raf, String nodeString) throws Exception {
		//always assume we started at a node, so read in the node
		int filepos = (int)raf.getFilePointer();		//for storing later
		int numb = (int)raf.read32BitUnsignedLE();
		if ( (numb != 16) && (numb != 17) && (numb != 19) ) {
			//happens sometimes
			raf.seek(raf.getFilePointer() - 4);
			Object x = readValue(raf);		//oddball node
			nodeString = nodeString + " ";
			DTBValue val = new DTBValue(x, numb);
			DTBNode oddNode = new DTBNode(val, 0, filepos, nodeString, -1);
			DTBNodes.add(oddNode);
			return;
		}
		//number of children
		int originalchildren = raf.read16BitUnsignedLE();
		//node ID
		int nodeID = (int)raf.read32BitUnsignedLE();
		if (originalchildren < 1) {
			throw new Exception("No children for node");
		}
		String keyword = "";
		//read first child (which is node name, usually)
		numb = (int)raf.read32BitUnsignedLE();
		int children = originalchildren;
		if (numb != 5) {
			//wasn't keyword
			keyword = " ";
			raf.seek(raf.getFilePointer() - 4);
			children = children + 1;
		} else {
			//was keyword
			int skipper = (int)raf.read32BitUnsignedLE();
			int counter = 0;
			while (counter < skipper) {
				keyword = keyword + (char)raf.read();
				counter = counter + 1;
			}
		}
		if (!nodeString.equals("")) {
			nodeString = nodeString + "/";
		}
		nodeString = nodeString + keyword;
		//now, figure out if other children are nodes or data
		//assumes all nodes are one or the other only
		if (children == 1) {
			DTBNode oddNode = new DTBNode(null, 0, filepos, nodeString, nodeID);
			DTBNodes.add(oddNode);
			return;
		}
		int counter = 0;
		numb = (int)raf.read32BitUnsignedLE();
		raf.seek(raf.getFilePointer() - 4);	//back up since we'll re-read this value below
		if ( (numb != 16) && (numb != 17) && (numb != 19) ) {
			//DATA FOUND
			if (children == 2) {
				//single value
				Object singleValue = readValue(raf);
				DTBValue val = new DTBValue(singleValue, numb);
				DTBNode node = new DTBNode(val, 0, filepos, nodeString, nodeID);
				DTBNodes.add(node);
			} else {
				//more than one object here
				Vector<DTBValue> v = new Vector<DTBValue>();
				counter = 1;
				while (counter < children) {
					Object singleValue = readValue(raf);
					DTBValue val = new DTBValue(singleValue, numb);
					v.add(val);
					counter = counter + 1;
				}
				DTBNode node = new DTBNode(v, 0, filepos, nodeString, nodeID);
				DTBNodes.add(node);
			}
		} else {
			//MORE NODES
			DTBNode node = new DTBNode(null, originalchildren, filepos, nodeString, nodeID);
			DTBNodes.add(node);
			counter = 0;
			while (counter < (children - 1) ) {
				//recurse
				processNode(raf, nodeString);
				counter = counter + 1;
			}
		}
	}

	/**
	 * Like process node, but just skips a node in the file rather than writing anything
	 */
	private void skipNode(SmartRandomAccessFile raf) throws Exception {
		//always assume we started at a node, so read in the node
		int numb = (int)raf.read32BitUnsignedLE();
		if ( (numb != 16) && (numb != 17) && (numb != 19) ) {
			//happens sometimes
			raf.seek(raf.getFilePointer() - 4);
			readValue(raf);		//oddball node
			return;
		}
		//number of children
		int originalchildren = raf.read16BitUnsignedLE();
		if (originalchildren < 1) {
			throw new Exception("No children for node");
		}
		//node id
		raf.skipBytes(4);
		//read first child (which is node name, usually)
		numb = (int)raf.read32BitUnsignedLE();
		int children = originalchildren;
		if (numb != 5) {
			raf.seek(raf.getFilePointer() - 4);
			children = children + 1;
		} else {
			//was keyword
			int skipper = (int)raf.read32BitUnsignedLE();
			int counter = 0;
			while (counter < skipper) {
				raf.read();
				counter = counter + 1;
			}
		}
		//now, figure out if other children are nodes or data
		//assumes all nodes are one or the other only
		if (children == 1) {
			return;
		}
		int counter = 0;
		numb = (int)raf.read32BitUnsignedLE();
		raf.seek(raf.getFilePointer() - 4);	//back up since we'll re-read this value below
		if ( (numb != 16) && (numb != 17) && (numb != 19) ) {
			//DATA FOUND
			if (children == 2) {
				//single value
				readValue(raf);
			} else {
				//more than one object here
				counter = 1;
				while (counter < children) {
					readValue(raf);
					counter = counter + 1;
				}
			}
		} else {
			//MORE NODES
			counter = 0;
			while (counter < (children - 1) ) {
				//recurse
				skipNode(raf);
				counter = counter + 1;
			}
		}
	}

	/**
	 * Read and return a single (non-tree node) value
	 */
	private Object readValue(SmartRandomAccessFile raf) throws Exception {
		int numb = (int)raf.read32BitUnsignedLE();
		int skipper;
		if (numb == 0) {
			//32 bit int
			int val = (int)raf.read32BitUnsignedLE();
			Integer intVal = new Integer(val);
			return intVal;
		} else if (numb == 1) {
			//32 bit float
			int val = (int)raf.read32BitUnsignedLE();
			Float floatVal = new Float(Float.intBitsToFloat(val));
			return floatVal;
		} else if (numb == 2) {
			//function name
			skipper = (int)raf.read32BitUnsignedLE();
			raf.skipBytes(skipper);
			return null;
		} else if (numb == 5) {
			//keyword (sometimes shows up here); treat as a string
			skipper = (int)raf.read32BitUnsignedLE();
			int innercounter = 0;
			String stringVal = "";
			while (innercounter < skipper) {
				stringVal = stringVal + (char)raf.read();
				innercounter = innercounter + 1;
			}
			return stringVal;
		} else if (numb == 6) {
			//unknown
			raf.skipBytes(4);
			return null;
		} else if (numb == 7) {
			//unknown
			skipper = (int)raf.read32BitUnsignedLE();
			raf.skipBytes(skipper);
			return null;
		} else if (numb == 8) {
			//unknown
			raf.skipBytes(4);
			return null;
		} else if (numb == 9) {
			//unknown
			raf.skipBytes(4);
			return null;
		} else if (numb == 18) {
			//STRING VALUE
			skipper = (int)raf.read32BitUnsignedLE();
			int innercounter = 0;
			String stringVal = "";
			while (innercounter < skipper) {
				stringVal = stringVal + (char)raf.read();
				innercounter = innercounter + 1;
			}
			return stringVal;
		} else if (numb == 32) {
			//string
			skipper = (int)raf.read32BitUnsignedLE();
			raf.skipBytes(skipper);
			return null;
		} else if (numb == 33) {
			//file ref
			skipper = (int)raf.read32BitUnsignedLE();
			raf.skipBytes(skipper);
			return null;
		} else if (numb == 34) {
			//file ref
			skipper = (int)raf.read32BitUnsignedLE();
			raf.skipBytes(skipper);
			return null;
		} else if (numb == 35) {
			//string
			skipper = (int)raf.read32BitUnsignedLE();
			raf.skipBytes(skipper);
			return null;
		} else {
			throw new Exception("Incorrect DTB reference: " + numb);
		}
	}

}
