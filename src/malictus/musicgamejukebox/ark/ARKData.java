package malictus.musicgamejukebox.ark;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import malictus.robusta.file.*;
import malictus.musicgamejukebox.dtb.*;
import malictus.musicgamejukebox.player.*;

/**
 * A class that represents the data in the HDR of an ARK/HDR combination file.
 * You can use this file to do lookups of the HDR data.
 */
public class ARKData {

	private File theHeaderFile;
	private File theArkFile;

	private Vector<String> stringTableElements = new Vector<String>();
	private Vector<Long> section2Offsets = new Vector<Long>();
	private Vector<Section3Entry> section3Entries = new Vector<Section3Entry>();

	/**
	 * Initialize by specifying either the HDR or ARK file. Class assumes that
	 * the other file is in the correct relative location.
	 */
	public ARKData(File inputFile, boolean isHeaderFile) throws Exception {
		if (inputFile == null) {
			throw new Exception("Input file is null");
		}
		if (inputFile.isDirectory()) {
			throw new Exception("Input file is directory");
		}
		if (isHeaderFile) {
			theHeaderFile = inputFile;
			theArkFile = new File(theHeaderFile.getParent() + "/MAIN_0.ARK");
			if (!theArkFile.exists()) {
				throw new Exception("No corresponding ARK file");
			}
		} else {
			theArkFile = inputFile;
			theHeaderFile = new File(theArkFile.getParent() + "/MAIN.HDR");
			if (!theHeaderFile.exists()) {
				throw new Exception("No corresponding HDR file");
			}
		}
		SmartRandomAccessFile raf = new SmartRandomAccessFile(theHeaderFile, "r");
		//skip to the part that tells us the size of the ARK file
		raf.seek(12);
		long arkSize = raf.read32BitUnsignedLE();
		//size of the string table (section 1) in bytes
		long section1Size = raf.read32BitUnsignedLE();
		//skip to the part of the file that tells us the number of 32-bit string offsets in section 2
		raf.seek(20 + section1Size);
		//number of 32-bit string offsets in section 2
		long section2Size = raf.read32BitUnsignedLE();
		//skip to the part of the file that tells us the number of file entries in section 3
		raf.seek(24 + section1Size + (section2Size * 4));
		long section3Size = raf.read32BitUnsignedLE();
		long section1Start = 20;
		long section2Start = 24 + section1Size;
		long section3Start = 28 + section1Size + (section2Size * 4);
		//now begin populating vectors, starting with String table vector
		raf.seek(section1Start + 1);	//skip over first character, which is null
		while (raf.getFilePointer() < (section2Start - 4)) {
			String string = raf.readNullTerminatedString();
			stringTableElements.add(string);
		}
		//now populate vector of section 2 offsets
		raf.seek(section2Start);
		while (raf.getFilePointer() < (section3Start - 4)) {
			section2Offsets.add(new Long(raf.read32BitUnsignedLE()));
		}
		//lastly, populate the file vector
		raf.seek(section3Start);
		while (raf.getFilePointer() < raf.length() - 1) {
			long offsetIntoHDR = raf.getFilePointer();
			long fileOffset = raf.read32BitUnsignedLE();
			long filenameStringID = raf.read32BitUnsignedLE();
			long directoryStringID = raf.read32BitUnsignedLE();
			long fileSize = raf.read32BitUnsignedLE();
			long shouldBeZero = raf.read32BitUnsignedLE();
			if (shouldBeZero != 0) {
				throw new Exception("Error parsing HDR file.");
			}
			Section3Entry s3e = new Section3Entry(fileOffset, filenameStringID, directoryStringID, fileSize, offsetIntoHDR);
			section3Entries.add(s3e);
		}
		raf.close();
	}

	public File getHeaderFile() {
		return theHeaderFile;
	}

	public File getArkFile() {
		return theArkFile;
	}

	/**
	 * Find a string from the string table, given its ID from section 3
	 */
	public String getNameFor(long ID) throws Exception {
		long offset = ((Long)section2Offsets.get((int)ID)).longValue();
		long x = 1;	//start at one since string table has null char at beginning
		int counter = 0;
		while (counter < stringTableElements.size()) {
			String check = (String)stringTableElements.get(counter);
			if (x == offset) {
				return check;
			}
			x = x + check.length() + 1;
			counter = counter + 1;
		}
		throw new Exception("Unable to look up string in string table");
	}

	/**
	 * Find a section 3 ID number, given a string from the string table
	 * @return -1 if not found
	 */
	public long getIDFor(String fileName) throws Exception {
		//first, find the offset for this string in the string table
		long x = 1;
		int counter = 0;
		long offset = -1;
		while (counter < stringTableElements.size() && (offset == -1) ) {
			String check = (String)stringTableElements.get(counter);
			if (check.equals(fileName) ) {
				offset = x;
			}
			x = x + check.length() + 1;
			counter = counter + 1;
		}
		if (offset == -1) {
			return offset;
		}
		counter = 0;
		while (counter < section2Offsets.size()) {
			Long check = (Long)section2Offsets.get(counter);
			if (check.longValue() == offset) {
				return counter;
			}
			counter = counter + 1;
		}
		throw new Exception("Matching string not found in string table");
	}

	/**
	 * Find out if a given file exists in this HDR/ARK
	 */
	public boolean fileExists(String fileName, String dirName) throws Exception {
		int counter = 0;
		while (counter < section3Entries.size()) {
			Section3Entry s = section3Entries.get(counter);
			if (getNameFor(s.getFileID()).equals(fileName) &&
					getNameFor(s.getDirectoryID()).equals(dirName)) {
				return true;
			}
			counter = counter + 1;
		}
		return false;
	}

	/**
	 * Get the file with the given name and put it in the temp directory
	 */
	public File putInTemp(String fileName, String dirName) throws Exception {
		int counter = 0;
		while (counter < section3Entries.size()) {
			Section3Entry s = (Section3Entry)section3Entries.get(counter);
			if ( (getNameFor(s.getFileID()).equals(fileName)) &&
					(getNameFor(s.getDirectoryID()).equals(dirName)) ) {
				File outfile = File.createTempFile("mgj", ".dtb");
				outfile.createNewFile();
				outfile.deleteOnExit();
				FileChannel fos = new FileOutputStream(outfile).getChannel();
				FileChannel inARK = new FileInputStream(theArkFile).getChannel();
	    	    inARK.position(s.getFileOffset());
	    	    long x = fos.transferFrom(inARK, 0, s.getFileSize());
	    	    if (x != s.getFileSize()) {
	    	    	throw new Exception("File not transferred correctly");
	    	    }
				fos.close();
				inARK.close();
				return outfile;
			}
			counter = counter + 1;
		}
		throw new Exception("File not found");
	}

	/**
	 * Return an array of MGJTrack objects that represent every music file in the ARK
	 * @return
	 * @throws Exception
	 */
	public MGJTrack[] getTracks() throws Exception {
		int counter = 0;
		int num = 0;
		while (counter < section3Entries.size()) {
			Section3Entry x = section3Entries.get(counter);
			String name = getNameFor(x.getFileID());
			if (name.endsWith(".vgs")) {
				num = num + 1;
			}
			counter = counter + 1;
		}
		MGJTrack[] tracks = new MGJTrack[num];
		counter = 0;
		num = 0;

		DTBFile songs = null;
		if (fileExists("songs.dtb", "config/gen")) {
			songs = new DTBFile(putInTemp("songs.dtb", "config/gen"));
		}
		while (counter < section3Entries.size()) {
			Section3Entry x = section3Entries.get(counter);
			String name = getNameFor(x.getFileID());
			String dir = getNameFor(x.getDirectoryID());
			if (name.endsWith(".vgs")) {
				String realname = null;
				String realartist = null;
				if (dir.startsWith("songs/")) {
					if (dir.startsWith("songs/graveyardshift")) {
						realname = "Graveyard Shift";
					} else if (dir.startsWith("songs/advharmony")) {
						realname = "Trippolette";
					} else {
						String endOf = dir.substring(dir.lastIndexOf("/") + 1);
						String realnameID = endOf + "/name";
						try {
							realname = songs.readString(realnameID);
						} catch (Exception err) {
							try {
								//KR uses 'title', not 'name'
								realnameID = endOf + "/title";
								realname = songs.readString(realnameID);
							} catch (Exception err23) {
								realname = null;
							}
						}

						String realartistID = endOf + "/artist";
						if (songs != null) {
							try {
								realartist = songs.readString(realartistID);
							} catch (Exception err) {
								realartist = null;
							}
						} else if (dir.startsWith("songs")) {
							if (!dir.startsWith("songs/tutorial")) {
								realname = name;
							}
						}
					}
					if (realname != null) {
						String noext = name.substring(0, name.length() - 4);
						if (noext.contains("_p")) {
							int i = noext.lastIndexOf("_p");
							if (i == (noext.length() - 4)) {
								//this is a practice mode file
								realname = realname + " (" + noext.substring(noext.length() - 2) +
										"% Practice Mode)";
							}
						}
						if (name.endsWith("coop.vgs")) {
							realname = realname + " (Co-op mode)";
						}
						if (noext.endsWith("_duet")) {
							realname = realname + " (Duet)";
						}
					}
				}
				tracks[num] = new MGJTrack(MGJTrack.TRACKTYPE_VGS, theArkFile, x.getFileOffset(),
						x.getFileOffset() + x.getFileSize(), name, dir, realname, realartist);
				num = num + 1;
			}
			counter = counter + 1;
		}
		java.util.Arrays.sort(tracks);
		return tracks;
	}

}

