package malictus.musicgamejukebox.ui;

import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import malictus.robusta.swing.*;
import malictus.robusta.util.*;
import malictus.musicgamejukebox.player.*;

/**
 * MGJ Main Window
 *
 * @author Jim Halliday
 */
public class MGJMainWindow extends SmartWindow {

	private JMenuBar menubar = null;
	private JMenu menuFile = null;
	private JMenuItem menuiOpenDisc = null;
	private JMenuItem menuiOpenFileFolder = null;
	private JMenuItem menuiClose = null;
	private JMenuItem menuiAbout = null;
	private JMenuItem menuiQuit = null;
	private SmartLabel lblCurOpen = null;
	private SmartLabel lblCurOpen2 = null;
	private SmartButton btnPlay = null;
	private JSlider slideDuration = null;
	private SmartLabel lblDuration = null;
	private AudioControl[] controls = null;

	private JTabbedPane jTabbedPane = null;
	private JPanel pnlRegTrackList = null;
	private SmartList regTrackList = null;
	private Vector<MGJTrack> regTracks = new Vector<MGJTrack>();
	private JPanel pnlPracTrackList = null;
	private SmartList pracTrackList = null;
	private Vector<MGJTrack> pracTracks = new Vector<MGJTrack>();
	private JPanel pnlTrackList = null;
	private SmartList trackList = null;
	private Vector<MGJTrack> tracks = new Vector<MGJTrack>();

	private SmartFileChooser sfc = new SmartFileChooser("msv, imf, isf, vgs, ark, hdr", "Music Game Disc, Folder, or File", false);
	private MGJPlayer player = null;
	public boolean isPlaying = false;
	public boolean playFlag = false;

	public final static String VERSION_NUMBER = "0.07";
	public final static int MAX_STREAMS = 10;

	/**
	 * The MGJMainWindow application's main method.
	 *
	 * @param args not currently used
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception err) {
			JOptionPane.showMessageDialog(null, "Error setting look and feel.");
		}
		new MGJMainWindow();
	}

	/**
	 * Constructor for MGJMainWindow
	 */
	public MGJMainWindow() {
		super(730, 575, true, false, true, "Music Game Jukebox " + VERSION_NUMBER, true);
		buildMenus();
		sfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		lblCurOpen = new SmartLabel(10, 8, "Currently Playing:");
    	contentPane.add(lblCurOpen);
    	lblCurOpen2 = new SmartLabel(lblCurOpen.getWidth() + 13, 8, "[none]");
    	contentPane.add(lblCurOpen2);
		lblCurOpen2.setEnabled(false);
		controls = new AudioControl[MAX_STREAMS];
		int counter = 0;
		while (counter < MAX_STREAMS) {
			controls[counter] = new AudioControl(10+(counter*68), 30, counter, this);
			contentPane.add(controls[counter]);
			counter = counter + 1;
		}
		lblDuration = new SmartLabel(10, controls[0].getY() + controls[0].getHeight() + 8, "0:00 / 0:00");
    	contentPane.add(lblDuration);
    	btnPlay = new SmartButton(85, lblDuration.getY(), "Play");
    	contentPane.add(btnPlay);
		btnPlay.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (isPlaying) {
					stop();
				} else {
					play();
				}
			}
		});
		slideDuration = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
		slideDuration.setBounds(140, btnPlay.getY() - 4, 380, 30);
		slideDuration.addMouseListener(new MouseListener() {
			public void mousePressed(MouseEvent e) {
		       if (isPlaying) {
		    	   playFlag = true;
		    	   stop();
		       }
		    }
		    public void mouseReleased(MouseEvent e) {
		    	try {
		    		player.setOffset( ((JSlider)e.getSource()).getValue() * 10);
		    		updateControls();
		    	} catch (Exception err) {
		    		err.printStackTrace();
		    	}
		    	if (playFlag) {
		    		play();
		       }
		       playFlag = false;
		    }
		    public void mouseExited(MouseEvent e) {
			}
		    public void mouseEntered(MouseEvent e) {
			}
		    public void mouseClicked(MouseEvent e) {
			}
		});
		contentPane.add(slideDuration);
		jTabbedPane = new JTabbedPane();
		jTabbedPane.setBounds(new java.awt.Rectangle(5, 290, 610, 230));
		pnlRegTrackList = new JPanel();
		pnlRegTrackList.setLayout(null);
		pnlRegTrackList.setBounds(new java.awt.Rectangle(0, 0, 590, 210));
		jTabbedPane.addTab("Regular Tracks", null, pnlRegTrackList, null);
		pnlPracTrackList = new JPanel();
		pnlPracTrackList.setLayout(null);
		pnlPracTrackList.setBounds(new java.awt.Rectangle(0, 0, 590, 210));
		jTabbedPane.addTab("Practice/Preview Tracks", null, pnlPracTrackList, null);
		pnlTrackList = new JPanel();
		pnlTrackList.setLayout(null);
		pnlTrackList.setBounds(new java.awt.Rectangle(0, 0, 590, 210));
		jTabbedPane.addTab("Other Tracks", null, pnlTrackList, null);
		contentPane.add(jTabbedPane);

		regTrackList = new SmartList(new java.awt.Rectangle(2, 3, 600, 200), null);
		regTrackList.getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		regTrackList.getList().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent evt) {
		        if (evt.getValueIsAdjusting()) {
		        	return;
		        }
		        try {
		        	enablePlayControls();
		        	doRegTrackListChange();
		        } catch (Exception err) {
		        	stop();
		        	disablePlayControls();
		        }
		    }
		});
		pnlRegTrackList.add(regTrackList);

		pracTrackList = new SmartList(new java.awt.Rectangle(2, 3, 600, 200), null);
		pracTrackList.getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pracTrackList.getList().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent evt) {
		        if (evt.getValueIsAdjusting()) {
		        	return;
		        }
		        try {
		        	enablePlayControls();
		        	doPracTrackListChange();
		        } catch (Exception err) {
		        	stop();
		        	disablePlayControls();
		        }
		    }
		});
		pnlPracTrackList.add(pracTrackList);

		trackList = new SmartList(new java.awt.Rectangle(2, 3, 600, 200), null);
		trackList.getList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		trackList.getList().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent evt) {
		        if (evt.getValueIsAdjusting()) {
		        	return;
		        }
		        try {
		        	enablePlayControls();
		        	doTrackListChange();
		        } catch (Exception err) {
		        	stop();
		        	disablePlayControls();
		        }
		    }
		});
		pnlTrackList.add(trackList);

		disableAllStreams();
		disablePlayControls();
		disableTrackNav();
	}

	private void buildMenus() {
		//build menus
		menuiQuit = new JMenuItem("Quit", KeyEvent.VK_Q);
		menuiQuit.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (canClose()) {
					System.exit(0);
				}
			}
		});
		menuiAbout = new JMenuItem("About", KeyEvent.VK_A);
		menuiAbout.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				JOptionPane.showMessageDialog(null, "Music Game Jukebox " + VERSION_NUMBER);
			}
		});
		menuiClose = new JMenuItem("Close", KeyEvent.VK_W);
		menuiClose.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				closeAudio();
			}
		});
		menuiClose.setEnabled(false);
		menuiOpenFileFolder = new JMenuItem("Open a File or Folder", KeyEvent.VK_O);
		menuiOpenFileFolder.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				try {
					openFileFolder();
				} catch (Exception err) {
					err.printStackTrace();
					closeAudio();
				}
			}
		});
		menuiOpenDisc = new JMenuItem("Open a Disc", KeyEvent.VK_D);
		menuiOpenDisc.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				try {
					openDisc();
				} catch (Exception err) {
					err.printStackTrace();
					closeAudio();
				}
			}
		});
		menuFile = new JMenu("File");
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuFile.add(menuiOpenDisc);
		menuFile.add(menuiOpenFileFolder);
		menuFile.add(menuiClose);
		menuFile.addSeparator();
		menuFile.add(menuiAbout);
		menuFile.add(menuiQuit);
		menubar = new JMenuBar();
		menubar.add(menuFile);
		this.setJMenuBar(menubar);
	}

	private void play() {
		if (player != null) {
			player.play();
			btnPlay.setText("Stop");
			isPlaying = true;
		}
	}

	private void stop() {
		if (player != null) {
			player.stop();
		}
		if (!playFlag) {
			btnPlay.setText("Play");
		}
		isPlaying = false;
	}

	protected void panChangeTrigger(int controlNumber, int newValue) {
		if ((player != null) && (player.getStreamCount() >= controlNumber)) {
			player.setPan(controlNumber, newValue);
		}
	}

	protected void volumeChangeTrigger(int controlNumber, int newValue) {
		if ((player != null) && (player.getStreamCount() >= controlNumber)) {
			player.setVolume(controlNumber, newValue);
		}
	}

	private void closeAudio() {
		stop();
		if (player != null) {
			player.reset();
			player = null;
		}
		menuiClose.setEnabled(false);
		this.lblCurOpen2.setEnabled(false);
		this.lblCurOpen2.setText("[none]");
		disableAllStreams();
		disableTrackNav();
		disablePlayControls();
	}

	private void disableAllStreams() {
		int counter = 0;
		while (counter < controls.length) {
			controls[counter].turnOffControl();
			counter = counter + 1;
		}
	}

	private void enableStreams() {
		if (player == null) {
			return;
		}
		player.enableStreams(controls);
	}

	private void disableTrackNav() {
		DefaultListModel lm = new DefaultListModel();
		lm.clear();
		lm.removeAllElements();
		regTrackList.getList().setModel(lm);
		regTrackList.setEnabled(false);
		regTrackList.getList().setEnabled(false);
		regTrackList.setVisible(false);
		regTracks.clear();
		pracTrackList.getList().setModel(lm);
		pracTrackList.setEnabled(false);
		pracTrackList.getList().setEnabled(false);
		pracTrackList.setVisible(false);
		pracTracks.clear();
		trackList.getList().setModel(lm);
		trackList.setEnabled(false);
		trackList.getList().setEnabled(false);
		trackList.setVisible(false);
		tracks.clear();
		jTabbedPane.setVisible(false);

	}

	private void doRegTrackListChange() throws Exception {
		if (regTracks != null && player != null) {
			if (regTrackList.getList().getSelectedIndex() != -1) {
				int newOne = regTrackList.getList().getSelectedIndex();
				disableAllStreams();
				player.setTrack(regTracks.get(newOne));
				this.pracTrackList.getList().clearSelection();
				this.trackList.getList().clearSelection();
				enableStreams();
				this.slideDuration.setMaximum(player.getTotalTime() / 10);
			}
		}
	}

	private void doPracTrackListChange() throws Exception {
		if (pracTracks != null && player != null) {
			if (pracTrackList.getList().getSelectedIndex() != -1) {
				int newOne = pracTrackList.getList().getSelectedIndex();
				disableAllStreams();
				this.regTrackList.getList().clearSelection();
				this.trackList.getList().clearSelection();
				player.setTrack(pracTracks.get(newOne));
				enableStreams();
				this.slideDuration.setMaximum(player.getTotalTime() / 10);
			}
		}
	}

	private void doTrackListChange() throws Exception {
		if (tracks != null && player != null) {
			if (trackList.getList().getSelectedIndex() != -1) {
				int newOne = trackList.getList().getSelectedIndex();
				disableAllStreams();
				this.pracTrackList.getList().clearSelection();
				this.regTrackList.getList().clearSelection();
				player.setTrack(tracks.get(newOne));
				enableStreams();
				this.slideDuration.setMaximum(player.getTotalTime() / 10);
			}
		}
	}

	private void enableTrackNav() throws Exception {
		if (this.player.getTrackCount() > 1) {
			jTabbedPane.setVisible(true);
			int counter = 0;
			while (counter < player.getTrackCount()) {
				MGJTrack x = player.getTracks()[counter];
				if ((x.trackname != null) && (!(x.trackname.equals("")))) {
					if (x.trackname.contains("Tutorial")) {
						pracTracks.add(x);
					} else if (x.trackname.contains("Practice Mode")) {
						pracTracks.add(x);
					} else if (x.trackname.contains("(Preview)")) {
						pracTracks.add(x);
					} else if (x.trackname.contains("DEBUG")) {
						pracTracks.add(x);
					} else {
						regTracks.add(x);
					}
				} else {
					tracks.add(x);
				}
				counter = counter + 1;
			}
			counter = 0;
			Collections.sort(regTracks);
			Collections.sort(pracTracks);
			Collections.sort(tracks);
			DefaultListModel lm = new DefaultListModel();
			lm.clear();
			if (regTracks.size() > 0) {
				while (counter < regTracks.size()) {
					String theName = regTracks.get(counter).trackname;
					if ((regTracks.get(counter).trackartist != null) && (!(regTracks.get(counter).trackartist.equals("")))) {
						theName = theName + " - " + regTracks.get(counter).trackartist;
					}
					lm.addElement(theName);
					counter = counter + 1;
				}
				regTrackList.getList().setModel(lm);
				regTrackList.setEnabled(true);
				regTrackList.setVisible(true);
				regTrackList.getList().setEnabled(true);
			}

			counter = 0;
			DefaultListModel lmprac = new DefaultListModel();
			lmprac.clear();
			if (pracTracks.size() > 0) {
				while (counter < pracTracks.size()) {
					String theName = pracTracks.get(counter).trackname;
					if ((pracTracks.get(counter).trackartist != null) && (!(pracTracks.get(counter).trackartist.equals("")))) {
						theName = theName + " - " + pracTracks.get(counter).trackartist;
					}
					lmprac.addElement(pracTracks.get(counter).trackname);
					counter = counter + 1;
				}
				pracTrackList.getList().setModel(lmprac);
				pracTrackList.setEnabled(true);
				pracTrackList.setVisible(true);
				pracTrackList.getList().setEnabled(true);
			}

			counter = 0;
			DefaultListModel lmother = new DefaultListModel();
			lmother.clear();
			if (tracks.size() > 0) {
				while (counter < tracks.size()) {
					lmother.addElement(tracks.get(counter).filenamepath + "/" + tracks.get(counter).filename);
					counter = counter + 1;
				}
				trackList.getList().setModel(lmother);
				trackList.setEnabled(true);
				trackList.setVisible(true);
				trackList.getList().setEnabled(true);
			}
			//only show tabs that we actually will use
			if (regTracks.size() == 0) {
				jTabbedPane.setEnabledAt(0, false);
			}
			if (pracTracks.size() == 0) {
				jTabbedPane.setEnabledAt(1, false);
			}
			if (tracks.size() == 0) {
				jTabbedPane.setEnabledAt(2, false);
			}

			//start playing
			if (regTracks.size() > 0) {
				player.setTrack(regTracks.get(0));
				regTrackList.getList().setSelectedIndex(0);
				jTabbedPane.setSelectedIndex(0);
			} else if (tracks.size() > 0) {
				player.setTrack(tracks.get(0));
				trackList.getList().setSelectedIndex(0);
				jTabbedPane.setSelectedIndex(2);
			} else if (pracTracks.size() > 0) {
				player.setTrack(pracTracks.get(0));
				pracTrackList.getList().setSelectedIndex(0);
				jTabbedPane.setSelectedIndex(1);
			} else {
				JOptionPane.showMessageDialog(this, "This disc contains no playable tracks.", "No playable tracks", JOptionPane.WARNING_MESSAGE);
				closeAudio();
			}
		}
	}

	private void disablePlayControls() {
		slideDuration.setValue(0);
		slideDuration.setVisible(false);
		this.lblDuration.setText("0:00 / 0:00");
		lblDuration.setVisible(false);
		this.btnPlay.setVisible(false);
	}

	private void enablePlayControls() {
		slideDuration.setValue(0);
		slideDuration.setVisible(true);
		updateControls();
		lblDuration.setVisible(true);
		this.btnPlay.setVisible(true);
		if (player.getDiscType().equals(MGJPlayer.DISCTYPE_NODISC)) {
			this.lblCurOpen2.setText(player.getRootfile().getPath());
		} else {
			if (player.getRootfile().getParent() != null) {
				this.lblCurOpen2.setText(player.getDiscType() + " (Archive)");
			} else {
				this.lblCurOpen2.setText(player.getDiscType());
			}
		}
		this.lblCurOpen2.setEnabled(true);
		this.slideDuration.setMaximum(player.getTotalTime() / 10);
	}

	private void openDisc() throws Exception {
		File[] drives = File.listRoots();
		String[] drivenames = new String[drives.length];
		int counter = 0;
		while (counter < drives.length) {
			drivenames[counter] = drives[counter].getPath();
			counter = counter + 1;
		}
		String answer = (String)JOptionPane.showInputDialog(this,
				"Choose the drive that contains\n your music game disc", "Choose a disc drive",
				JOptionPane.QUESTION_MESSAGE, null, drivenames, null);
		if ((answer != null) && (answer.length() > 0)) {
		    openArchive(answer);
		}
	}

    private void openFileFolder() throws Exception {
		int response = sfc.showOpenDialog(this);
		if (response == JFileChooser.CANCEL_OPTION) {
			return;
		}
		File file = sfc.getSelectedFile();
		openArchive(file.getPath());
	}

    private void openArchive(String fileloc) throws Exception {
    	closeAudio();
    	try {
    		PlayerLoaderDialog pld = new PlayerLoaderDialog(fileloc, this);
    		player = pld.getPlayer();
    	} catch (Exception err) {
    		err.printStackTrace();
			JOptionPane.showMessageDialog(this, err.getMessage(), "Invalid file", JOptionPane.ERROR_MESSAGE);
			player = null;
			return;
    	}
    	menuiClose.setEnabled(true);
		enableStreams();
		enablePlayControls();
		enableTrackNav();
		play();
    }

    public void errorLoadingPlayer(Exception err) {
    	err.printStackTrace();
		JOptionPane.showMessageDialog(this, err.getMessage(), "Invalid file", JOptionPane.ERROR_MESSAGE);
		player = null;
    }

    public void updateControls() {
    	if (player != null) {
	    	this.slideDuration.setValue(player.getOffset() / 10);
	    	this.lblDuration.setText(StringUtils.convertMillisToHoursMinutesSeconds(player.getOffset())
	    			+ " / " + StringUtils.convertMillisToHoursMinutesSeconds(player.getTotalTime()));
    	}
    }

    public void endOfStream() {
    	stop();
    	try {
    		player.setOffset(0);
    	} catch (Exception err) {
    		err.printStackTrace();
    	}
	}

    public void errorReadingDisc() {
    	closeAudio();
    }

	/**
	 * User is trying to close; do any necessary tidying up
	 */
	protected boolean canClose() {
    	closeAudio();
		return true;
	}

}
