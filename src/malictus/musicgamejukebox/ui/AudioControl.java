package malictus.musicgamejukebox.ui;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.*;
import malictus.robusta.swing.*;

/**
 * This class represents a single pan/volume combination. One of these will exist
 * for every stream in the file.
 */
public class AudioControl extends JPanel {

	private SmartLabel lblName;
	private JSlider slidePan;
	private SmartLabel lblL;
	private SmartLabel lblR;
	private JSlider slideVolume;
	private SmartLabel lblVol;

	private int controlNumber;
	MGJMainWindow parent;

	public final static int MAX_VOLUME = 15;
	public final static int DEFAULT_VOLUME = 12;
	public final static int MAX_PAN = 6;

	public AudioControl(int x, int y, int controlNumber, MGJMainWindow parent) {
		super();
		this.controlNumber = controlNumber;
		this.parent = parent;
		this.setBounds(x, y, 65, 215);
		this.setLayout(null);
		lblName = new SmartLabel(5, 3, "Track " + (controlNumber+1));
		slidePan = new JSlider(JSlider.HORIZONTAL, 0, MAX_PAN, MAX_PAN/2);
		slidePan.setBounds(10, 25, 45, 30);
		slidePan.setPaintTicks(true);
		slidePan.setSnapToTicks(true);
		slidePan.setMajorTickSpacing(1);
		slidePan.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				doPanChange(source.getValue());
		    }
		});
		lblL = new SmartLabel(6, 25, "L");
		lblR = new SmartLabel(56, 25, "R");
		slideVolume = new JSlider(JSlider.VERTICAL, 0, MAX_VOLUME, DEFAULT_VOLUME);
		slideVolume.setBounds(7, 68, 50, 130);
		slideVolume.setPaintTicks(true);
		slideVolume.setSnapToTicks(true);
		slideVolume.setMajorTickSpacing(1);
		slideVolume.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider)e.getSource();
				doVolumeChange(source.getValue());
		    }
		});
		lblVol = new SmartLabel(27, 198, "Vol");
		this.setBorder(BorderFactory.createLineBorder(Color.black));
		this.add(lblName);
		this.add(lblL);
		this.add(lblR);
		this.add(slidePan);
		this.add(lblVol);
		this.add(slideVolume);
	}

	private void doVolumeChange(int newVol) {
        parent.volumeChangeTrigger(controlNumber, newVol);
	}

	private void doPanChange(int newPan) {
        parent.panChangeTrigger(controlNumber, newPan);
	}

	public int getControlNumber() {
		return controlNumber;
	}

	public void setTitle(String newTitle) {
		lblName.setText(newTitle);
	}

	public void turnOffControl() {
		resetControl();
		lblName.setVisible(false);
		slidePan.setVisible(false);
		lblL.setVisible(false);
		lblR.setVisible(false);
		slideVolume.setVisible(false);
		lblVol.setVisible(false);
		this.setBorder(null);
	}

	public void turnOnControl() {
		resetControl();
		lblName.setVisible(true);
		slidePan.setVisible(true);
		lblL.setVisible(true);
		lblR.setVisible(true);
		slideVolume.setVisible(true);
		lblVol.setVisible(true);
		this.setBorder(BorderFactory.createLineBorder(Color.black));
	}

	public void resetControl() {
		lblName.setText("Track " + (controlNumber+1));
		slideVolume.setValue(DEFAULT_VOLUME);
		slidePan.setValue(slidePan.getMaximum() / 2);
	}

	public void makeLeftChannel() {
		slidePan.setValue(0);
	}

	public void makeRightChannel() {
		slidePan.setValue(slidePan.getMaximum());
	}

	public void makeCenterChannel() {
		slidePan.setValue(slidePan.getMaximum() / 2);
	}

	public void setPan(int newValue) {
		slidePan.setValue(newValue);
	}

	public int getPan() {
		return slidePan.getValue();
	}

	public void setVolume(int newValue) {
		slideVolume.setValue(newValue);
	}

	public int getVolume() {
		return slideVolume.getValue();
	}

}
