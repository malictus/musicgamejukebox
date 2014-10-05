package malictus.musicgamejukebox.qb;

import java.io.*;
import java.util.*;
import malictus.robusta.file.*;

public class QBFile {

	File theFile;
	Vector<QBNameEntry> entries;
	static public QBHash hash = new QBHash();

	public QBFile(File qbFile) throws Exception {
		entries = new Vector<QBNameEntry>();
		this.theFile = qbFile;
		SmartRandomAccessFile raf = new SmartRandomAccessFile(theFile, "r");
		raf.seek(4);
		long size = raf.read32BitUnsignedLE();
		raf.skipBytes(20);
		int curSection = 0;

		while (raf.getFilePointer() < size) {
			int cand = (int)raf.read32BitUnsignedLE();
			if ( (cand == 0x00030400) || (cand == 0x000C0400) || (cand == 0x000A0400) || (cand == 0x00070400) )  {
				int sectionId = (int)raf.read32BitUnsignedLE();
				curSection = sectionId;
				int fileId = (int)raf.read32BitUnsignedLE();
				int pointerStart = (int)raf.read32BitUnsignedLE();
				raf.skipBytes(4);
				raf.seek(pointerStart);
				if (cand == 0x00030400) {
					String x = raf.readNullTerminatedString();
					while ((raf.getFilePointer() % 4) != 0) {
						raf.seek(raf.getFilePointer() + 1);
					}
				} else if (cand == 0x00070400) {
					raf.skipBytes(8);
					raf.skipBytes((int)raf.read32BitUnsignedLE());
					while ((raf.getFilePointer() % 4) != 0) {
						raf.seek(raf.getFilePointer() + 1);
					}
				}
			} else if (cand == 0x000D0100) {
				int numentries = (int)raf.read32BitUnsignedLE();
				if (numentries > 1) {
					raf.seek((int)raf.read32BitUnsignedLE());
				}
				raf.skipBytes(4 * numentries);
			} else if (cand == 0x00010000) {
				if (curSection == hash.hashit("permanent_songlist_props")) {
					parseStruct(raf, true);
				} else {
					parseStruct(raf, false);
				}
			} else if (cand == 0x00000100) {
				raf.skipBytes(8);
			} else {
				raf.close();
				throw new Exception("QB data not recognized " + Long.toHexString(cand));
			}
		}

		raf.close();

	}

	private void parseStruct(SmartRandomAccessFile raf, boolean thisIsIt) throws Exception {
		String internalName = "";
		String title = "";
		String artist = "";
		long x = raf.read32BitUnsignedLE();
		while (x != 0) {
			raf.seek(x);
			int type = (int)raf.read32BitUnsignedLE();
			long entryName = raf.read32BitUnsignedLE();
			if ( (type == 0x00003500) || (type == 0x00001B00) ) {
				int entryValue = (int)raf.read32BitUnsignedLE();
				x = (int)raf.read32BitUnsignedLE();
			} else if (type == 0x00000300) {
				int entryValue = (int)raf.read32BitUnsignedLE();
				x = (int)raf.read32BitUnsignedLE();
			} else if (type == 0x00000700) {
				int seeker = (int)raf.read32BitUnsignedLE();
				x = (int)raf.read32BitUnsignedLE();
				raf.seek(seeker);
				String string = raf.readNullTerminatedString();
				if (entryName == hash.hashit("name")) {
					internalName = string;
				}
				if (entryName == hash.hashit("title")) {
					title = string;
				}
				if (entryName == hash.hashit("artist")) {
					artist = string;
				}
				while ((raf.getFilePointer() % 4) != 0) {
					raf.seek(raf.getFilePointer() + 1);
				}
			} else if (type == 0x00000500) {
				int entryValue = (int)raf.read32BitUnsignedLE();
				x = (int)raf.read32BitUnsignedLE();
			} else if (type == 0x00001500) {
				int seeker = (int)raf.read32BitUnsignedLE();
				x = (int)raf.read32BitUnsignedLE();
				raf.seek(seeker + 4);
				parseStruct(raf, thisIsIt);
			} else {
				raf.close();
				throw new Exception("QB data not recognized");
			}
		}
		if ( (!internalName.equals("")) && (!title.equals("")) ) {
			entries.add(new QBNameEntry(internalName, title, artist));
		}
	}

	public Vector<QBNameEntry> getEntries() {
		return entries;
	}

	public File getQBFile() {
		return theFile;
	}

}
