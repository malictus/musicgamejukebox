package malictus.musicgamejukebox.pak;

public class PakEntry {
	
	private long fileStart;
	private long fileSize;
	private String pathName;
	
	public PakEntry(long fileStart, long fileSize, String pathName) {
		this.fileSize = fileSize;
		this.fileStart = fileStart;
		this.pathName = pathName;
	}
	
	public long getFileStart() {
		return fileStart;
	}
	
	public long getFileSize() {
		return fileSize;
	}
	
	public String getPathName() {
		return pathName;
	}
	
}
