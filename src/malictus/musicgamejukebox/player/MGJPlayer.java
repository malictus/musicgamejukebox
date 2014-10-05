package malictus.musicgamejukebox.player;

import java.io.*;
import java.util.*;
import malictus.robusta.util.*;
import malictus.robusta.file.*;
import malictus.musicgamejukebox.ui.*;
import malictus.musicgamejukebox.ark.*;
import malictus.musicgamejukebox.wad.*;
import malictus.musicgamejukebox.pak.*;
import malictus.musicgamejukebox.qb.*;

/*
 * The 'master' player that directly interfaces with the UI and calls whatever specific
 * track players are necessary to play a specific track
 */
public class MGJPlayer {

	private MGJTrack[] tracks;
	private MGJTrack curTrack;
	private AudioPlayer player;
	private File rootfile;
	MGJMainWindow parent;
	private String discType = "";

	public final static String DISCTYPE_UNKNOWN = "Music Game Disc (unknown type)";
	public final static String DISCTYPE_NODISC = "No disc";
	public final static String DISCTYPE_GH1_PS2 = "Guitar Hero 1 (PS2)";
	public final static String DISCTYPE_GH2_PS2 = "Guitar Hero 2 (PS2)";
	public final static String DISCTYPE_GH80s_PS2 = "Guitar Hero Rocks The 80's (PS2)";
	public final static String DISCTYPE_KRIDOL_PS2 = "Karaoke Revolution Presents American Idol (PS2)";
	public final static String DISCTYPE_KRCOUNTRY_PS2 = "CMT Presents Karaoke Revolution Country (PS2)";
	public final static String DISCTYPE_GHAERO_PS2 = "Guitar Hero Aerosmith (PS2)";
	public final static String DISCTYPE_GH3_PS2 = "Guitar Hero 3 (PS2)";
	public final static String DISCTYPE_GHWT_PS2 = "Guitar Hero World Tour (PS2)";

	public MGJPlayer(File rootfile, MGJMainWindow parent) throws Exception {
		this.parent = parent;
		this.rootfile = rootfile;
		if (rootfile == null) {
			throw new Exception("Invalid file specified");
		}
		if (!rootfile.exists()) {
			throw new Exception("File does not exist");
		}
		if (rootfile.isFile()) {
			//VGS FILE
			if (StringUtils.getExtension(rootfile.getName()).toLowerCase().equals("vgs")) {
				MGJTrack track = new MGJTrack(MGJTrack.TRACKTYPE_VGS, rootfile, 0,
						rootfile.length(), rootfile.getName(), "", "", "");
				tracks = new MGJTrack[1];
				tracks[0] = track;
				player = new VGSPlayer(this);
				this.curTrack = track;
				player.settrack(track);
				discType = DISCTYPE_NODISC;
			//ARK FILE
			} else if (StringUtils.getExtension(rootfile.getName()).toLowerCase().equals("ark")) {
				openARK(rootfile, false);
			} else if (StringUtils.getExtension(rootfile.getName()).toLowerCase().equals("hdr")) {
				openARK(rootfile, true);
			//GH3/A files
			} else if (StringUtils.getExtension(rootfile.getName()).toLowerCase().equals("msv") ||
					StringUtils.getExtension(rootfile.getName()).toLowerCase().equals("isf")) {
				MGJTrack track = new MGJTrack(MGJTrack.TRACKTYPE_MSV, rootfile, 0,
						rootfile.length(), rootfile.getName(), "", "", "");
				tracks = new MGJTrack[1];
				tracks[0] = track;
				player = new MSVPlayer(this);
				this.curTrack = track;
				player.settrack(track);
				discType = DISCTYPE_NODISC;
			} else if (StringUtils.getExtension(rootfile.getName()).toLowerCase().equals("imf")) {
				SmartRandomAccessFile raf = new SmartRandomAccessFile(rootfile, "r");
				String ext = raf.readChunkID();
				raf.close();
				if (ext.equals("MSVp")) {
					MGJTrack track = new MGJTrack(MGJTrack.TRACKTYPE_MSV, rootfile, 0,
							rootfile.length(), rootfile.getName(), "", "", "");
					tracks = new MGJTrack[1];
					tracks[0] = track;
					player = new MSVPlayer(this);
					this.curTrack = track;
					player.settrack(track);
					discType = DISCTYPE_NODISC;
				} else if (ext.equals("494D")) {
					MGJTrack track = new MGJTrack(MGJTrack.TRACKTYPE_MSV_NEO, rootfile, 0,
							rootfile.length(), rootfile.getName(), "", "", "");
					tracks = new MGJTrack[1];
					tracks[0] = track;
					player = new MSVNeoPlayer(this);
					this.curTrack = track;
					player.settrack(track);
					discType = DISCTYPE_NODISC;
				}
			} else {
				throw new Exception("Unsupported file type");
			}
		} else {
			//FIRST, TRY GUITAR HERO 1/2/80's PS2 STRUCTURE
			String arkfile = rootfile.getPath();
			arkfile = arkfile + "GEN/MAIN_0.ARK";
			File gen = new File(arkfile);
			if (!gen.exists()) {
				arkfile = rootfile.getPath() + "/GEN/MAIN_0.ARK";
				gen = new File(arkfile);
				if (!gen.exists()) {
					arkfile = rootfile.getPath() + "/MAIN_0.ARK";
					gen = new File(arkfile);
				}
			}
			if (gen.exists()) {
				openARK(gen, false);
			} else {
				//NEXT, TRY GH3/A STRUCTURE
				String musicfolder = rootfile.getPath();
				musicfolder = musicfolder + "MUSIC/";
				File musicfolderfile = new File(musicfolder);
				if (musicfolderfile.exists()) {
					//assume it's GH3/A
					openGH3A(rootfile);
				} else {
					musicfolder = rootfile.getPath() + "/MUSIC/";
					musicfolderfile = new File(musicfolder);
					if (musicfolderfile.exists()) {
						//assume it's GH3/A
						openGH3A(rootfile);
					} else {
						throw new Exception("File structure not recognized");
					}
				}
			}
		}
	}

	public void enableStreams(AudioControl[] controls) {
		int counter = 0;
		while ((counter < getStreamCount()) && (counter < MGJMainWindow.MAX_STREAMS)) {
			controls[counter].turnOnControl();
			controls[counter].setPan(getStartPan(counter));
			if (getDiscType().equals(MGJPlayer.DISCTYPE_GH1_PS2) ||
					getDiscType().equals(MGJPlayer.DISCTYPE_GH2_PS2) ||
					getDiscType().equals(MGJPlayer.DISCTYPE_GH80s_PS2)) {
				if (getStreamCount() >= 4) {
					controls[0].setTitle("Backing L");
					controls[1].setTitle("Backing R");
					controls[2].setTitle("Guitar L");
					controls[3].setTitle("Guitar R");
				}
				if (getStreamCount() == 5) {
					controls[4].setTitle("Bass");
				}
				if (getStreamCount() == 6) {
					controls[4].setTitle("Bass/Rhy L");
					controls[5].setTitle("Bass/Rhy R");
				}
			} else if (getDiscType().equals(MGJPlayer.DISCTYPE_KRIDOL_PS2) ||
					getDiscType().equals(MGJPlayer.DISCTYPE_KRCOUNTRY_PS2)) {
				if (getStreamCount() >= 4) {
					controls[0].setTitle("Backing L");
					controls[1].setTitle("Backing R");
					controls[2].setTitle("Vocal L");
					controls[3].setTitle("Vocal R");
				}
				if (curTrack.filename.contains("_duet")) {
					controls[2].makeCenterChannel();
					controls[3].makeCenterChannel();
					controls[2].setTitle("Vocal 1");
					controls[3].setTitle("Vocal 2");
				}
			} else if (getDiscType().equals(MGJPlayer.DISCTYPE_GH3_PS2) ||
					getDiscType().equals(MGJPlayer.DISCTYPE_GHAERO_PS2)) {
				if (getStreamCount() >= 6) {
					controls[2].setTitle("Backing L");
					controls[3].setTitle("Backing R");
					controls[0].setTitle("Guitar L");
					controls[1].setTitle("Guitar R");
					controls[4].setTitle("Bass/Rhy L");
					controls[5].setTitle("Bass/Rhy R");
				}
				if (getStreamCount() == 8) {
					controls[6].setTitle("Crowd Sounds");
					controls[7].setTitle("Crowd Sounds");
				}
			} else if (getDiscType().equals(MGJPlayer.DISCTYPE_GHWT_PS2)) {
				if (getStreamCount() == 10) {
					controls[0].setTitle("Bass");
					controls[1].setTitle("Drum-Lo");
					controls[4].setTitle("Guitar L");
					controls[5].setTitle("Guitar R");
					controls[2].setTitle("Backing L");
					controls[3].setTitle("Backing R");
					controls[6].setTitle("Drum-Hi L");
					controls[7].setTitle("Drum-Hi R");
					controls[8].setTitle("Drum-Mid L");
					controls[9].setTitle("Drum-Mid R");
				}
			}
			counter = counter + 1;
		}
	}

	//also opens GHWT
	private void openGH3A(File rootfolder) throws Exception {
		String musicfolder = rootfolder.getPath() + "MUSIC/";
		String streamsfolder = rootfolder.getPath() + "STREAMS/";
		String practicefolder = rootfolder.getPath() + "PREVIEWS/";
		File music = new File(musicfolder);
		File streams = new File(streamsfolder);
		File practice = new File(practicefolder);
		if ((!music.exists()) || (!streams.exists())) {
			musicfolder = rootfolder.getPath() + "/MUSIC/";
			streamsfolder = rootfolder.getPath() + "/STREAMS/";
			music = new File(musicfolder);
			streams = new File(streamsfolder);
			if ((!music.exists()) || (!streams.exists())) {
				throw new Exception("Unrecognized disc type");
			}
		}
		if (!practice.exists()) {
			practicefolder = rootfolder.getPath() + "/PREVIEWS/";
			practice = new File(practicefolder);
		}
		File testAero = new File(music.getPath() + "/0/00A7DF47.IMF");
		File testGH3 = new File(music.getPath() + "/0/00D5E836.IMF");
		File testGHWT = new File(music.getPath() + "/0/0E6E7E09.IMF");
		if (testAero.exists()) {
			discType = MGJPlayer.DISCTYPE_GHAERO_PS2;
		} else if (testGH3.exists()) {
			discType = MGJPlayer.DISCTYPE_GH3_PS2;
		} else if (testGHWT.exists()) {
			discType = MGJPlayer.DISCTYPE_GHWT_PS2;
		} else {
			discType = MGJPlayer.DISCTYPE_UNKNOWN;
		}
		Vector<File> allMusicFiles = getAllMusicFilesUnder(music);
		Vector<File> allStreamsFiles = getAllMusicFilesUnder(streams);
		Vector<File> allPracticeFiles = getAllMusicFilesUnder(practice);
		if ((allMusicFiles.size() == 0) && (allStreamsFiles.size() == 0)) {
			throw new Exception("No music files found");
		}
		tracks = new MGJTrack[allMusicFiles.size() + allStreamsFiles.size() + allPracticeFiles.size()];
		//extract name/artist information, if possible
		String hed = rootfolder.getPath() + "DATAP.HED";
		File hedFile = new File(hed);
		if (!hedFile.exists()) {
			hed = rootfolder.getPath() + "/DATAP.HED";
			hedFile = new File(hed);
		}
		String wad = rootfolder.getPath() + "DATAP.WAD";
		File wadFile = new File(wad);
		if (!wadFile.exists()) {
			wad = rootfolder.getPath() + "/DATAP.WAD";
			wadFile = new File(wad);
		}
		Vector<QBNameEntry> entries = new Vector<QBNameEntry>();
		if (wadFile.exists() && hedFile.exists()) {
			try {
				//GHWT will currently fail here
				HedWadExtract hw = new HedWadExtract(hedFile, wadFile);
				File pakHed = hw.putInTemp("\\pak\\qb.pak.ps2");
				File pakWad = hw.putInTemp("\\pak\\qb.pab.ps2");
				PakExtract px = new PakExtract(pakHed, pakWad);
				File songfile = null;
				if (px.fileExists("cripts\\guitar\\songlist.qb.ps2")) {
					songfile = px.putInTemp("cripts\\guitar\\songlist.qb.ps2");
				} else {
					songfile = px.putInTemp("scripts\\guitar\\songlist.qb.ps2");
				}
				QBFile qb = new QBFile(songfile);
				entries = qb.getEntries();
			} catch (Exception err) {
				//GHWT fails everytime; no need for stack trace
				//err.printStackTrace();
			}
		}
		int counter = 0;
		while (counter < allMusicFiles.size()) {
			File x = allMusicFiles.get(counter);
			QBNameEntry entry = QBLookupTable.getEntryFor(x.getName(), entries);
			if (StringUtils.getExtension(x.getName()).toLowerCase().equals("isf")) {
				entry.setTitle(entry.getTitle() + " (Preview)");
			}
			//this is weird
			if (entry.getTitle().startsWith("PLACEHOLDER")) {
				entry.setTitle("Cult of Personality");
				entry.setArtist("Living Colour");
			}
			if (discType.equals(MGJPlayer.DISCTYPE_GHWT_PS2) && x.getPath().toLowerCase().endsWith("imf")) {
				tracks[counter] = new MGJTrack(MGJTrack.TRACKTYPE_MSV_NEO, x, 0,
						x.length(), x.getName(), x.getParent().substring(x.getPath().lastIndexOf("MUSIC")), entry.getTitle(), entry.getArtist());
			} else {
				tracks[counter] = new MGJTrack(MGJTrack.TRACKTYPE_MSV, x, 0,
						x.length(), x.getName(), x.getParent().substring(x.getPath().lastIndexOf("MUSIC")), entry.getTitle(), entry.getArtist());
			}
			counter = counter + 1;
		}
		int secondcounter = 0;
		while (secondcounter < allStreamsFiles.size()) {
			File x = allStreamsFiles.get(secondcounter);
			QBNameEntry entry = QBLookupTable.getEntryFor(x.getName(), entries);
			if (discType.equals(MGJPlayer.DISCTYPE_GHWT_PS2) && x.getPath().endsWith("imf")) {
				tracks[counter + secondcounter] = new MGJTrack(MGJTrack.TRACKTYPE_MSV_NEO, x, 0,
						x.length(), x.getName(), x.getParent().substring(x.getPath().lastIndexOf("STREAMS")), entry.getTitle(), entry.getArtist());
			} else {
				tracks[counter + secondcounter] = new MGJTrack(MGJTrack.TRACKTYPE_MSV, x, 0,
						x.length(), x.getName(), x.getParent().substring(x.getPath().lastIndexOf("STREAMS")), entry.getTitle(), entry.getArtist());
			}
			secondcounter = secondcounter + 1;
		}
		int thirdcounter = 0;
		while (thirdcounter < allPracticeFiles.size()) {
			File x = allPracticeFiles.get(thirdcounter);
			QBNameEntry entry = QBLookupTable.getEntryFor(x.getName(), entries);
			if (StringUtils.getExtension(x.getName()).toLowerCase().equals("isf")) {
				entry.setTitle(entry.getTitle() + " (Preview)");
			}
			if (discType.equals(MGJPlayer.DISCTYPE_GHWT_PS2) && x.getPath().endsWith("imf")) {
				tracks[counter + secondcounter + thirdcounter] = new MGJTrack(MGJTrack.TRACKTYPE_MSV_NEO, x, 0,
						x.length(), x.getName(), x.getParent().substring(x.getPath().lastIndexOf("PREVIEWS")), entry.getTitle(), entry.getArtist());
			} else {
				tracks[counter + secondcounter + thirdcounter] = new MGJTrack(MGJTrack.TRACKTYPE_MSV, x, 0,
						x.length(), x.getName(), x.getParent().substring(x.getPath().lastIndexOf("PREVIEWS")), entry.getTitle(), entry.getArtist());
			}
			thirdcounter = thirdcounter + 1;
		}
		//set track will reset this to MSV_Neo if necessary
		player = new MSVPlayer(this);
		this.curTrack = tracks[0];
		this.setTrack(tracks[0]);
	}

	private Vector<File> getAllMusicFilesUnder(File topFile) {
		if (!topFile.exists()) {
			return new Vector<File>();
		}
		int counter = 0;
		File[] kids = topFile.listFiles();
		Vector<File> ret = new Vector<File>();
		while (counter < kids.length) {
			File x = kids[counter];
			if (x.isFile()) {
				if (StringUtils.getExtension(x.getName().toLowerCase()).equals("isf") ||
						StringUtils.getExtension(x.getName().toLowerCase()).equals("imf") ||
						StringUtils.getExtension(x.getName().toLowerCase()).equals("msv") ) {
					if (x.length() > 0) {
						ret.add(x);
					}
				}
			} else {
				ret.addAll(getAllMusicFilesUnder(x));
			}
			counter = counter + 1;
		}
		return ret;
	}

	private void openARK(File thefile, boolean isHeaderFile) throws Exception {
		//check for existence of two ark files to see if RB or not
		/*
		File parent = thefile.getParentFile();
		File test = new File(parent.getPath() + "/MAIN_1.ARK");
		if (test.exists()) {
			NeoARKData arkData = new NeoArkData(thefile, isHeaderFile);
		} else {
		*/
			ARKData arkData = new ARKData(thefile, isHeaderFile);
			if (arkData.fileExists("aceofspades.mid", "songs/aceofspades")) {
	        	discType = MGJPlayer.DISCTYPE_GH1_PS2;
	        } else if (arkData.fileExists("arterialblack.mid", "songs/arterialblack")) {
	        	discType = MGJPlayer.DISCTYPE_GH2_PS2;
	        } else if (arkData.fileExists("18andlife.mid", "songs/18andlife")) {
	        	discType = MGJPlayer.DISCTYPE_GH80s_PS2;
	        } else if (arkData.fileExists("always.vgs", "songs/always")) {
	        	discType = MGJPlayer.DISCTYPE_KRIDOL_PS2;
	        } else if (arkData.fileExists("celebrity.vgs", "songs/celebrity")) {
	        	discType = MGJPlayer.DISCTYPE_KRCOUNTRY_PS2;
	        } else {
	        	discType = MGJPlayer.DISCTYPE_UNKNOWN;
	        }
			tracks = arkData.getTracks();
			player = new VGSPlayer(this);
		/*
		}
		*/
		this.curTrack = tracks[0];
		player.settrack(tracks[0]);
	}

	public String getDiscType() {
		return discType;
	}

	public void setTrack(MGJTrack track) throws Exception {
		this.curTrack = track;
		int type = this.curTrack.tracktype;
		boolean wasPlaying = false;
		if (player != null) {
			if (player.isPlaying) {
				wasPlaying = true;
			}
			player.reset();
		}
		if (type == MGJTrack.TRACKTYPE_VGS) {
			player = new VGSPlayer(this);
		} else if (type == MGJTrack.TRACKTYPE_MSV) {
			player = new MSVPlayer(this);
		} else if (type == MGJTrack.TRACKTYPE_MSV_NEO) {
			player = new MSVNeoPlayer(this);
		} else {
			throw new Exception("Unknown track type");
		}
		System.gc();
		player.settrack(track);
		if (wasPlaying) {
			player.play();
		}
	}

	public void setOffset(int millis) throws Exception {
		player.setOffset(millis);
	}

	public void updateControls() {
		parent.updateControls();
	}

	public int getOffset() {
		return player.getOffset();
	}

	public int getTotalTime() {
		return player.getTotalTime();
	}

	public void setVolume(int streamNumber, int newVolume) {
		player.setVolume(streamNumber, newVolume);
	}

	public int getVolume(int streamNumber) {
		return player.getVolume(streamNumber);
	}

	public void setPan(int streamNumber, int newPan) {
		player.setPan(streamNumber, newPan);
	}

	public int getPan(int streamNumber) {
		return player.getPan(streamNumber);
	}

	public int getStartPan(int streamNumber) {
		return player.getStartPan(streamNumber);
	}

	public File getRootfile() {
		return rootfile;
	}

	public void reset() {
		player.reset();
	}

	public void play() {
		player.play();
	}

	public void stop() {
		player.stop();
	}

	public int getTrackCount() {
		if (tracks == null) {
			return 0;
		}
		return tracks.length;
	}

	public MGJTrack[] getTracks() {
		return tracks;
	}

	public int getStreamCount() {
		return player.getStreamCount();
	}

	public void endOfStream() {
		parent.endOfStream();
	}

	public void errorReadingDisc() {
		parent.errorReadingDisc();
	}

}
