package malictus.musicgamejukebox.ui;

import java.io.*;
import malictus.musicgamejukebox.player.*;
import malictus.robusta.swing.*;

public class PlayerLoaderDialog extends SmartProgressWindow {

	MGJPlayer player = null;
	MGJMainWindow parent = null;

	public PlayerLoaderDialog(String fileloc, MGJMainWindow parent) throws Exception {
		super(parent, false, true, false);
		this.setTitle("Reading contents");
		this.parent = parent;
		this.setStatus("Reading contents... please wait");
		final String FILELOC = fileloc;
		final MGJMainWindow PARENT = parent;
		Runnable q = new Runnable() {
            public void run() {
            	try {
            		player = new MGJPlayer(new File(FILELOC), PARENT);
            		setVisible(false);
            	} catch (Exception err) {
            		setVisible(false);
            		PARENT.errorLoadingPlayer(err);
            	}
            }
        };
        Thread t = new Thread(q);
        t.start();
        setVisible(true);
	}

	public MGJPlayer getPlayer() {
		return player;
	}

}