package malictus.musicgamejukebox.player;

import javax.sound.sampled.*;
import malictus.musicgamejukebox.ui.*;

/**
 * This class does the actual work of playing the converted PCM stream data. All players
 * will use this class to do the actual output of sound. For now 16-bit is always assumed
 */
public class WAVStream {

	private int sampRate;
	private SourceDataLine	lineOut;
	private int pan;
	private int volume;
	AudioFormat theFormat;
	int startPan = 0;

	public WAVStream(int sampRate) throws Exception {
		this.sampRate = sampRate;
		theFormat = new AudioFormat(sampRate, 16, 2, true, false);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, theFormat);
		lineOut = (SourceDataLine)AudioSystem.getLine(info);
	}

	public void playData(short[] wavData) throws Exception {
		byte[] finalout = new byte[wavData.length * 2 * 2];
		for (int jj = 0; jj < wavData.length; jj++) {
			short candidate;
			candidate = wavData[jj];
			float candidateRFloat = candidate * ((float)volume / (float)AudioControl.MAX_VOLUME);
			candidateRFloat = candidateRFloat * ((float)pan / (float)AudioControl.MAX_PAN);
			float candidateLFloat = candidate * ((float)volume / (float)AudioControl.MAX_VOLUME);
			candidateLFloat = candidateLFloat * ((AudioControl.MAX_PAN - (float)pan) / (float)AudioControl.MAX_PAN);
			short candidateL = (short)candidateLFloat;
			short candidateR = (short)candidateRFloat;
			finalout[(jj * 4)] = (byte)(candidateL & 0xff);
			finalout[(jj * 4) + 1] = (byte)((candidateL >>> 8) & 0xff);
			finalout[(jj * 4) + 2] = (byte)(candidateR & 0xff);
			finalout[(jj * 4) + 3] = (byte)((candidateR >>> 8) & 0xff);
		}
		lineOut.write(finalout, 0, finalout.length);
	}

	public void setStartPan(int newStartPan) {
		startPan = newStartPan;
		this.pan = startPan;
	}

	public int getStartPan() {
		return startPan;
	}

	public void openLine() throws Exception {
		lineOut.open(theFormat);
	}

	public void start() {
		lineOut.start();
	}

	public void flush() {
		if (lineOut != null) {
			lineOut.flush();
		}
	}

	public void drain() {
		if (lineOut != null) {
			lineOut.drain();
		}
	}

	public void close() {
		if (lineOut != null) {
			lineOut.close();
		}
	}

	public void stop() {
		if (lineOut != null) {
			lineOut.stop();
		}
	}

	public int getPan() {
		return pan;
	}

	public void setPan(int newPan) {
		pan = newPan;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int newVol) {
		volume = newVol;
	}

	public int getSampRate() {
		return sampRate;
	}

	public void remove() {
		if (lineOut != null) {
			lineOut.stop();
			lineOut.drain();
			lineOut.close();
			lineOut = null;
		}
	}

}
