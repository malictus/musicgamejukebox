package malictus.musicgamejukebox.player;

import malictus.musicgamejukebox.ui.*;

public class MSVPlayer extends AudioPlayer {

	int samprate;
	int datalength;

	public MSVPlayer(MGJPlayer parent) {
		super(parent);
	}

	public void settrack(MGJTrack track) throws Exception {
		super.settrack(track);
		//calculate info about total number of streams and info for each one
		//first get data for very first stream
		String chunkID = raf.readChunkID();
		if (!chunkID.equals("MSVp")) {
			//not even a single stream; wrong
			throw new Exception("Incorrect chunk info");
		}
		raf.skipBytes(8);
		datalength = raf.readInt();
		samprate = raf.readInt();
		raf.skipBytes((128 * 1024) - 20);
		int numStreams = 1;
		while (true) {
			chunkID = raf.readChunkID();
			if (!chunkID.equals("MSVp")) {
				//no more new streams
				break;
			}
			//assume blocksize, and skip it
			raf.skipBytes(8);
			//verify data size
			int x = raf.readInt();
			if (x < datalength) {
				//sometimes lengths aren't the same
				datalength = x;
			}
			//verify sample rate
			if (raf.readInt() != samprate) {
				throw new Exception("Sample rates are not the same");
			}
			//skip to next stream
			raf.skipBytes((128 * 1024) - 20);
			numStreams = numStreams + 1;
		}
		if (numStreams > MGJMainWindow.MAX_STREAMS) {
			throw new Exception("Too many streams: " + numStreams);
		}
		raf.seek(theTrack.filestart);
		streams = new WAVStream[numStreams];
		int counter = 0;
		while (counter < numStreams) {
			streams[counter] = new WAVStream(samprate);
			if ((counter % 2) == 0) {
				streams[counter].setStartPan(0);
			} else {
				streams[counter].setStartPan(AudioControl.MAX_PAN);
			}
			counter = counter + 1;
		}
		//set last stream to mono if odd number of streams
		if ((streams.length % 2) != 0) {
			streams[counter-1].setStartPan(AudioControl.MAX_PAN / 2);
		}
		float numblocks = ((float)datalength) / 16f;
		float numsamples = numblocks * 28f;
		float dura = (float)((float)numsamples / (float)samprate);
		dura = dura * 1000;	//millis
		this.duration = (int)dura;
		//default volume
		counter = 0;
		while (counter < streams.length) {
			streams[counter].setVolume(AudioControl.DEFAULT_VOLUME);
			counter = counter + 1;
		}
		this.curOffset = 0;
		parent.updateControls();
	}

	protected void playStreams() throws Exception {
		byte[] bigblockin = new byte[streams.length*1024*128];
		byte[][] blockin = new byte[streams.length][16];
		short[][] blockout = new short[streams.length][28];
		//INITIALIZE VAG BLOCKS AND WAV LINES
		VAGDecode[] vags = new VAGDecode[streams.length];
		int counter = 0;
		while (counter < streams.length) {
			vags[counter] = new VAGDecode();
			streams[counter].openLine();
			streams[counter].start();
			counter = counter + 1;
		}
		//FIGURE OUT WHERE TO START READING FILE
		float off = ((float)curOffset / (float)duration);
		int nowbytes = (int)(datalength * off);
		int extra = nowbytes % (1024*128);
		extra = extra - (extra % 16);
		nowbytes = nowbytes - (nowbytes % (1024*128));
		boolean justStarted = true;
		//seek to beginning of a chunk of data to start reading
		raf.seek(theTrack.filestart + (nowbytes*streams.length));
		long z;
		long numfullblocks;
		long leftover;
		float num;
		//STEP 4 - PLAY LOOP
		while (isPlaying == true) {
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
			if ((extra == 0) || justStarted ) {
				//time to read the next chunk
				try {
					raf.read(bigblockin);
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
			z = (raf.getFilePointer() - theTrack.filestart) - bigblockin.length;
			z = z + extra;
			numfullblocks = z / (1024*128*streams.length);
			leftover = (z % (1024*128*streams.length))/16;
			num = -1;
			num = (numfullblocks * ((1024*128) / 16)) + leftover;
			num = num * 28f;
			num = num / samprate;
			num = num * 1000f;
			curOffset = (int)num;
			int nowpos = extra;
			counter = 0;
			if ((bigblockin[0] == 'M') && (bigblockin[1] == 'S') && (bigblockin[2] == 'V') && (bigblockin[3] == 'p')) {
				if (extra < 64) {
					extra = 64;
				}
			}
			//read 16 bytes from each stream
			while (counter < streams.length) {
				System.arraycopy(bigblockin, extra, blockin[counter], 0, 16);
				try {
					blockout[counter] = vags[counter].decodeVAGBlock(blockin[counter], 0);
				} catch (Exception err) {
					//shouldn't happen, but may be EOF
					isPlaying = false;
					endOfStream();
					break;
				}
				streams[counter].playData(blockout[counter]);
				//test to see where we are in stream (ugh)
				if (counter < (streams.length - 1)) {
					extra = extra + (1024*128);
				} else {
					extra = extra - (streams.length*1024*128) + 16;
					if (!(extra == 0)) {
						extra = nowpos + 16;
					}
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
