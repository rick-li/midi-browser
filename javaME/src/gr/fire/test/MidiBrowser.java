package gr.fire.test;

import gr.fire.browser.Browser;
import gr.fire.browser.util.Page;
import gr.fire.core.CommandListener;
import gr.fire.core.Component;
import gr.fire.core.FireScreen;
import gr.fire.core.Panel;
import gr.fire.ui.Alert;
import gr.fire.ui.FireTheme;
import gr.fire.util.Log;

import java.io.IOException;
import java.util.Timer;

import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class MidiBrowser extends MIDlet implements CommandListener {
	FireScreen screen;
	public static MidiBrowser instance;
	private Browser b;
	public static Panel containerPanel;

	public static final String landingPage = "http://www.duosuccess.com";
//	public static final String landingPage = "http://10.114.199.114/cv.html";

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

	Timer heartbeatTimer;

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
		b.setImageLoadingPolicy(Browser.NO_IMAGES);
		loadPage(landingPage);

	}

	public void commandAction(javax.microedition.lcdui.Command c, Component cmp) {
		Log.logInfo("Command type is " + c.getCommandType());
		if (b.midiPlayer != null) {
			try {
				b.midiPlayer.stop();
			} catch (Exception e) {
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

	public void loadPage(String url) {
		try {
			Page p = b.loadPage(url, HttpConnection.GET, null, null);
			containerPanel = new Panel(p.getPageContainer(),
					Panel.HORIZONTAL_SCROLLBAR | Panel.VERTICAL_SCROLLBAR, true);
			containerPanel.setCommandListener(this); // listen for events on
														// this
			if (MidiBrowser.landingPage.equals(b.getCurrentUrl())) {
				Log.logInfo("response url is landing page "
						+ MidiBrowser.landingPage + " set to exit.");
				containerPanel.setLeftSoftKeyCommand(new Command("退出",
						Command.EXIT, 1));
				containerPanel.setRightSoftKeyCommand(new Command("刷新", Command.OK,
						1));
			} else {
				containerPanel.setLeftSoftKeyCommand(new Command("退出",
						Command.EXIT, 1));
				containerPanel.setRightSoftKeyCommand(new Command("后退",
						Command.BACK, 1));
			}
			containerPanel.setDragScroll(true); // This enables the Drag scroll
												// function
			// for this Panel.
			containerPanel.setLabel(p.getPageTitle()); // The html page has a
														// title tag,
			// display it as a label on the
			// panel
			screen.setCurrent(containerPanel); // show the panel on the screen.
		} catch (Exception e) {
			FireScreen.getScreen().showAlert("页面加载失败，请检查网络是否打开。",
					Alert.TYPE_ERROR, Alert.USER_SELECTED_OK,
					new Command("退出", Command.EXIT, 1), this);
			Log.logError("Failed to load Browser.", e);
		}
	}

	public void commandAction(javax.microedition.lcdui.Command c,
			Displayable arg1) {
		Log.logInfo("Command type is " + c.getCommandType());
	}

}
