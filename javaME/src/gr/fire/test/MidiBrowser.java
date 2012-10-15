package gr.fire.test;

import gr.fire.browser.Browser;
import gr.fire.browser.util.Page;
import gr.fire.core.CommandListener;
import gr.fire.core.Component;
import gr.fire.core.FireScreen;
import gr.fire.core.Panel;
import gr.fire.ui.FireTheme;
import gr.fire.util.Log;

import java.io.IOException;

import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.media.MediaException;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class MidiBrowser extends MIDlet implements CommandListener {
	FireScreen screen;
	public static MidiBrowser instance;
	private Browser b;
	public static Panel containerPanel;

	public static final String landingPage = "http://www.duosuccess.com";

	// String landingPage =
	// "http://www.duosuccess.com/tcm/001a01080301b01aj.htm";

	public MidiBrowser() {
		// TODO Auto-generated constructor stub
	}

	protected void destroyApp(boolean unconditional)
			throws MIDletStateChangeException {
		// TODO Auto-generated method stub

	}

	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	protected void startApp() throws MIDletStateChangeException {
		// TODO Auto-generated method stub

		System.out.println("start app");
		instance = this;
		if (b != null) {
			System.out.println("resume from bg.");
			return;
		}
		screen = FireScreen.getScreen(Display.getDisplay(this));
		screen.setFullScreenMode(true); // on full screen mode

		try {
			// load a theme file.
			FireScreen.setTheme(new FireTheme("file://theme.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		// initialize a browser instance
		b = new Browser();

		try {

			loadPage(landingPage);
		} catch (Exception e) {
			// Use the Log class of the fire utility classes to easily log
			// errors.
			// Check the BrowserTest.java application and the javadoc for more
			// info on
			// the Log class and the Logger interface.
			Log.logError("Failed to load Browser.", e);
		}

	}

	public void commandAction(javax.microedition.lcdui.Command c, Component cmp) {
		Log.logInfo("Command type is " + c.getCommandType());
		if (b.midiPlayer != null) {
			try {
				b.midiPlayer.stop();
			} catch (MediaException e) {
				e.printStackTrace();
			}
		}
		if (Command.EXIT == c.getCommandType()) {
			notifyDestroyed();
			return;
		} else if (Command.BACK == c.getCommandType()) {
			loadPage(landingPage);
		} else if (Command.OK == c.getCommandType()) {
			loadPage(b.getCurrentUrl());
		}
	}

	private void loadPage(String url) {
		try {
			Page p = b.loadPage(url, HttpConnection.GET, null, null);
			containerPanel = new Panel(p.getPageContainer(),
					Panel.HORIZONTAL_SCROLLBAR | Panel.VERTICAL_SCROLLBAR, true);
			containerPanel.setCommandListener(this); // listen for events on
														// this
			if (MidiBrowser.landingPage.equals(b.getCurrentUrl())) {
				Log.logInfo("response url is landing page " + MidiBrowser.landingPage
						+ " set to exit.");
				containerPanel.setLeftSoftKeyCommand(new Command("退出",
						Command.EXIT, 1));
			} else {
				containerPanel.setLeftSoftKeyCommand(new Command("后退",
						Command.BACK, 1));
			}
			containerPanel.setRightSoftKeyCommand(new Command("刷新",
					Command.OK, 1));
			containerPanel.setDragScroll(true); // This enables the Drag scroll
												// function
			// for this Panel.
			containerPanel.setLabel(p.getPageTitle()); // The html page has a
														// title tag,
			// display it as a label on the
			// panel
			screen.setCurrent(containerPanel); // show the panel on the screen.
		} catch (Exception e) {
			Log.logError("Failed to load Browser.", e);
		}
	}

	public void commandAction(javax.microedition.lcdui.Command c,
			Displayable arg1) {
		Log.logInfo("Command type is " + c.getCommandType());
	}

}
