package malictus.musicgamejukebox.player;

import java.io.*;

/**
 * A MGJTrack object stores information about a track.
 *
 * @author Jim Halliday
 */
public class MGJTrack implements Comparable<MGJTrack> {

	public int tracktype = -1;
	public File parentfile = null;
	public long filestart = -1;
	public long fileend = -1;
	public String filename = "";
	public String filenamepath = "";
	public String trackname = "";
	public String trackartist = "";

	public static final int TRACKTYPE_VGS = 1;
	public static final int TRACKTYPE_MSV = 2;
	public static final int TRACKTYPE_MSV_NEO = 3;

	/**
	 *Initialize a MGJTrack object
	 *
	 * @param tracktype The audio file type; one of the tracktype constants.
	 * @param parentfile The parentfile that contains this track;
	 * this points to the file itself for single track players.
	 * @param filestart The position in the file where this track starts - zero for single
	 * track players.
	 * @param fileend The end of the track within the parent file - end of file for single track
	 * players.
	 * @param filename name of individual song file; may be same as parentfile if single file
	 * @param filenamepath The full path of the track file (not including filename itself).
	 * 			This may represent either the directory within a parent file,
	 * 			or the directory structure within an archive.
	 * 			Empty string if reading single file.
	 * @param trackname The track name, if known (empty string if no name given).
	 * @param trackartist The track artist, if known (empty string if no name given).
	 */
	public MGJTrack(int tracktype, File parentfile, long filestart, long fileend,
			String filename, String filenamepath, String trackname, String trackartist) {
		this.tracktype = tracktype;
		this.parentfile = parentfile;
		this.filestart = filestart;
		this.fileend = fileend;
		this.filename = filename;
		this.filenamepath = filenamepath;
		this.trackname = trackname;
		this.trackartist = trackartist;
	}

	/**
	 * Default sort; tracknames, or filename/path if no trackname exists
	 */
	public int compareTo(MGJTrack otherOne) {
		if ((trackname != null) && (otherOne.trackname != null)) {
			if ((!trackname.equals("")) && (!otherOne.equals(""))) {
				return trackname.compareTo(otherOne.trackname);
			}
		}
		String thisone = filenamepath + "/" + filename;
		String other = otherOne.filenamepath + "/" + otherOne.filename;
		return thisone.compareTo(other);
	}

}
