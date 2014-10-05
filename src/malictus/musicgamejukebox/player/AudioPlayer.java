package malictus.musicgamejukebox.player;

import malictus.robusta.file.*;

/**
 * A simple audio player that does nothing on its own, but can be extended to play various
 * kinds of files
 */
public class AudioPlayer {

	MGJPlayer parent;
	boolean isPlaying = false;
	int curOffset = 0;
	WAVStream[] streams;
	boolean isPlayingFlag = false;
	int duration = 0;
	SmartRandomAccessFile raf;
	MGJTrack theTrack;

	public AudioPlayer(MGJPlayer parent) {
		this.parent = parent;
	}

	/**
	 * Must be overwritten to make it work (but call super)
	 */
	public void settrack(MGJTrack track) throws Exception {
		curOffset = 0;
		duration = 0;
		reset();
		theTrack = track;
		raf = new SmartRandomAccessFile(theTrack.parentfile, "r");
		raf.seek(theTrack.filestart);
	}

	/**
	 * Must be overwritten to make it work
	 */
	protected void playStreams() throws Exception {

	}

	public void setOffset(int newOffset) {
		boolean wasPlaying = false;
		if (isPlaying) {
			stop();
			wasPlaying = true;
		}
		curOffset = newOffset;
		if (wasPlaying) {
			play();
		}
	}

	public int getOffset() {
		return curOffset;
	}

	public int getStreamCount() {
		return streams.length;
	}

	public void play() {
		System.gc();
		Runnable q = new Runnable() {
            public void run() {
            	try {
            		playStreams();
            	} catch (Exception err) {
            		err.printStackTrace();
            	}
            }
        };
        Thread t = new Thread(q);
        t.start();
		isPlaying = true;
	}

	public void stop() {
		if (isPlaying) {
			isPlaying = false;
			while (isPlayingFlag == false) {
				//wait for thread to finish
				try {
					Thread.sleep(20);
				} catch (Exception err) {}
			}
			isPlayingFlag = false;
		}
	}

	public void reset() {
		stop();
		int counter = 0;
		if (streams != null) {
			while (counter < streams.length) {
				streams[counter].remove();
				counter = counter + 1;
			}
		}
		try {
			if (raf != null) {
				raf.close();
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
	}

	public int getTotalTime() {
		return duration;
	}

	public int getPan(int streamNumber) {
		return streams[streamNumber].getPan();
	}

	public int getVolume(int streamNumber) {
		return streams[streamNumber].getVolume();
	}

	public void setPan(int streamNumber, int newPan) {
		if (streams.length > streamNumber) {
			streams[streamNumber].setPan(newPan);
		}
	}

	public void setVolume(int streamNumber, int newVol) {
		if (streams.length > streamNumber) {
			streams[streamNumber].setVolume(newVol);
		}
	}

	public int getStartPan(int streamNumber) {
		return streams[streamNumber].startPan;
	}

	public void endOfStream() {
		parent.endOfStream();
	}

	public void errorReadingDisc() {
		parent.errorReadingDisc();
	}

}
