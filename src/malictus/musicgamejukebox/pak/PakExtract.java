package malictus.musicgamejukebox.pak;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import malictus.robusta.file.*;

public class PakExtract {

	private File pakHed;
	private File pakWad;
	Vector<PakEntry> pakEntries;

	public PakExtract(File pakHed, File pakWad) throws Exception {
		this.pakWad = pakWad;
		this.pakHed = pakHed;
		pakEntries = new Vector<PakEntry>();
		if (!pakWad.exists() || (!pakHed.exists())) {
			throw new Exception("Pak file does not exist");
		}
		SmartRandomAccessFile raf = new SmartRandomAccessFile(pakHed, "r");
		long tot = raf.length();
		while (raf.getFilePointer() < (raf.length() - 1)) {
			long start = raf.getFilePointer();
			raf.skipBytes(4);
			long offset = raf.read32BitUnsignedLE() + start - tot;
			long filesize = raf.read32BitUnsignedLE();
			if ((filesize % 16) != 0) {
				filesize = filesize + (16-(filesize%16));
			}
			long check = raf.read32BitUnsignedLE();
			raf.skipBytes(12);
			long flags = raf.read32BitUnsignedLE();
			if (flags == 32) {
				long pos = raf.getFilePointer();
				String name = raf.readNullTerminatedString();
				PakEntry x = new PakEntry(offset, filesize, name);
				pakEntries.add(x);
				raf.seek(pos+160);
			}
			if (check == 0) {
				break;
			}
		}
		raf.close();
	}

	public File getPakHed() {
		return pakHed;
	}

	public File getPakWad() {
		return pakWad;
	}

	public boolean fileExists(String path) throws Exception {
		int counter = 0;
		while (counter < pakEntries.size()) {
			PakEntry s = pakEntries.get(counter);
			if (s.getPathName().equals(path)) {
				return true;
			}
			counter = counter + 1;
		}
		return false;
	}

	public File putInTemp(String path) throws Exception {
		int counter = 0;
		while (counter < pakEntries.size()) {
			PakEntry s = pakEntries.get(counter);
			if (s.getPathName().equals(path)) {
				File outfile = File.createTempFile("mgj", ".tmp");
				outfile.createNewFile();
				outfile.deleteOnExit();
				FileChannel fos = new FileOutputStream(outfile).getChannel();
				FileChannel inARK = new FileInputStream(pakWad).getChannel();
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
