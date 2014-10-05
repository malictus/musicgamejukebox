package malictus.musicgamejukebox.player;

import malictus.musicgamejukebox.ui.*;

public class VGSPlayer extends AudioPlayer {

	boolean multiSampRate = false;
	int fastestSamprate;

	public VGSPlayer(MGJPlayer parent) {
		super(parent);
	}

	public void settrack(MGJTrack track) throws Exception {
		super.settrack(track);
		fastestSamprate = 0;
		multiSampRate = false;
		String chunkID = raf.readChunkID();
		if (!chunkID.equals("VgS!")) {
			throw new Exception("Incorrect VGS chunk ID");
		}
		raf.skipBytes(4);
		// first check to see how many valid streams there are, then reread to populate WAVStream
		boolean keepgoing = true;
		int numStreams = 0;
		while (keepgoing && (numStreams <= MGJMainWindow.MAX_STREAMS)) {
			long tempsampRate = raf.read32BitUnsignedLE();
			long numberOfBlocks = raf.read32BitUnsignedLE();
			if ((tempsampRate != 0) && (numberOfBlocks != 0)) {
				numStreams = numStreams + 1;
			} else {
				keepgoing = false;
			}
		}
		raf.seek(theTrack.filestart + 8);
		streams = new WAVStream[numStreams];
		int counter = 0;
		long firstNumBlocks = 0;
		//if file is practice mode file, change sample rate accordingly
		String noext = track.filename.substring(0, track.filename.length() - 4);
		int i = noext.lastIndexOf("_p");
		float factor = 1f;
		if (i == (noext.length() - 4)) {
			//this is a practice mode file
			String perc = noext.substring(noext.length() - 2);
			try {
				Integer x = Integer.valueOf(perc);
				if ((x < 100) && (x > 0)) {
					factor = factor * (((float)x.intValue())/100f);
				}
			} catch (NumberFormatException err) {}
		}
		while (counter < numStreams) {
			long tempsampRate = raf.read32BitUnsignedLE();
			tempsampRate = (int)(tempsampRate * factor);
			if (tempsampRate != fastestSamprate) {
				if (fastestSamprate == 0) {
					fastestSamprate = (int)tempsampRate;
				} else {
					//assumes slower sample rate files are at the end
					if (tempsampRate != (fastestSamprate / 2)) {
						throw new Exception("Invalid sample rates");
					}
					if ((counter < 4) || (counter > 5)) {
						throw new Exception("Unexpected sample rates");
					}
					multiSampRate = true;
				}
			}
			long numberOfBlocks = raf.read32BitUnsignedLE();
			if (counter == 0) {
				firstNumBlocks = numberOfBlocks - 1;
			}
			streams[counter] = new WAVStream((int)tempsampRate);
			if ((counter % 2) == 0) {
				streams[counter].setStartPan(0);
			} else {
				streams[counter].setStartPan(AudioControl.MAX_PAN);
			}
			counter = counter + 1;
		}
		if ((streams.length % 2) != 0) {
			streams[counter-1].setStartPan(AudioControl.MAX_PAN / 2);
		}
		if (multiSampRate && ((streams.length < 5) || (streams.length > 6))) {
			//wouldn't know how to parse these
			throw new Exception("Unexpected sample rates");
		}
		//calculate total song duration
		int samprate = streams[0].getSampRate();
		long numsamples = firstNumBlocks * 28;
		float dura = (float)((float)numsamples / (float)samprate);
		dura = dura * 1000;	//millis
		this.duration = (int)dura;
		//default volumes
		counter = 0;
		while (counter < streams.length) {
			streams[counter].setVolume(AudioControl.DEFAULT_VOLUME);
			counter = counter + 1;
		}
		this.curOffset = 0;
		parent.updateControls();
	}

	protected void playStreams() throws Exception {
		int counter = 0;
		while (counter < streams.length) {
			streams[counter].openLine();
			counter = counter + 1;
		}
		byte[][] blockin = new byte[streams.length][16];
		short[][] blockout = new short[streams.length][28];
		VAGDecode[] vags = new VAGDecode[streams.length];
		counter = 0;
		while (counter < streams.length) {
			vags[counter] = new VAGDecode();
			counter = counter + 1;
		}
		counter = 0;
		while (counter < streams.length) {
			streams[counter].start();
			counter = counter + 1;
		}
		float off = ((float)curOffset) / 1000f;
		off = off * this.fastestSamprate;
		off = off / 28f;
		int offs = (int)off;
		float streamlength = streams.length;
		if (multiSampRate && streams.length == 5) {
			streamlength = 4.5f;
		} else if (multiSampRate && streams.length == 6) {
			streamlength = 5f;
		}
		int amt = (int)((float)offs * 16f * streamlength);
		amt = amt - (amt % 16);
		if (amt >= 0) {
			raf.seek(theTrack.filestart + 128 + (int)amt);
		} else {
			raf.seek(theTrack.filestart + 128);
		}
		if (multiSampRate) {
			//realign to start of the appropriate block
			byte flagcheck = 0;
			while (flagcheck != (streams.length - 1)) {
				raf.skipBytes(1);
				flagcheck = raf.readByte();
				raf.skipBytes(14);
			}
			raf.skipBytes(16 * 4);
		}
		boolean readextra = false;
		while (isPlaying == true) {
			float num = -1;
			if (multiSampRate) {
				if (streams.length == 5) {
					num = (raf.getFilePointer() - theTrack.filestart - 128) / ((16 * streams.length) * (9f/10f));
				} else {
					num = (raf.getFilePointer() - theTrack.filestart - 128) / ((16 * streams.length) * (5f/6f));
				}
			} else {
				num = (raf.getFilePointer() - theTrack.filestart - 128) / (16 * streams.length);
			}
			num = num * 28f;
			num = num / streams[0].getSampRate();
			num = num * 1000f;
			curOffset = (int)num;
			counter = 0;
			if ((raf.getFilePointer() + blockin[counter].length) >= (theTrack.fileend - 1)) {
				isPlaying = false;
				int subcounter = 0;
				while (subcounter < streams.length) {
					streams[subcounter].drain();
					subcounter = subcounter + 1;
				}
				endOfStream();
			}
			if (readextra) {
				readextra = false;
			} else {
				readextra = true;
			}
			while (counter < streams.length) {
				if ((counter > 3) && multiSampRate && !readextra) {
					//multi sample rate file; this block does not appear in the file this time around
				} else  {
					try {
						raf.read(blockin[counter]);
					} catch (Exception err) {
						//can be thrown if disc is ejected
						stop();
						reset();
						isPlaying = false;
						errorReadingDisc();
					}
					try {
						blockout[counter] = vags[counter].decodeVAGBlock(blockin[counter], counter);
					} catch (Exception err) {
						//usually EOF
						isPlaying = false;
						endOfStream();
						break;
					}
					streams[counter].playData(blockout[counter]);
				}
				counter = counter + 1;
			}
			parent.updateControls();
		}
		if (isPlaying == false) {
			counter = 0;
			while (counter < streams.length) {
				streams[counter].stop();
				streams[counter].flush();
				streams[counter].close();
				counter = counter + 1;
			}
			parent.updateControls();
			//set flag to trigger next stream
			isPlayingFlag = true;
		}
		isPlaying = false;	//just in case
	}
}
