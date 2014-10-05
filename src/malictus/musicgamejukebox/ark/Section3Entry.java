package malictus.musicgamejukebox.ark;

/**
 * Section3Entry
 * An object that represents a single entry in the Section 3 table of a HDR file.
 */
public class Section3Entry {

	private long fileOffset;
	private long fileID;
	private long directoryID;
	private long fileSize;
	private long offsetIntoHDR;

	public Section3Entry(long fileoffset, long fileid, long directoryid, long filesize, long offsetintohdr) {
		fileOffset = fileoffset;
		fileID = fileid;
		directoryID = directoryid;
		fileSize = filesize;
		offsetIntoHDR = offsetintohdr;
	}

	public long getFileOffset() {
		return fileOffset;
	}

	public void setFileOffset(long newOffset) {
		fileOffset = newOffset;
	}

	public long getFileID() {
		return fileID;
	}

	public long getDirectoryID() {
		return directoryID;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long newFileSize) {
		fileSize = newFileSize;
	}

	public long getOffsetIntoHDR() {
		return offsetIntoHDR;
	}

}