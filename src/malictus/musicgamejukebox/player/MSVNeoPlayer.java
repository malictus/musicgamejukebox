package malictus.musicgamejukebox.player;

import malictus.musicgamejukebox.ui.*;

public class MSVNeoPlayer extends AudioPlayer {

	int fastestsamprate;
	int largeststreamsize;

	public MSVNeoPlayer(MGJPlayer parent) {
		super(parent);
	}

	public void settrack(MGJTrack track) throws Exception {
		super.settrack(track);
		String chunkID = raf.readChunkID();
		if (!chunkID.equals("494D")) {
			throw new Exception("Incorrect chunk info");
		}
		raf.skipBytes(12);
		long pos = raf.getFilePointer();
		//now start reading stream infos to populate our stream data; read twice, first time just to get total number of channels
		int counter = 0;
		while (true) {
			if (counter > MGJMainWindow.MAX_STREAMS) {
				throw new Exception("Too many streams");
			}
			int test = (int)raf.read32BitUnsignedLE();
			if (test == 0) {
				//reached end of stream info
				break;
			}
			raf.skipBytes(20);
			byte numchannels = raf.readByte();
			raf.skipBytes(3);
			counter = counter + numchannels;
		}
		if (counter == 0) {
			throw new Exception("No data");
		}
		raf.seek(pos);
		streams = new WAVStream[counter];
		counter = 0;
		//now populate streams vec
		while (counter < streams.length) {
			raf.skipBytes(4);
			//first block offset - don't need
			raf.skipBytes(4);
			int channelblocksize = (int)raf.read32BitUnsignedLE();
			if ((channelblocksize != 65536) && (channelblocksize != 32768)) {
				throw new Exception("Unexpected channel block size");
			}
			int streamsize = (int)raf.read32BitUnsignedLE();
			if (streamsize > largeststreamsize) {
				largeststreamsize = streamsize;
			}
			int samprate = (int)raf.read32BitUnsignedLE();
			if (samprate >= fastestsamprate) {
				fastestsamprate = samprate;
			}
			float unk = Float.intBitsToFloat((int)raf.read32BitUnsignedLE());
			byte numchannels = raf.readByte();
			if (numchannels == 1) {
				streams[counter] = new WAVStream(samprate);
				streams[counter].setStartPan(AudioControl.MAX_PAN / 2);
			} else if (numchannels == 2) {
				streams[counter] = new WAVStream(samprate);
				streams[counter].setStartPan(0);
				streams[counter + 1] = new WAVStream(samprate);
				streams[counter + 1].setStartPan(AudioControl.MAX_PAN);
			}
			raf.skipBytes(3);
			counter = counter + numchannels;
		}
		//calculate overall song duration
		float numblocks = ((float)largeststreamsize - 65536f) / 16f;
		float numsamples = numblocks * 28f;
		float dura = (float)((float)numsamples / (float)fastestsamprate);
		dura = dura * 1000;	//millis
		this.duration = (int)dura;
		counter = 0;
		while (counter < streams.length) {
			streams[counter].setVolume(AudioControl.DEFAULT_VOLUME);
			counter = counter + 1;
		}
		this.curOffset = 0;
		parent.updateControls();
	}

	protected void playStreams() throws Exception {
		int tot = 0;
		int counter = 0;
		while (counter < streams.length) {
			if (streams[counter].getSampRate() == fastestsamprate) {
				tot = tot + 65536;
			} else {
				tot = tot + 32768;
			}
			counter = counter + 1;
		}
		byte[] bigblockin = new byte[tot];
		byte[][] blockin = new byte[streams.length][16];
		short[][] blockout = new short[streams.length][28];
		//INITIALIZE VAG BLOCKS AND WAV LINES
		VAGDecode[] vags = new VAGDecode[streams.length];
		counter = 0;
		while (counter < streams.length) {
			vags[counter] = new VAGDecode();
			streams[counter].openLine();
			streams[counter].start();
			counter = counter + 1;
		}
		//FIGURE OUT WHERE TO START READING IN FILE
		float off = ((float)curOffset / (float)duration);
		int nowbytes = (int)(largeststreamsize * off);
		int numblocks = nowbytes / 65536;
		if (numblocks > 0) {
			numblocks = numblocks - 1;
		}
		int extra = nowbytes % 65536;
		extra = extra - (extra % 32);
		int[] points = new int[streams.length];
		raf.seek(theTrack.filestart + 65536 + (numblocks * tot));
		//go ahead and read in the first chunk here
		try {
			raf.read(bigblockin);
			int keepgoing = 0;
			counter = 0;
			while (counter < points.length) {
				points[counter] = keepgoing;
				if (streams[counter].getSampRate() == fastestsamprate) {
					keepgoing = keepgoing + 65536;
				} else {
					keepgoing = keepgoing + 32768;
				}
				counter = counter + 1;
			}
			counter = 0;
			while (counter < points.length) {
				if (streams[counter].getSampRate() == fastestsamprate) {
					points[counter] = points[counter] + extra;
				} else {
					points[counter] = points[counter] + (extra/2);
				}
				counter = counter + 1;
			}
		} catch (Exception err) {
			//can be thrown if disc is ejected
			stop();
			reset();
			isPlaying = false;
			errorReadingDisc();
		}
		boolean justStarted = false;
		boolean thisTime = false;
		long numfullblocks;
		float num;
		//PLAY LOOP
		while (isPlaying == true) {
			thisTime = !thisTime;
			off = ((float)curOffset / (float)duration);
			if (off >= 1) {
				isPlaying = false;
				int zz = 0;
				while (zz < streams.length) {
					streams[zz].drain();
					zz = zz + 1;
				}
				endOfStream();
				curOffset = 0;
				break;
			}
			if (justStarted) {
				//time to read the next chunk
				try {
					raf.read(bigblockin);
					int keepgoing = 0;
					counter = 0;
					while (counter < points.length) {
						points[counter] = keepgoing;
						if (streams[counter].getSampRate() == fastestsamprate) {
							keepgoing = keepgoing + 65536;
						} else {
							keepgoing = keepgoing + 32768;
						}
						counter = counter + 1;
					}
				} catch (Exception err) {
					//can be thrown if disc is ejected
					stop();
					reset();
					isPlaying = false;
					errorReadingDisc();
				}
			}
			justStarted = false;
			//calculate current offset
			numfullblocks = ((raf.getFilePointer() - theTrack.filestart) - tot)/tot;
			num = -1;
			if (streams[0].getSampRate() == fastestsamprate) {
				num = (numfullblocks * ((65536) / 16)) + (points[0]/16);
			} else {
				num = (numfullblocks * ((65536) / 16)) + (points[0]/8);
			}
			num = num * 28f;
			num = num / fastestsamprate;
			num = num * 1000f;
			curOffset = (int)num;
			counter = 0;
			//read 16 bytes from each stream
			while (counter < streams.length) {
				if ((streams[counter].getSampRate() == fastestsamprate) || (thisTime)) {
					System.arraycopy(bigblockin, points[counter], blockin[counter], 0, 16);
					try {
						blockout[counter] = vags[counter].decodeVAGBlock(blockin[counter], 0);
					} catch (Exception err) {
						//EOF?
						isPlaying = false;
						endOfStream();
						break;
					}
					streams[counter].playData(blockout[counter]);
					points[counter] = points[counter] + 16;
				}
				if (points[counter] + 16 >= bigblockin.length) {
					justStarted = true;
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