package malictus.musicgamejukebox.wad;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import malictus.robusta.file.*;

public class HedWadExtract {

	private File hedFile;
	private File wadFile;
	Vector<HedEntry> hedEntries;

	public HedWadExtract(File hedFile, File wadFile) throws Exception {
		this.hedFile = hedFile;
		this.wadFile = wadFile;
		hedEntries = new Vector<HedEntry>();
		if (!hedFile.exists() || (!wadFile.exists())) {
			throw new Exception("Hed/wad files do not exist");
		}
		SmartRandomAccessFile raf = new SmartRandomAccessFile(hedFile, "r");
		while (raf.getFilePointer() < (raf.length() - 1)) {
			long pos = raf.read32BitUnsignedLE();
			if (pos == 4294967295L) {
				raf.close();
	             break;
	        }
			pos = pos * 2048;
			long start = raf.read32BitUnsignedLE();
			String name = raf.readNullTerminatedString();
			if ((raf.getFilePointer() % 4) != 0) {
				raf.skipBytes(4 - (int)(raf.getFilePointer() % 4));
			}
			HedEntry he = new HedEntry(pos, start, name);
			hedEntries.add(he);
		}
	}

	public File getHedFile() {
		return hedFile;
	}

	public File getWadFile() {
		return wadFile;
	}

	public File putInTemp(String path) throws Exception {
		int counter = 0;
		while (counter < hedEntries.size()) {
			HedEntry s = hedEntries.get(counter);
			if (s.getPathName().equals(path)) {
				File outfile = File.createTempFile("mgj", ".tmp");
				outfile.createNewFile();
				outfile.deleteOnExit();
				FileChannel fos = new FileOutputStream(outfile).getChannel();
				FileChannel inARK = new FileInputStream(wadFile).getChannel();
	    	    inARK.position(s.getFileStart());
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
}
