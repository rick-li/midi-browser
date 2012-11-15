package com.duo.midi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.Toast;

import com.duo.midi.alarm.WakefulIntentService;
import com.duo.midi.music.MusicRepeatListener;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.actionbar.R;

public class MusicFragment extends Fragment {
	private static final String TAG = "midi-browser";
	private static final String KEY_CONTENT = "MusicFragment:Content";

//	private static final String homeUrl = "http://www.duosuccess.com";
	 private static final String homeUrl = "http://rick-li.github.com/android-midi/index.html";
	// private static final String homeUrl = "http://www.baidu.com";
	private String strBaseDir = Environment.getExternalStorageDirectory()
			.getPath() + "/duosuccess";
	private String tmpMidiFile = strBaseDir + "/tmpMid.mid";
	private MediaPlayer mp = new MediaPlayer();
	public static WebView webView;
	private Timer musicTimer;
	private Timer waitTimer;
	private volatile boolean needRepeat = false;
	private String mContent = "music";

	//private int waitInterval = 1 * 60 * 60 * 1000 + 5 * 60 * 1000;
//	private int musicDuration = 1 * 60 * 60 * 1000;
	
//	private int waitInterval = 10 * 1000;
	private int musicDuration = 5 * 1000;
	
	enum STATE {
		stop {
			public String toString() {
				return "停止";
			}
		},
		wait {
			public String toString() {
				return "等待";
			}
		},
		play {
			public String toString() {
				return "播放";
			}
		}
	}


	private ActionBar footer;
	private Handler handler;
	private ProgressDialog pd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if ((savedInstanceState != null)
				&& savedInstanceState.containsKey(KEY_CONTENT)) {
			mContent = savedInstanceState.getString(KEY_CONTENT);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.music, null);

		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		handler = new Handler();
		final ActionBar actionBar = (ActionBar) this.getView().findViewById(
				R.id.actionbar);

		footer = (ActionBar) this.getView().findViewById(R.id.bottombar);
		footer.setTitle("停止");
		actionBar.setHomeAction(new Action() {

			@Override
			public int getDrawable() {
				return R.drawable.ic_title_home_default;
			}

			@Override
			public void performAction(View view) {
				webView.loadUrl(homeUrl);
			}

		});
		actionBar.addAction(new Action() {

			@Override
			public int getDrawable() {
				return R.drawable.ic_menu_rotate_disable;
			}

			@Override
			public void performAction(View view) {
				ImageButton labelView = (ImageButton) view
						.findViewById(R.id.actionbar_item);
				needRepeat = !needRepeat;
				if (needRepeat) {
					labelView.setImageResource(R.drawable.ic_menu_rotate);
					Toast.makeText(MusicFragment.this.getActivity(),
							"设定为每隔一小时播放", 1000).show();
				} else {
					labelView
							.setImageResource(R.drawable.ic_menu_rotate_disable);
					Toast.makeText(MusicFragment.this.getActivity(), "设定为单次播放",
							1000).show();
				}
			}

		});
		// refresh button
		actionBar.addAction(new Action() {

			@Override
			public int getDrawable() {
				return R.drawable.ic_menu_refresh;
			}

			@Override
			public void performAction(View view) {
				actionBar.setProgressBarVisibility(View.VISIBLE);
				clearCache();
				webView.reload();
			}
		});

		// close button
		actionBar.addAction(new Action() {

			@Override
			public int getDrawable() {
				return R.drawable.ic_menu_close_clear_cancel;
			}

			@Override
			public void performAction(View view) {
				stopMedia();
				clearCache();
				MusicFragment.this.getActivity().finish();
				return;
			}

		});

		webView = (WebView) this.getView().findViewById(R.id.webView);
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		WebView.enablePlatformNotifications();
		settings.setDefaultZoom(ZoomDensity.FAR);
		settings.setBuiltInZoomControls(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);

		// auto clear cache.
		clearCache();
		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				Log.i(TAG, "Page started " + url);
				try{
				pd = ProgressDialog.show(MusicFragment.this.getActivity(), "",
						"请稍侯");
				new Timer().schedule(new TimerTask(){

					@Override
					public void run() {
						pd.dismiss();
					}
					
				}, 30*1000);
				stopMedia();
				}catch(Exception e){}
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.i(TAG, "Should overrid " + url);
				view.loadUrl(url);
				stopMedia();
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				// Avoid music to auto stop;
				webView.loadUrl("javascript:window.stopmusic = function(){}");
				webView.loadUrl("javascript:midiExtractor.extract(document.querySelector('embed').src, window.location.href);");
				actionBar.setProgressBarVisibility(View.INVISIBLE);
				pd.dismiss();
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				pd.dismiss();
			}
		});

		class MidiExtractor {
			public void extract(String midiFile, String pageUrl) {
				Log.i(TAG, "pageUrl is " + pageUrl);
				String midiUrl = midiFile;
				if (!midiFile.startsWith("http")) {
					midiUrl = pageUrl.substring(0, pageUrl.lastIndexOf("/"))
							+ "/" + midiFile;
				}
				Log.i(TAG, "midi is " + midiUrl);
				pd = ProgressDialog.show(MusicFragment.this.getActivity(), "",
						"请稍侯");
				clearCache();
				try {
					URLConnection cn = new URL(midiUrl).openConnection();
					InputStream stream = cn.getInputStream();
					byte[] buffer = new byte[512];
					Log.i(TAG, "Checking baseDir " + strBaseDir);
					File dir = new File(strBaseDir);
					if (!dir.exists()) {
						Log.i(TAG, "base dir " + strBaseDir
								+ " not exist, create new. ");
						dir.mkdir();
					}
					File tmpMidFile = new File(tmpMidiFile);
					tmpMidFile.createNewFile();
					FileOutputStream fos = new FileOutputStream(tmpMidFile);
					int n = -1;
					while ((n = stream.read(buffer)) != -1) {
						fos.write(buffer, 0, n);
					}
					fos.close();
					stream.close();
					pd.dismiss();
					playMusic(tmpMidFile);
					
				} catch (Exception e) {
					Log.e(TAG, "unable to play midi. ", e);
				}
			}
		}

		webView.addJavascriptInterface(new MidiExtractor(), "midiExtractor");
		webView.loadUrl(homeUrl);
		super.onViewCreated(view, savedInstanceState);
	}

/*	private void startWaitCountdown() {
		waitTimer = new Timer();
		final TimeCounter timeCounter = new TimeCounter();
		waitTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				timeCounter.increaseOneSec();

				setFooterText(STATE.wait
						+ " "
						+ new SimpleDateFormat("mm:ss").format(new Date(
								timeCounter.getStartMillSec())));

				if (timeCounter.getStartMillSec() == waitInterval) {
					waitTimer.cancel();
					webView.reload();
				}
			}

		}, 0, 1000);
	}
*/
	private void playMusic(File tmpMidFile) throws Exception {
		mp = new MediaPlayer();
		mp.setDataSource(MusicFragment.this.getActivity(),
				Uri.fromFile(tmpMidFile));
		mp.prepare();
		mp.setLooping(true);
		mp.start();
		final Date startDate = new Date();

		final SimpleDateFormat startSdf = new SimpleDateFormat("MM/dd HH:mm:ss");

		Log.i(TAG, "music start " + new Date());
		// stop after 1 hour
		if (musicTimer != null) {
			musicTimer.cancel();
		}
		musicTimer = new Timer();
		final TimeCounter timeCounter = new TimeCounter();
		musicTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				timeCounter.increaseOneSec();

				setFooterText("开始时间 "
						+ startSdf.format(startDate)
						+ " 已播放 "
						+ new SimpleDateFormat("mm:ss").format(new Date(
								timeCounter.getStartMillSec())));
				if (timeCounter.getStartMillSec() == musicDuration) {
					mp.stop();
					if (needRepeat && !alarmStarted) {
						WakefulIntentService.scheduleAlarms(new MusicRepeatListener(),
	                            getActivity(), false);
						alarmStarted = true;
						Log.i(TAG, "Start schedule alarms.");
					} else {
						webView.loadUrl(homeUrl);
					}
					Log.i(TAG, "music stop " + new Date().toString());

					musicTimer.cancel();
				}

			}
		}, 0, 1000);
	}
	private boolean alarmStarted = false;
	private void setFooterText(final String text) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				footer.setTitle(text);
			}
		});
	}

	class TimeCounter {
		int startMillSec = 0;

		void increaseOneSec() {
			startMillSec += 1000;
		}

		int getStartMillSec() {
			Log.d(TAG, "StartMillSec: " + startMillSec);
			return startMillSec;
		}
	}

	private void clearCache() {
		webView.clearCache(true);
		File tmpMidi = new File(tmpMidiFile);
		if (tmpMidi.exists()) {
			boolean status = tmpMidi.delete();
			Log.i(TAG, "TMP midi file removed status: " + status);
		}
	}

	private void stopMedia() {
		if (mp.isPlaying()) {
			mp.stop();
			Log.i(TAG, "media is stopped.");
		}
		setFooterText(STATE.stop.toString());
		if (musicTimer != null) {
			musicTimer.cancel();
		}
		if (waitTimer != null) {
			waitTimer.cancel();
		}
	}

	public void onBackPressed() {
		if (webView != null && webView.canGoBack()) {
			webView.goBack();
		} else {
			// MusicFragment.this.getActivity().onBackPressed();
		}
		stopMedia();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_CONTENT, mContent);
	}

	public WebView getWebView() {
		return webView;
	}

	public void setWebView(WebView webView) {
		this.webView = webView;
	}

	public boolean isNeedRepeat() {
		return needRepeat;
	}

	public void setNeedRepeat(boolean needRepeat) {
		this.needRepeat = needRepeat;
	}

	
	
}
